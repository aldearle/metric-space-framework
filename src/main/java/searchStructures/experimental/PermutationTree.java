package searchStructures.experimental;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;
import searchStructures.SemiSorter;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannonViaSed;

public class PermutationTree<T> extends SearchIndex<T> {

	PNode root;
	int treeNodes = 0;
	int leafNodes = 0;
	private int noOfPermutants;
	List<T> permutants;
	private int maxLeaf;
	private int leafDataSize;
	private boolean foreignPermutants;

	public class PLeaf {
		List<T> leafData;

		PLeaf() {
			this.leafData = new ArrayList<>();
			leafNodes++;
		}

		public void query(T q, double t, List<T> res) {
			for (T d : this.leafData) {
				double dist = PermutationTree.this.metric.distance(d, q);
				if (dist < t) {
					res.add(d);
				}
			}
		}

		public void addDatum(T d) {
			this.leafData.add(d);
			PermutationTree.this.maxLeaf = Math.max(
					PermutationTree.this.maxLeaf, this.leafData.size());
			leafDataSize++;
		}
	}

	public class PNode {
		boolean terminal;
		int p1, p2;
		PNode left, right;
		PLeaf leftL, rightL;
		double crL, crR;
		double pDist;
		double hhtDist;
		int noOfData;

		PNode(int maxDepth, int depth, PTBuildAction.PNumGen png) {
			PermutationTree.this.treeNodes++;
			this.crL = 0;
			this.crR = 0;
			this.noOfData = 0;
			this.p1 = png.left;
			this.p2 = png.right;
			this.pDist = PermutationTree.this.metric.distance(
					PermutationTree.this.permutants.get(this.p1),
					PermutationTree.this.permutants.get(this.p2));
			if (depth < maxDepth) {
				this.terminal = false;
				this.left = new PNode(maxDepth, depth + 1, png.getNext());
				this.right = new PNode(maxDepth, depth + 1, png.getNext());
			} else {
				this.terminal = true;
				this.leftL = new PLeaf();
				this.rightL = new PLeaf();
			}
		}

		public void query(T q, double t, double[] pDists, List<T> res) {

			// double diff = Math.abs(pDists[this.p1] - pDists[this.p2]);
			// boolean canExclude = diff > 2 * t;
			boolean canExclude = false;
			double projD = 0;
			if (this.noOfData == 0) {
				canExclude = true;
			} else {
				projD = projectionDistance(this.pDist,
						getPDist(q, pDists, this.p1),
						getPDist(q, pDists, this.p2));
				canExclude = Math.abs(projD - this.hhtDist) > t;
			}

			boolean excL = false;
			boolean excR = false;
			if (canExclude) {
				if (this.noOfData <= 1) {
					excR = true;
					excL = true;
				} else if (projD < this.hhtDist) {
					excR = true;
				} else {
					excL = true;
				}
			}
			if (!excL) {
				if (getPDist(q, pDists, this.p1) < this.crL + t) {
					if (this.terminal) {
						this.leftL.query(q, t, res);
					} else {
						this.left.query(q, t, pDists, res);
					}
				}
			}
			if (!excR) {
				if (getPDist(q, pDists, this.p2) < this.crR + t) {
					if (this.terminal) {
						this.rightL.query(q, t, res);
					} else
						this.right.query(q, t, pDists, res);
				}
			}
		}

		void addDataBalanced(List<T> dataToAdd) {
			this.noOfData = dataToAdd.size();
			if (this.noOfData == 1) {
				T datumToAdd = dataToAdd.get(0);
				double d1 = PermutationTree.this.metric.distance(datumToAdd,
						PermutationTree.this.permutants.get(PNode.this.p1));
				double d2 = PermutationTree.this.metric.distance(datumToAdd,
						PermutationTree.this.permutants.get(PNode.this.p2));
				this.hhtDist = projectionDistance(PNode.this.pDist, d1, d2);

				if (this.terminal) {
					this.crR = d2;
					this.rightL.addDatum(datumToAdd);
				} else {
					// happens only if tree is deeper than necessary to hold
					// data
					this.crR = d2;
					this.right.addDataBalanced(dataToAdd);
				}

			} else {
				SemiSorter<T> ss = new SemiSorter<T>(dataToAdd) {
					@Override
					public double measure(T d) {
						double d1 = PermutationTree.this.metric.distance(d,
								PermutationTree.this.permutants
										.get(PNode.this.p1));
						double d2 = PermutationTree.this.metric.distance(d,
								PermutationTree.this.permutants
										.get(PNode.this.p2));
						return projectionDistance(PNode.this.pDist, d1, d2);
					}
				};
				final List<T> left2 = ss.getLeft();
				final List<T> right2 = ss.getRight();
				crL = listMaxDist(
						PermutationTree.this.permutants.get(PNode.this.p1),
						left2);
				crR = listMaxDist(
						PermutationTree.this.permutants.get(PNode.this.p2),
						right2);
				this.hhtDist = ss.getPivotDistance();
				if (this.terminal) {
					for (T d : left2) {
						this.leftL.addDatum(d);
					}
					for (T d : right2) {
						this.rightL.addDatum(d);
					}
				} else {
					this.left.addDataBalanced(left2);
					this.right.addDataBalanced(right2);
				}
			}
		}

	}

	protected PermutationTree(List<T> data, Metric<T> metric,
			EuclideanCorners ec) {
		super(data, metric);
		this.foreignPermutants = true;
		int n = data.size();
		int depth = 1;
		int lwb = 2;
		while (lwb <= n) {
			lwb *= 2;
			depth++;
		}
		this.noOfPermutants = permutantsRequired((int) (Math.pow(2, depth) - 1));
		final PTBuildAction.PNumGen png = new PTBuildAction.PNumGen();

		this.permutants = new ArrayList<>();
		for (int i = 0; i < noOfPermutants; i++) {
			this.permutants.add((T) new CartesianPoint(ec.nextCorner()));
		}

		this.root = new PNode(depth, 1, png);
		this.root.addDataBalanced(this.data);
	}

	public PermutationTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.foreignPermutants = false;
		this.maxLeaf = 0;

		int n = data.size();
		int depth = getRequiredDepth(n);
		// experimental depth adjustment
		// depth += 6;
		//

		// System.out.println("data size is " + n + ", internal tree depth is "
		// + depth);
		this.noOfPermutants = permutantsRequired((int) (Math.pow(2, depth) - 1));
		this.permutants = new ArrayList<>();
		for (int i = 0; i < noOfPermutants; i++) {
			this.permutants.add(this.data.remove(0));
		}

		// System.out.println("permutants required: " + this.noOfPermutants);

		final PTBuildAction.PNumGen png = new PTBuildAction.PNumGen();

		this.root = new PNode(depth, 1, png);

		// System.out.println(this.treeNodes + " tree nodes, " + this.leafNodes
		// + " leaf nodes");

		this.root.addDataBalanced(this.data);
		// System.out.println("max leaf size is " + this.maxLeaf);
	}

	protected int getRequiredDepth(int n) {
		int depth = 1;
		while (dataSizeForTreeDepth(depth) < n) {
			depth++;
		}
		return depth;
	}

	private static <T> int doSerial(List<T> data, List<T> queries, Metric<T> m,
			double t) {
		int hits = 0;
		for (T p1 : data) {
			for (T p2 : queries) {
				if (m.distance(p1, p2) < t) {
					hits++;
				}
			}
		}
		return hits;
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		double[] pDists = new double[this.noOfPermutants];
		for (int i = 0; i < pDists.length; i++) {
			pDists[i] = -1;
		}
		for (int i = 0; i < pDists.length; i++) {
			pDists[i] = this.metric.distance(query, this.permutants.get(i));
			if (pDists[i] < t) {
				res.add(this.permutants.get(i));
			}
		}
		this.root.query(query, t, pDists, res);
		return res;
	}

	private double getPDist(T query, double[] pDists, int index) {
		if (pDists[index] == -1) {
			pDists[index] = metric.distance(query, permutants.get(index));
		}
		return pDists[index];
	}

	@Override
	public String getShortName() {
		return "PermTree";
	}

	public static int permutantsRequired(int noOfInternalNodes) {
		return (int) Math.ceil((1 + Math.sqrt(1 + 8 * noOfInternalNodes)) / 2);
	}

	public static int dataSizeForTreeDepth(int depth) {
		int leaves = (int) Math.pow(2, depth);
		int perms = permutantsRequired(leaves - 1);
		return leaves + perms;
	}

}
