package searchStructures.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import searchStructures.SearchIndex;
import searchStructures.SemiSorter;
import coreConcepts.Metric;

/**
 * @author newrichard
 *
 *         a balanced monotonous generalised hyperplane tree
 */
public class LeanestTree<T> extends SearchIndex<T> {

	public int maxDepth;
	private boolean hilbertMetric;
	Map<Integer, T> pivotsByDepth;
	Map<T, Double> memoDistances;

	private class HeadNode {
		T lHead, rHead;
		TreeNode lTree, rTree;
		double lCR, rCR;
		double l_r_dist;
		double offset;

		HeadNode(List<T> data) {
			// assume more than two nodes!
			List<T> pivs = chooseTwoPivots(data);
			this.lHead = pivs.get(0);
			this.rHead = pivs.get(1);
			this.l_r_dist = LeanestTree.this.metric.distance(this.lHead,
					this.rHead);

			final T left = HeadNode.this.lHead;
			final T right = HeadNode.this.rHead;

			SemiSorter<T> ss = getSemiSorter(data, left, right, this.l_r_dist);

			this.offset = ss.getPivotDistance();
			List<T> lList = ss.getLeft();
			List<T> rList = ss.getRight();
			this.lCR = listMaxDist(this.lHead, lList);
			this.rCR = listMaxDist(this.rHead, rList);

			this.lTree = new TreeNode(this.lHead, lList, 1);
			this.rTree = new TreeNode(this.rHead, rList, 1);
		}

		@SuppressWarnings("synthetic-access")
		public void thresholdSearch(T query, double t, List<T> res) {
			double dLeft, dRight;
			if (this.lHead == null) {
				//
			} else if (this.rHead == null) {
				if (LeanestTree.this.metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
			} else {
				dLeft = LeanestTree.this.metric.distance(query, this.lHead);
				dRight = LeanestTree.this.metric.distance(query, this.rHead);

				if (dLeft <= t) {
					res.add(this.lHead);
				}
				if (dRight <= t) {
					res.add(this.rHead);
				}
				if (this.lTree != null) {
					if (!excludeCR(dLeft, t, this.lCR)
							&& !(excludeVor(dLeft, dRight, t, this.offset))
							&& !(excludeCosNew(this.l_r_dist, dRight, dLeft, t,
									this.l_r_dist - this.offset))) {

						this.lTree.thresholdSearch(query, t, dLeft, res);
					}
					if (!excludeCR(dRight, t, this.rCR)
							&& !(excludeVor(dRight, dLeft, t, -this.offset))
							&& !(excludeCosNew(this.l_r_dist, dLeft, dRight, t,
									this.offset))) {

						this.rTree.thresholdSearch(query, t, dRight, res);
					}
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

		double lCR, rCR;
		double l_r_dist;
		T lHead, rHead;
		TreeNode lTree, rTree;
		double offset;

		TreeNode(T upperNode, List<T> data, int depth) {
			LeanestTree.this.maxDepth = Math.max(LeanestTree.this.maxDepth,
					depth);
			this.lCR = 0;
			this.rCR = 0;
			if (data.size() == 0) {
				//
			} else if (data.size() == 1) {
				this.lHead = data.get(0);
			} else if (data.size() == 2) {
				this.lHead = data.get(0);
				this.rHead = data.get(1);
			} else {
				// more than two nodes in data so requires recursion
				T piv = getLeanPivot(upperNode, data, depth);

				this.lHead = upperNode;
				this.rHead = piv;
				this.l_r_dist = LeanestTree.this.metric.distance(this.lHead,
						this.rHead);

				SemiSorter<T> ss = getSemiSorter(data, this.lHead, this.rHead,
						this.l_r_dist);

				this.offset = ss.getPivotDistance();
				List<T> lList = ss.getLeft();
				List<T> rList = ss.getRight();
				this.lCR = listMaxDist(this.lHead, lList);
				this.rCR = listMaxDist(this.rHead, rList);

				this.lTree = new TreeNode(this.lHead, lList, depth + 1);
				this.rTree = new TreeNode(this.rHead, rList, depth + 1);

				//
				// List<T> lList = new ArrayList<>();
				// List<T> rList = new ArrayList<>();
				// for (T d : data) {
				// double d1 = BGHMTree.this.metric.distance(d, this.lHead);
				// double d2 = BGHMTree.this.metric.distance(d, this.rHead);
				// if (d1 < d2) {
				// lList.add(d);
				// this.lCR = Math.max(this.lCR, d1);
				// } else {
				// rList.add(d);
				// this.rCR = Math.max(this.rCR, d2);
				// }
				// }
				//
				// this.lTree = new TreeNode(this.lHead, lList, depth + 1);
				// this.rTree = new TreeNode(this.rHead, rList, depth + 1);
			}

		}

		protected T getLeanPivot(T upperNode, List<T> data, int depth) {
			if (!LeanestTree.this.pivotsByDepth.keySet().contains(depth)) {
				// if (pivotsByDepth.size() == 0) {
				// pivotsByDepth.put(depth, data.remove(0));
				// } else if (pivotsByDepth.size() == 1) {
				// final T piv = chooseFurthestPivot(pivotsByDepth.get(1),
				// data);
				// data.remove(piv);
				// pivotsByDepth.put(depth, piv);
				// }
				// final T piv = chooseBestPivot(pivotsByDepth.values(), data);
				// data.remove(piv);
				// LeanestTree.this.pivotsByDepth.put(depth, piv);
				pivotsByDepth.put(depth, chooseFurthestPivot(upperNode, data));
			}
			return LeanestTree.this.pivotsByDepth.get(depth);
		}

		public int cardinality() {
			if (this.lHead == null) {
				return 0;
			} else if (this.rHead == null) {
				return 1;
			} else if (this.lTree == null) {
				return 2;
			} else {
				return 2 + this.lTree.cardinality() + this.rTree.cardinality();
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
				List<T> res) {
			double dLeft, dRight;
			if (this.lHead == null) {
				//
			} else if (this.rHead == null) {
				if (LeanestTree.this.metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
			} else if (this.rTree == null) {
				// now needed for the two-data leaf nodes
				if (LeanestTree.this.metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
				if (LeanestTree.this.metric.distance(query, this.rHead) <= t) {
					res.add(this.rHead);
				}
			} else {
				dLeft = upperDist;
				dRight = memoDistance(query, this.rHead);

				if (dRight <= t) {
					if (res.contains(this.rHead)) {
						res.add(this.rHead);
					}
				}
				if (this.lTree != null) {
					if (!excludeCR(dLeft, t, this.lCR)
							&& !(excludeVor(dLeft, dRight, t, this.offset))
							&& !(excludeCosNew(this.l_r_dist, dRight, dLeft, t,
									this.l_r_dist - this.offset))) {

						this.lTree.thresholdSearch(query, t, dLeft, res);
					}
					if (!excludeCR(dRight, t, this.rCR)
							&& !(excludeVor(dRight, dLeft, t, -this.offset))
							&& !(excludeCosNew(this.l_r_dist, dLeft, dRight, t,
									this.offset))) {

						this.rTree.thresholdSearch(query, t, dRight, res);
					}
				}

			}
		}

	}

	private LeanestTree<T>.HeadNode head;

	private boolean crExclusionEnabled;

	private boolean vorExclusionEnabled;

	private boolean cosExclusionEnabled;

	public LeanestTree(List<T> data, Metric<T> metric, boolean hilbertMetric) {
		super(data, metric);

		this.hilbertMetric = hilbertMetric;
		this.maxDepth = 0;
		this.pivotsByDepth = new HashMap<>();
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

	@SuppressWarnings("boxing")
	private double memoDistance(T q, T p) {
		if (!this.memoDistances.containsKey(p)) {
			this.memoDistances.put(p, this.metric.distance(q, p));
		}
		return this.memoDistances.get(p);
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		this.memoDistances = new HashMap<>();
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

	private boolean excludeCosNew(double pivotDistance, double closerDistance,
			double furtherDistance, double threshold, double offset) {
		double projD = projectionDistance(pivotDistance, closerDistance,
				furtherDistance);
		final boolean res = offset - projD > threshold;
		return this.cosExclusionEnabled && res;
	}

	private boolean excludeCR(double dPivot, double threshold,
			double coveringRadius) {
		return LeanestTree.this.crExclusionEnabled
				&& dPivot > coveringRadius + threshold;
	}

	private boolean excludeVor(double dLarge, double dSmall, double threshold,
			double offset) {
		return LeanestTree.this.vorExclusionEnabled
				&& dLarge - dSmall > threshold * 2 + offset;
	}

	@Override
	public String getShortName() {
		return "ghmt";
	}

	protected SemiSorter<T> getSemiSorter(List<T> data, final T left,
			final T right, final double pivot_distance) {
		SemiSorter<T> ss;
		if (this.hilbertMetric) {
			ss = new SemiSorter<T>(data) {
				@Override
				public double measure(T d) {
					double d1 = LeanestTree.this.metric.distance(d, left);
					double d2 = LeanestTree.this.metric.distance(d, right);
					return projectionDistance(pivot_distance, d1, d2);
				}
			};
		} else {
			ss = new SemiSorter<T>(data) {
				@Override
				public double measure(T d) {
					double d1 = LeanestTree.this.metric.distance(d, left);
					double d2 = LeanestTree.this.metric.distance(d, right);
					return d1 - d2;
				}
			};
		}
		return ss;
	}
}
