package searchStructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import coreConcepts.Metric;

/**
 * @author newrichard
 *
 *         a generalised hyperplane tree, also of course using a covering radius
 *         as that costs nothing extra
 */
public class HilbertHyperplaneTree<T> extends SearchIndex<T> {

	/**
	 * a GHTNode has two pivot nodes, lHead and rHead with each pivot is
	 * associated another GHTnode,lTree and rTree
	 * 
	 * special cases: there may be zero or one pivot nodes, in which case both
	 * lTree and rTree are null
	 * 
	 *
	 */
	private class HHTNode {

		double lCR, rCR;
		double l_r_dist;
		double median_proj_dist;
		T lHead, rHead;
		HHTNode lTree, rTree;

		@SuppressWarnings("synthetic-access")
		HHTNode(List<T> data) {
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
				this.l_r_dist = HilbertHyperplaneTree.this.metric.distance(
						this.lHead, this.rHead);

				List<T> lList = new ArrayList<>();
				List<T> rList = new ArrayList<>();
				@SuppressWarnings("unchecked")
				ObjectWithDistance<T>[] objs = new ObjectWithDistance[data
						.size()];

				int ptr = 0;
				for (T d : data) {
					double dLeft = HilbertHyperplaneTree.this.metric.distance(
							d, this.lHead);
					double dRight = HilbertHyperplaneTree.this.metric.distance(
							d, this.rHead);
					double projectionDistance = projectionDistance(
							this.l_r_dist, dLeft, dRight);
					ObjectWithDistance<T> owd = new ObjectWithDistance<>(d,
							projectionDistance);
					objs[ptr++] = owd;
				}
				Quicksort.placeMedian(objs);

				int halfWay = (data.size()) / 2;

				this.median_proj_dist = objs[halfWay].getDistance();
				for (int i = 0; i < data.size(); i++) {
					final T value = objs[i].getValue();
					if (i <= halfWay) {
						lList.add(value);
						this.lCR = Math.max(this.lCR,
								HilbertHyperplaneTree.this.metric.distance(
										value, this.lHead));
					} else {
						rList.add(value);
						this.rCR = Math.max(this.rCR,
								HilbertHyperplaneTree.this.metric.distance(
										value, this.rHead));
					}
				}

				this.lTree = new HHTNode(lList);
				this.rTree = new HHTNode(rList);
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
			double dLeft, dRight;
			if (this.lHead == null) {
				//
			} else if (this.rHead == null) {
				if (HilbertHyperplaneTree.this.metric.distance(query,
						this.lHead) <= t) {
					res.add(this.lHead);
				}
			} else {
				dLeft = HilbertHyperplaneTree.this.metric.distance(query,
						this.lHead);
				dRight = HilbertHyperplaneTree.this.metric.distance(query,
						this.rHead);

				if (dLeft <= t) {
					res.add(this.lHead);
				}
				if (dRight <= t) {
					res.add(this.rHead);
				}
				if (this.lTree != null) {
					double dProj = projectionDistance(this.l_r_dist, dLeft,
							dRight);
					boolean canExcludeOnProjection = Math.abs(dProj
							- this.median_proj_dist) > t;
					if (!excludeCR(dLeft, t, this.lCR)) {
						if (!(canExcludeOnProjection && dProj >= this.median_proj_dist)) {
							this.lTree.thresholdSearch(query, t, res);
						}
					}
					if (!excludeCR(dRight, t, this.rCR)) {
						if (!(canExcludeOnProjection && dProj < this.median_proj_dist)) {
							this.rTree.thresholdSearch(query, t, res);
						}
					}
				}

			}
		}

	}

	private HHTNode head;

	public HilbertHyperplaneTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.head = new HHTNode(data);
	}


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
		return ((pivotDistance / 2) - projection) > threshold;
	}

	private boolean excludeCR(double dPivot, double threshold,
			double coveringRadius) {
		return dPivot > coveringRadius + threshold;
	}

	private boolean excludeVor(double dLarge, double dSmall, double threshold) {
		return dLarge - dSmall > threshold * 2;
	}


	@Override
	public String getShortName() {
		return "hht";
	}

}
