package searchStructures;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

public class AltitudeTree<T> extends SearchIndex<T> {

	/**
	 * 
	 * special cases: there may be zero or one pivot nodes, in which case both
	 * lTree and rTree are null
	 * 
	 *
	 */
	private class AltTreeNode {

		double altCutoff;
		double l_r_dist;
		T lHead, rHead;
		AltTreeNode lTree, rTree;

		@SuppressWarnings("synthetic-access")
		AltTreeNode(List<T> data) {
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
				this.l_r_dist = AltitudeTree.this.metric.distance(this.lHead,
						this.rHead);

				ObjectWithDistance<T>[] toSort = new ObjectWithDistance[data
						.size()];
				int ptr = 0;
				for (T d : data) {
					double B = AltitudeTree.this.metric.distance(this.lHead, d);
					double C = AltitudeTree.this.metric.distance(this.rHead, d);
					double a = getMetricDistance(this.l_r_dist, B, C);
					ObjectWithDistance<T> owd = new ObjectWithDistance<>(d, a);
					toSort[ptr++] = owd;
				}
				Quicksort.placeMedian(toSort);
				List<T> lList = new ArrayList<>();
				List<T> rList = new ArrayList<>();
				this.altCutoff = toSort[toSort.length / 2].getDistance();
				for (int i = 0; i < toSort.length; i++) {
					if (i < toSort.length / 2) {
						lList.add(toSort[i].getValue());
					} else {
						rList.add(toSort[i].getValue());
					}
				}

				this.lTree = new AltTreeNode(lList);
				this.rTree = new AltTreeNode(rList);
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
		 * @param threshold
		 *            the threshold
		 * @param res
		 *            result list to be added to by side-effect
		 * @param dMin
		 *            the smallest distance that's been encountered above this
		 *            node of the tree
		 */
		public void thresholdSearch(T query, double threshold, List<T> res) {
			double dLeft, dRight;
			if (this.lHead == null) {
				//
			} else if (this.rHead == null) {
				if (AltitudeTree.this.metric.distance(query, this.lHead) <= threshold) {
					res.add(this.lHead);
				}
			} else {
				dLeft = AltitudeTree.this.metric.distance(query, this.lHead);
				dRight = AltitudeTree.this.metric.distance(query, this.rHead);

				if (dLeft <= threshold) {
					res.add(this.lHead);
				}
				if (dRight <= threshold) {
					res.add(this.rHead);
				}
				if (this.lTree != null) {
					double alt = getMetricDistance(this.l_r_dist, dLeft, dRight);

					if (alt - this.altCutoff > threshold) {
						this.rTree.thresholdSearch(query, threshold, res);
					} else if (this.altCutoff - alt > threshold) {
						this.lTree.thresholdSearch(query, threshold, res);
					} else {
						this.lTree.thresholdSearch(query, threshold, res);
						this.rTree.thresholdSearch(query, threshold, res);
					}
				}
			}
		}

	}

	private AltTreeNode head;

	public AltitudeTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.head = new AltTreeNode(data);
	}

	/**
	 * @param ab
	 * @param ac
	 * @param bc
	 * @return the altitude, using the harmonic mean theorem
	 */
	private static double getAltitude(double ab, double ac, double bc) {
		double cosCAB = (ab * ab + ac * ac - bc * bc) / (2 * ab * ac);
		double lhs = ac * cosCAB;
		return Math.sqrt(ac * ac - lhs * lhs);
	}

	private static double getMetricDistance(double ab, double aq, double bq) {
		return getAltitude(ab, aq, bq);
	}

	private static double getDistFromPivotCentre(double ab, double ac, double bc) {
		double cosCAB = (ab * ab + ac * ac - bc * bc) / (2 * ab * ac);
		double lhs = ac * cosCAB;
		double altSq = ac * ac - lhs * lhs;
		double offset = ab / 2 - lhs;
		return Math.sqrt(offset * offset + altSq);
	}

	@Override
	public List<T> thresholdSearch(T query, double threshold) {
		List<T> res = new ArrayList<>();
		this.head.thresholdSearch(query, threshold, res);
		return res;
	}

	@Override
	public String getShortName() {
		return "alt";
	}

}
