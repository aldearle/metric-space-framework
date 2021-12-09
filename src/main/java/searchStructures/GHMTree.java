package searchStructures;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

/**
 * @author newrichard
 *
 *         a monotonous generalised hyperplane tree
 */
public class GHMTree<T> extends SearchIndex<T> {

	private class HeadNode {
		T lHead, rHead;
		TreeNode lTree, rTree;
		double lCR, rCR;
		double l_r_dist;

		HeadNode(List<T> data) {
			// assume more than two nodes!
			List<T> pivs = chooseTwoPivots(data);
			this.lHead = pivs.get(0);
			this.rHead = pivs.get(1);
			this.l_r_dist = GHMTree.this.metric
					.distance(this.lHead, this.rHead);

			List<T> lList = new ArrayList<>();
			List<T> rList = new ArrayList<>();
			for (T d : data) {
				double d1 = GHMTree.this.metric.distance(d, this.lHead);
				double d2 = GHMTree.this.metric.distance(d, this.rHead);
				if (d1 < d2) {
					this.lCR = Math.max(this.lCR, d1);
					lList.add(d);
				} else {
					this.rCR = Math.max(this.rCR, d2);
					rList.add(d);
				}
			}

			this.lTree = new TreeNode(this.lHead, lList);
			this.rTree = new TreeNode(this.rHead, rList);
		}

		@SuppressWarnings("synthetic-access")
		public void thresholdSearch(T query, double t, List<T> res) {
			double dLeft, dRight;
			if (this.lHead == null) {
				//
			} else if (this.rHead == null) {
				if (GHMTree.this.metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
			} else {
				dLeft = GHMTree.this.metric.distance(query, this.lHead);
				dRight = GHMTree.this.metric.distance(query, this.rHead);
				boolean lClosest = dLeft < dRight;

				if (dLeft <= t) {
					res.add(this.lHead);
				}
				if (dRight <= t) {
					res.add(this.rHead);
				}
				if (this.lTree != null) {
					if (!excludeCR(dLeft, t, this.lCR)
							&& !(!lClosest && excludeVor(dLeft, dRight, t))
							&& !(!lClosest && excludeHilbert(this.l_r_dist,
									dRight, dLeft, t))) {

						this.lTree.thresholdSearch(query, t, dLeft, res);
					}
					if (!excludeCR(dRight, t, this.rCR)
							&& !(lClosest && excludeVor(dRight, dLeft, t))
							&& !(lClosest && excludeHilbert(this.l_r_dist,
									dLeft, dRight, t))) {

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

		TreeNode(T upperNode, List<T> data) {
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
				T piv = chooseFurthestPivot(upperNode, data);
				this.lHead = upperNode;
				this.rHead = piv;
				this.l_r_dist = GHMTree.this.metric.distance(this.lHead,
						this.rHead);

				List<T> lList = new ArrayList<>();
				List<T> rList = new ArrayList<>();
				for (T d : data) {
					double d1 = GHMTree.this.metric.distance(d, this.lHead);
					double d2 = GHMTree.this.metric.distance(d, this.rHead);
					if (d1 < d2) {
						lList.add(d);
						this.lCR = Math.max(this.lCR, d1);
					} else {
						rList.add(d);
						this.rCR = Math.max(this.rCR, d2);
					}
				}

				this.lTree = new TreeNode(this.lHead, lList);
				this.rTree = new TreeNode(this.rHead, rList);
			}

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
				if (GHMTree.this.metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
			} else if (this.rTree == null) {
				// now needed for the two-data leaf nodes
				if (GHMTree.this.metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
				if (GHMTree.this.metric.distance(query, this.rHead) <= t) {
					res.add(this.rHead);
				}
			} else {
				dLeft = upperDist;
				dRight = GHMTree.this.metric.distance(query, this.rHead);
				boolean lClosest = dLeft < dRight;

				if (dRight <= t) {
					res.add(this.rHead);
				}
				if (this.lTree != null) {
					if (!excludeCR(dLeft, t, this.lCR)
							&& !(!lClosest && excludeVor(dLeft, dRight, t))
							&& !(!lClosest && excludeHilbert(this.l_r_dist,
									dRight, dLeft, t))) {

						this.lTree.thresholdSearch(query, t, dLeft, res);
					}
					if (!excludeCR(dRight, t, this.rCR)
							&& !(lClosest && excludeVor(dRight, dLeft, t))
							&& !(lClosest && excludeHilbert(this.l_r_dist,
									dLeft, dRight, t))) {

						this.rTree.thresholdSearch(query, t, dRight, res);
					}
				}

			}
		}

	}

	private GHMTree<T>.HeadNode head;

	private boolean crExclusionEnabled;

	private boolean vorExclusionEnabled;

	private boolean cosExclusionEnabled;

	public GHMTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.head = new HeadNode(data);

		this.cosExclusionEnabled = false;
		this.crExclusionEnabled = false;
		this.vorExclusionEnabled = false;
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

	private boolean excludeHilbert(double P, double C, double B,
			double threshold) {
		if (this.cosExclusionEnabled) {
			return excludeCos(P, C, B, threshold);
			// return (B * B - C * C) / (2 * P) > threshold;
		} else {
			return false;
		}
	}

	private boolean excludeCR(double dPivot, double threshold,
			double coveringRadius) {
		return GHMTree.this.crExclusionEnabled
				&& dPivot > coveringRadius + threshold;
	}

	private boolean excludeVor(double dLarge, double dSmall, double threshold) {
		return GHMTree.this.vorExclusionEnabled
				&& dLarge - dSmall > threshold * 2;
	}

	@Override
	public String getShortName() {
		return "ghmt";
	}

}
