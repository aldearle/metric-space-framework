package searchStructures;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;


/**
 * @author newrichard
 *
 *         a generalised hyperplane tree, also of course using a covering radius
 *         as that costs nothing extra
 */
public class GHTree<T> extends SearchIndex<T> {

	/**
	 * a GHTNode has two pivot nodes, lHead and rHead with each pivot is
	 * associated another GHTnode,lTree and rTree
	 * 
	 * special cases: there may be zero or one pivot nodes, in which case both
	 * lTree and rTree are null
	 * 
	 *
	 */
	private class GHTNode {

		double lCR, rCR;
		double l_r_dist;
		T lHead, rHead;
		GHTNode lTree, rTree;

		GHTNode(List<T> data) {
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
				// more than three nodes in data so requires recursion
				@SuppressWarnings("synthetic-access")
				List<T> pivs = chooseTwoPivots(data);
				this.lHead = pivs.get(0);
				this.rHead = pivs.get(1);
				this.l_r_dist = GHTree.this.metric.distance(this.lHead,
						this.rHead);

				List<T> lList = new ArrayList<>();
				List<T> rList = new ArrayList<>();
				for (T d : data) {
					double d1 = GHTree.this.metric.distance(d, this.lHead);
					double d2 = GHTree.this.metric.distance(d, this.rHead);
					if (d1 < d2) {
						lList.add(d);
						this.lCR = Math.max(this.lCR, d1);
					} else {
						rList.add(d);
						this.rCR = Math.max(this.rCR, d2);
					}
				}

				this.lTree = new GHTNode(lList);
				this.rTree = new GHTNode(rList);
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
		public void thresholdSearch(T query, double t, List<T> res) {
			// dMin is rubbish here, it doesn't work!
			double dLeft, dRight;
			if (this.lHead == null) {
				//
			} else if (this.rHead == null) {
				if (GHTree.this.metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
			} else {
				dLeft = GHTree.this.metric.distance(query, this.lHead);
				dRight = GHTree.this.metric.distance(query, this.rHead);
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
							&& !(!lClosest && excludeCos(this.l_r_dist, dRight,
									dLeft, t))) {

						this.lTree.thresholdSearch(query, t, res);
					}
					if (!excludeCR(dRight, t, this.rCR)
							&& !(lClosest && excludeVor(dRight, dLeft, t))
							&& !(lClosest && excludeCos(this.l_r_dist, dLeft,
									dRight, t))) {

						this.rTree.thresholdSearch(query, t, res);
					}
				}

			}
		}

	}

	private GHTNode head;

	private boolean crExclusionEnabled;

	private boolean vorExclusionEnabled;

	private boolean cosExclusionEnabled;

	public GHTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.head = new GHTNode(data);

		this.cosExclusionEnabled = false;
		this.crExclusionEnabled = true;
		this.vorExclusionEnabled = false;
	}

	public GHTree(List<T> data, Metric<T> metric, boolean fourPoint) {
		super(data, metric);
		this.head = new GHTNode(data);

		this.crExclusionEnabled = true;
		if (fourPoint) {
			this.cosExclusionEnabled = true;
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
	@SuppressWarnings("synthetic-access")
	private boolean excludeCos(double pivotDistance, double closerDistance,
			double furtherDistance, double threshold) {
		double cosTheta = (pivotDistance * pivotDistance + closerDistance
				* closerDistance - furtherDistance * furtherDistance)
				/ (2 * pivotDistance * closerDistance);
		double projection = closerDistance * cosTheta;
		return this.cosExclusionEnabled
				&& (((pivotDistance / 2) - projection) > threshold);
	}

	private boolean excludeCR(double dPivot, double threshold,
			double coveringRadius) {
		return GHTree.this.crExclusionEnabled
				&& dPivot > coveringRadius + threshold;
	}

	private boolean excludeVor(double dLarge, double dSmall, double threshold) {
		return GHTree.this.vorExclusionEnabled
				&& dLarge - dSmall > threshold * 2;
	}

	@Override
	public String getShortName() {
		return "ght";
	}

}
