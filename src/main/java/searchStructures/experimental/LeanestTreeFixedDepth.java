package searchStructures.experimental;

import java.util.ArrayList;
import java.util.List;

import searchStructures.ListOfPermPartitions;
import searchStructures.SearchIndex;
import searchStructures.SemiSorter;
import testloads.TestLoad;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

/**
 * @author newrichard
 *
 *         a balanced hyperplane tree with hardly any pivots!
 */
public class LeanestTreeFixedDepth<T> extends SearchIndex<T> {

	public int treeDepth;
	private boolean hilbertMetric;
	ArrayList<T> pivots;
	double[][] interPivotDistances;
	double[] queryToPivotDistances;

	private class HeadNode {
		int lHead, rHead;
		TreeNode lTree, rTree;
		double lCR, rCR;
		double l_r_dist;
		double offset;

		@SuppressWarnings("synthetic-access")
		HeadNode(List<T> data) {
			// assume more than two nodes!
			this.lHead = 0;
			this.rHead = 1;
			this.l_r_dist = interPivotDistances[0][1];

			SemiSorter<T> ss = getSemiSorter(data, 0, 1, this.l_r_dist);

			this.offset = ss.getPivotDistance();
			List<T> lList = ss.getLeft();
			List<T> rList = ss.getRight();

			this.lCR = listMaxDist(pivots.get(this.lHead), lList);
			this.rCR = listMaxDist(pivots.get(this.rHead), rList);

			this.lTree = new TreeNode(this.lHead, lList, 1);
			this.rTree = new TreeNode(this.rHead, rList, 1);
		}

		@SuppressWarnings("synthetic-access")
		public void thresholdSearch(T query, double t, List<T> res) {
			double dLeft, dRight;
			dLeft = queryToPivotDistances[this.lHead];
			dRight = queryToPivotDistances[this.rHead];

			if (this.lTree != null) {
				if (!excludeCR(dLeft, t, this.lCR)
						&& !(excludeVor(dLeft, dRight, t, this.offset))
						&& !(excludeHilbert(this.l_r_dist, dRight, dLeft, t,
								this.l_r_dist - this.offset))) {

					this.lTree.thresholdSearch(query, t, dLeft, res, 1);
				}
				if (!excludeCR(dRight, t, this.rCR)
						&& !(excludeVor(dRight, dLeft, t, -this.offset))
						&& !(excludeHilbert(this.l_r_dist, dLeft, dRight, t,
								this.offset))) {

					this.rTree.thresholdSearch(query, t, dRight, res, 1);
				}
			}
		}

	}

	/**
	 * a GHTNode has two pivot nodes, lHead and rHead with each pivot is
	 * associated another GHTnode,lTree and rTree
	 * 
	 * special cases: there may be zero or one pivot nodes, in which case both
	 * lTree and rTree are null
	 * 
	 *
	 */
	private class TreeNode {

		boolean isNull;
		T leafDatum;
		double lCR, rCR;
		double l_r_dist;
		int lHead, rHead;
		TreeNode lTree, rTree;
		double offset;

		TreeNode(int upperNode, List<T> data, int depth) {

			this.lCR = 0;
			this.rCR = 0;
			/*
			 * now constructing this tree to the given depth whether there are
			 * any data there or not...
			 */

			if (depth == treeDepth) {
				if (data == null || data.size() == 0) {
					this.isNull = true;
				} else if (data.size() > 1) {
					throw new RuntimeException(
							"too much data for the fixed depth tree");
				} else {
					// place single data node - the norm
					this.leafDatum = data.get(0);
				}
			} else {
				if (data == null || data.size() == 0) {
					this.isNull = true;
				} else if (data.size() == 1) {
					// need only to push the datum down the correct side of the
					// Hilbert partition

					this.lHead = upperNode;
					this.rHead = depth + 1;
					this.l_r_dist = interPivotDistances[lHead][rHead];

					final double dToLHead = metric.distance(data.get(0),
							pivots.get(lHead));
					this.offset = projectionDistance(l_r_dist, dToLHead,
							metric.distance(data.get(0), pivots.get(rHead)));

					this.lCR = dToLHead;

					this.lTree = new TreeNode(this.lHead, data, depth + 1);
					this.rTree = new TreeNode(this.rHead, null, depth + 1);

				} else {
					// more than two nodes in data so requires recursion
					T piv = pivots.get(depth + 1);

					this.lHead = upperNode;
					this.rHead = depth + 1;
					this.l_r_dist = interPivotDistances[lHead][rHead];

					SemiSorter<T> ss = getSemiSorter(data, this.lHead,
							this.rHead, this.l_r_dist);

					this.offset = ss.getPivotDistance();
					List<T> lList = ss.getLeft();
					List<T> rList = ss.getRight();
					this.lCR = listMaxDist(pivots.get(this.lHead), lList);
					this.rCR = listMaxDist(pivots.get(this.rHead), rList);

					this.lTree = new TreeNode(this.lHead, lList, depth + 1);
					this.rTree = new TreeNode(this.rHead, rList, depth + 1);
				}
			}

		}

		public int cardinality(int depth) {
			if (this.isNull) {
				return 0;
			} else if (depth == treeDepth) {
				return 1;
			} else {
				return this.lTree.cardinality(depth + 1)
						+ this.rTree.cardinality(depth + 1);
			}
		}

		/**
		 * @param query
		 *            the query itself
		 * @param t
		 *            the threshold
		 * @param res
		 *            result list to be added to by side-effect
		 * @param dMin
		 *            the smallest distance that's been encountered above this
		 *            node of the tree
		 */
		@SuppressWarnings("synthetic-access")
		public void thresholdSearch(T query, double t, double upperDist,
				List<T> res, int depth) {
			double dLeft, dRight;
			if (this.isNull) {
			} else if (depth == treeDepth) {
				if (metric.distance(query, this.leafDatum) <= t) {
					res.add(this.leafDatum);
				}
			} else {
				dLeft = upperDist;
				dRight = queryToPivotDistances[this.rHead];

				if (this.lTree != null) {
					if (!excludeCR(dLeft, t, this.lCR)
							&& !(excludeVor(dLeft, dRight, t, this.offset))
							&& !(excludeHilbert(this.l_r_dist, dRight, dLeft,
									t, this.l_r_dist - this.offset))) {

						this.lTree.thresholdSearch(query, t, dLeft, res,
								depth + 1);
					}
					if (!excludeCR(dRight, t, this.rCR)
							&& !(excludeVor(dRight, dLeft, t, -this.offset))
							&& !(excludeHilbert(this.l_r_dist, dLeft, dRight,
									t, this.offset))) {

						this.rTree.thresholdSearch(query, t, dRight, res,
								depth + 1);
					}
				}

			}
		}

	}

	private LeanestTreeFixedDepth<T>.HeadNode head;

	private boolean crExclusionEnabled;

	private boolean vorExclusionEnabled;

	private boolean cosExclusionEnabled;

	public LeanestTreeFixedDepth(List<T> data, Metric<T> metric, int depth,
			boolean hilbertMetric) {
		super(data, metric);

		this.hilbertMetric = hilbertMetric;
		this.treeDepth = depth;
		this.pivots = new ArrayList<T>();
		for (int i = 0; i < depth + 1; i++) {
			pivots.add(data.remove(0));
		}

		intitialisePivotDistances();

		this.head = new HeadNode(data);

		this.crExclusionEnabled = true;
		if (hilbertMetric) {
			this.cosExclusionEnabled = true;
			this.vorExclusionEnabled = false;
		} else {
			this.cosExclusionEnabled = false;
			this.vorExclusionEnabled = true;
		}
	}

	private void intitialisePivotDistances() {
		this.interPivotDistances = new double[this.pivots.size()][this.pivots
				.size()];
		for (int i = 0; i < this.pivots.size(); i++) {
			for (int j = i + 1; j < this.pivots.size(); j++) {
				this.interPivotDistances[i][j] = this.metric.distance(
						this.pivots.get(i), this.pivots.get(j));
				this.interPivotDistances[j][i] = this.interPivotDistances[i][j];
			}
		}
	}

	/**
	 * @param cosExclusionEnabled
	 *            the cosExclusionEnabled to set
	 */
	public void setCosExclusionEnabled(boolean cosExclusionEnabled) {
		this.cosExclusionEnabled = cosExclusionEnabled;
	}

	/**
	 * @param crExclusionEnabled
	 *            the crExclusionEnabled to set
	 */
	public void setCrExclusionEnabled(boolean crExclusionEnabled) {
		this.crExclusionEnabled = crExclusionEnabled;
	}

	/**
	 * @param vorExclusionEnabled
	 *            the vorExclusionEnabled to set
	 */
	public void setVorExclusionEnabled(boolean vorExclusionEnabled) {
		this.vorExclusionEnabled = vorExclusionEnabled;
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();

		this.queryToPivotDistances = new double[this.pivots.size()];
		for (int i = 0; i < this.pivots.size(); i++) {
			final double d = metric.distance(query, pivots.get(i));
			queryToPivotDistances[i] = d;
			if (d <= t) {
				res.add(pivots.get(i));
			}
		}
		this.head.thresholdSearch(query, t, res);
		return res;
	}

	/**
	 * @param pivotDistance
	 *            distance between pivots
	 * @param closerDistance
	 *            distance from query to closer pivot
	 * @param furtherDistance
	 *            distance from query to further pivot
	 * @param threshold
	 * @return
	 */
	private boolean excludeCos(double pivotDistance, double closerDistance,
			double furtherDistance, double threshold) {
		double cosTheta = (pivotDistance * pivotDistance + closerDistance
				* closerDistance - furtherDistance * furtherDistance)
				/ (2 * pivotDistance * closerDistance);
		double projection = closerDistance * cosTheta;
		return this.cosExclusionEnabled
				&& (((pivotDistance / 2) - projection) > threshold);
	}

	private boolean excludeHilbert(double pivotDistance, double closerDistance,
			double furtherDistance, double threshold, double offset) {
		double projD = projectionDistance(pivotDistance, closerDistance,
				furtherDistance);
		final boolean res = offset - projD > threshold;
		return this.cosExclusionEnabled && res;
	}

	private boolean excludeCR(double dPivot, double threshold,
			double coveringRadius) {
		return LeanestTreeFixedDepth.this.crExclusionEnabled
				&& dPivot > coveringRadius + threshold;
	}

	private boolean excludeVor(double dLarge, double dSmall, double threshold,
			double offset) {
		return LeanestTreeFixedDepth.this.vorExclusionEnabled
				&& dLarge - dSmall > threshold * 2 + offset;
	}

	@Override
	public String getShortName() {
		return "ghmt";
	}

	protected SemiSorter<T> getSemiSorter(List<T> data, final int i,
			final int j, final double pivot_distance) {
		SemiSorter<T> ss;
		if (this.hilbertMetric) {
			ss = new SemiSorter<T>(data) {
				@Override
				public double measure(T d) {
					double d1 = metric.distance(pivots.get(i), d);
					double d2 = metric.distance(pivots.get(j), d);
					return projectionDistance(pivot_distance, d1, d2);
				}
			};
		} else {
			ss = new SemiSorter<T>(data) {
				@Override
				public double measure(T d) {
					double d1 = metric.distance(pivots.get(i), d);
					double d2 = metric.distance(pivots.get(j), d);
					return d1 - d2;
				}
			};
		}
		return ss;
	}

	static public void main(String[] a) throws Exception {
		// TestLoad t = new TestLoad(6, 100000 + 1, true, false);
		TestLoad.SisapFile testfile = TestLoad.SisapFile.colors;
		TestLoad t = new TestLoad(testfile);
		Metric<CartesianPoint> euc = new Euclidean<>();
		CountedMetric<CartesianPoint> m = new CountedMetric<>(euc);
		double[] thresh = TestLoad.getSisapThresholds(testfile);

		List<CartesianPoint> qs = t.getQueries(t.dataSize() / 10);
		final List<CartesianPoint> d = t.getDataCopy();
		SearchIndex<CartesianPoint> index;

		LeanestTreeFixedDepth<CartesianPoint> tr;// ee = new
													// LeanestTreeFixedDepth<>(
		// d, m, 30, true);//

		// VPTree<CartesianPoint> vpt = new VPTree<>(t.getDataCopy(), m);
		// SATGeneric<CartesianPoint> tree = new SATGeneric<>(d, m,
		// SATGeneric.Strategy.distal);

		System.out.println(d.size());
		ListOfPermPartitions<CartesianPoint> lop = new ListOfPermPartitions<>(
				t.getDataCopy(), m, 15);
		// ListOfClusters<CartesianPoint> loc = new ListOfClusters<>(
		// t.getDataCopy(), m, 25);

		index = lop;

		//
		// System.out.println(tree.head.lTree.cardinality());
		// System.out.println(tree.head.rTree.cardinality());

		// System.out
		// .println("threshold\tHyp dists\thyp time\thil dists\thil time");
		for (int i = 0; i < 1; i++) {
			for (double th : thresh) {
				System.out.print("\tt: " + th);
				int totalResults = 0;

				m.reset();
				// tree.setCosineTestEnabled(false);

				long t0 = System.currentTimeMillis();
				for (CartesianPoint p : qs) {
					List<CartesianPoint> res = index.thresholdSearch(p, th);
					totalResults += res.size();
				}
				System.out.print("\td per q: " + m.reset() / (long) qs.size());
				System.out.print("\ttime for all q: "
						+ (System.currentTimeMillis() - t0) / (double) 1000);

				System.out.print("\ttotal res: " + totalResults);

				// tree.setCosineTestEnabled(true);
				//
				// long t1 = System.currentTimeMillis();
				// for (CartesianPoint p : qs) {
				// List<CartesianPoint> res = index.thresholdSearch(p, th);
				// }
				// System.out.print("\t" + m.reset() / (long) qs.size());
				// System.out.print("\t" + (System.currentTimeMillis() - t1)
				// / (double) 1000);
				//
				System.out.println();
			}
		}
	}
}
