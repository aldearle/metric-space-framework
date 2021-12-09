package generic_partition_tree;

import generic_partition_tree.MonotonicTree.Strategy;
import generic_partition_tree.MonotonicTree.TreeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import searchStructures.ObjectWithDistance;
import searchStructures.SearchIndex;
import coreConcepts.Metric;

public abstract class GenericPartitionTree<T> extends SearchIndex<T> {

	public class CoverRadii {
		double lCr, rCr;
		double p2ToLmin, p1ToRmin;

		CoverRadii() {
			this.rCr = 0;
			this.lCr = 0;
			this.p2ToLmin = Double.MAX_VALUE;
			this.p1ToRmin = Double.MAX_VALUE;
		}
	}

	public class DataNode {
		int from, to;

		DataNode(int from, int to) {
			this.from = from;
			this.to = to;
		}

		void search(T query, double threshold, List<T> result) {
			for (int i = this.from; i <= this.to; i++) {
				if (GenericPartitionTree.this.metric.distance(query,
						GenericPartitionTree.this.data[i].getValue()) <= threshold) {
					result.add(GenericPartitionTree.this.data[i].getValue());
				}
			}
		}
	}

	protected class ReferencePoints {
		int p1;
		int p2;
		double p1p2dist;
		Map<T, Double> p1memos;
		int inc;

		ReferencePoints(int newP1) {
			this.p1 = newP1;
			this.p2 = -1;
			this.p1p2dist = -1;
			this.inc = -1;
			this.p1memos = new HashMap<>();
		}
	}

	protected class TreeNode {
		ReferencePoints refPoints;
		int depth;
		double splitValue;
		CoverRadii cr;
		TreeNode left, right;
		DataNode leftD, rightD;

		TreeNode(int depth, int from, int to, int newP1) {
			/*
			 * invariants
			 */
			assert newP1 < from : "TreeNode: newP1 in range";
			assert to < GenericPartitionTree.this.data.length : "TreeNode: to past end of data";
			this.cr = new CoverRadii();

			// from/to are inclusive indexes into data
			// newP1 is outside the from/to boundaries
			this.depth = depth;
			// whatever pivots are chosen, they will be put in the first 0,1,or
			// 2 locations depending on whether they are external or not

			this.refPoints = getRefPoints(from, to, newP1);
			from += this.refPoints.inc;
			// assign the new ref points remove their locations from use

			// create memo for all distances between p2 and enclosed data
			T p2val = GenericPartitionTree.this.data[this.refPoints.p2]
					.getValue();
			Map<T, Double> p2dists = new HashMap<>();
			for (int i = from; i <= to; i++) {
				final T value = GenericPartitionTree.this.data[i].getValue();
				final double d2 = GenericPartitionTree.this.metric.distance(
						value, p2val);
				p2dists.put(value, d2);
			}

			/*
			 * so... pivots have been set up and clones removed
			 * 
			 * if there are two or less data left, put them in data nodes
			 * 
			 * otherwise split; if only one left put in data, otherwise create
			 * new node
			 */
			if (to - from > 0) {// two or more data left
				assert this.refPoints.p1memos.keySet().contains(
						GenericPartitionTree.this.data[from].getValue()) : "from not in p1dists";

				assert to > from : "to is not greater than from: " + from
						+ " (from);" + to + " (to);";

				int firstOnRight = assignSplitValues(from, to,
						this.refPoints.p1memos, p2dists,
						this.refPoints.p1p2dist);

				this.splitValue = GenericPartitionTree.this.data[firstOnRight]
						.getDistance();

				// set lCr, lToRmin;
				for (int i = from; i < firstOnRight; i++) {
					final Double d1 = this.refPoints.p1memos
							.get(GenericPartitionTree.this.data[i].getValue());
					final Double d2 = p2dists
							.get(GenericPartitionTree.this.data[i].getValue());
					this.cr.lCr = Math.max(this.cr.lCr, d1);
					this.cr.p2ToLmin = Math.min(this.cr.p2ToLmin, d2);
				}
				// set lCr, lToRmin;
				for (int i = firstOnRight; i <= to; i++) {
					final Double d1 = this.refPoints.p1memos
							.get(GenericPartitionTree.this.data[i].getValue());
					final Double d2 = p2dists
							.get(GenericPartitionTree.this.data[i].getValue());
					this.cr.rCr = Math.max(this.cr.rCr, d2);
					this.cr.p1ToRmin = Math.min(this.cr.p1ToRmin, d1);
				}

				assert firstOnRight >= from;
				assert firstOnRight <= to;
				if ((firstOnRight - 1) - from > 1) {
					this.left = new TreeNode(depth + 1, from, firstOnRight - 1,
							this.refPoints.p1);
				} else {
					this.leftD = new DataNode(from, firstOnRight - 1);
				}
				if (to - firstOnRight > 1) {
					this.right = new TreeNode(depth + 1, firstOnRight, to,
							this.refPoints.p2);
				} else {
					this.rightD = new DataNode(firstOnRight, to);
				}
			} else {
				this.leftD = new DataNode(to, to);
			}

		}

		public void search(T query, double threshold, List<T> result,
				double p1dist) {
			if (MonotonicTree.treeType == TreeType.normal) {
				p1dist = GenericPartitionTree.this.metric.distance(
						GenericPartitionTree.this.data[this.refPoints.p1]
								.getValue(), query);
			}
			final double p2dist = GenericPartitionTree.this.metric.distance(
					GenericPartitionTree.this.data[this.refPoints.p2]
							.getValue(), query);
			if (p2dist <= threshold) {
				result.add(GenericPartitionTree.this.data[this.refPoints.p2]
						.getValue());
			}
			if (this.depth == 0 || MonotonicTree.treeType == TreeType.normal) {
				if (GenericPartitionTree.this.metric.distance(
						GenericPartitionTree.this.data[this.refPoints.p1]
								.getValue(), query) <= threshold) {
					result.add(GenericPartitionTree.this.data[this.refPoints.p1]
							.getValue());
				}
			}
			if (this.left != null
					&& !canExcludeLeft(threshold, cr, p1dist, p2dist,
							this.refPoints.p1p2dist, this.splitValue)) {
				this.left.search(query, threshold, result, p1dist);
			} else if (this.leftD != null) {
				this.leftD.search(query, threshold, result);
			}
			if (this.right != null
					&& !canExcludeRight(threshold, cr, p1dist, p2dist,
							this.refPoints.p1p2dist, this.splitValue)) {
				this.right.search(query, threshold, result, p2dist);
			} else if (this.rightD != null) {
				this.rightD.search(query, threshold, result);
			}
		}

		protected int cardinality() {
			int c = 1;
			if (this.depth == 0) {
				c++;
			}
			if (this.left != null) {
				c += this.left.cardinality();
			} else if (this.leftD != null) {
				c += (this.leftD.to - this.leftD.from) + 1;
			}
			if (this.right != null) {
				c += this.right.cardinality();
			} else if (this.rightD != null) {
				c += (this.rightD.to - this.rightD.from) + 1;
			}
			return c;
		}

		int maxDepth() {
			int d = this.depth;
			if (this.left != null) {
				d = this.left.maxDepth();
			}
			if (this.right != null) {
				d = Math.max(d, this.right.maxDepth());
			}
			return d;
		}
	}

	@SuppressWarnings("hiding")
	ObjectWithDistance<T>[] data;
	TreeNode root;
	int totalClones;

	@SuppressWarnings("unchecked")
	protected GenericPartitionTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.data = new ObjectWithDistance[data.size()];
		int ptr = 0;
		for (T d : data) {
			this.data[ptr++] = new ObjectWithDistance<>(d, 0);
		}
		if (MonotonicTree.treeType == TreeType.monotonic) {
			this.root = new TreeNode(0, 1, this.data.length - 1, 0);
		} else {
			this.root = new TreeNode(0, 0, this.data.length - 1, -1);
		}
	}

	public abstract int assignSplitValues(int from, int to,
			Map<T, Double> p1dists, Map<T, Double> p2dists, double pDist);

	@Override
	public String getShortName() {
		return "PT";
	}

	public void swap(int a, int b) {
		ObjectWithDistance<T> temp = this.data[a];
		this.data[a] = this.data[b];
		this.data[b] = temp;
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> result = new ArrayList<>();
		double p1dist = this.metric.distance(query, this.data[0].getValue());
		this.root.search(query, t, result, p1dist);
		return result;
	}

	protected abstract boolean canExcludeLeft(double threshold, CoverRadii cr,
			double p1dist, final double p2dist, double pDist, double splitValue);

	protected abstract boolean canExcludeRight(double threshold, CoverRadii cr,
			double p1dist, final double p2dist, double pDist, double splitValue);

	protected abstract ReferencePoints getRefPoints(int from, int to, int newP1);

	protected boolean canExcludeLeftCr(double lDist, double rDist,
			double threshold, CoverRadii cr) {
		return cr.lCr < lDist - threshold || rDist < cr.p2ToLmin - threshold;
	}

	protected boolean canExcludeRightCr(double lDist, double rDist,
			double threshold, CoverRadii cr) {
		return cr.rCr < rDist - threshold || lDist < cr.p1ToRmin - threshold;
	}
}
