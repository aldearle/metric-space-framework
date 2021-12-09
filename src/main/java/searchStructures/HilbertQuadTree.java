package searchStructures;

import coreConcepts.Metric;

import java.util.ArrayList;
import java.util.List;

/**
 * @author newrichard
 *
 * @param <T>
 * 
 *            metric index that works only for Hilbert spaces, relying upon the
 *            tighter geometry
 */
public class HilbertQuadTree<T> extends SearchIndex<T> {

	private class QuadTreeNode {

		List<QuadTreeNode> subTrees;
		double[] crs;
		private T lHead;
		private T rHead;
		private double l_r_dist;
		private double median_proj_dist;
		private double left_median_altitude;
		private double right_median_altitude;
		double lCR;
		double rCR;

		@SuppressWarnings("unchecked")
		QuadTreeNode(List<T> data) {
			if (data.size() == 0) {
				//
			} else if (data.size() == 1) {
				this.lHead = data.get(0);
			} else if (data.size() == 2) {
				this.lHead = data.get(0);
				this.rHead = data.get(1);
			} else {
				// more than two nodes in data so requires recursion
				this.subTrees = new ArrayList<>();

				List<T> pivs = chooseTwoPivots(data);
				this.lHead = pivs.get(0);
				this.rHead = pivs.get(1);
				this.l_r_dist = HilbertQuadTree.this.metric.distance(
						this.lHead, this.rHead);

				List<T> lList = new ArrayList<>();
				List<T> rList = new ArrayList<>();

				divideByProjection(data, lList, rList);

				if (lList.size() > 2) {
					List<List<T>> leftDiv = divideByAltitude(lList, true);
					this.subTrees.add(new QuadTreeNode(leftDiv.get(0)));
					this.subTrees.add(new QuadTreeNode(leftDiv.get(1)));
				} else {
					this.subTrees.add(new QuadTreeNode(lList));
					this.subTrees.add(new QuadTreeNode(new ArrayList<T>()));
				}

				if (rList.size() > 2) {
					List<List<T>> rightDiv = divideByAltitude(rList, false);
					this.subTrees.add(new QuadTreeNode(rightDiv.get(0)));
					this.subTrees.add(new QuadTreeNode(rightDiv.get(1)));
				} else {
					this.subTrees.add(new QuadTreeNode(rList));
					this.subTrees.add(new QuadTreeNode(new ArrayList<T>()));
				}
			}

		}

		@SuppressWarnings("synthetic-access")
		private void thresholdSearch(T query, double t, List<T> res) {
			double dLeft, dRight;
			if (this.lHead == null) {
				//
			} else if (this.rHead == null) {
				if (metric.distance(query, this.lHead) <= t) {
					res.add(this.lHead);
				}
			} else {
				dLeft = metric.distance(query, this.lHead);
				dRight = metric.distance(query, this.rHead);

				if (dLeft <= t) {
					res.add(this.lHead);
				}
				if (dRight <= t) {
					res.add(this.rHead);
				}
				if (this.subTrees != null) {
					double dProj = projectionDistance(this.l_r_dist, dLeft,
							dRight);
					boolean canExcludeOnProjection = Math.abs(dProj
							- this.median_proj_dist) > t;

					boolean[] mustSearch = new boolean[4];
					for (int i = 0; i < 4; i++) {
						mustSearch[i] = true;
					}

					if (excludeCR(dLeft, t, lCR)
							|| (canExcludeOnProjection && dProj >= this.median_proj_dist)) {
						mustSearch[0] = false;
						mustSearch[1] = false;
					}

					if (excludeCR(dRight, t, rCR)
							|| (canExcludeOnProjection && dProj < this.median_proj_dist)) {
						mustSearch[2] = false;
						mustSearch[3] = false;

					}

					double altitude = getAltitude(this.l_r_dist, dLeft, dRight);
					if (Math.abs(altitude - this.left_median_altitude) > t) {
						if (altitude < this.left_median_altitude) {
							mustSearch[0] = false;
						} else {
							mustSearch[1] = false;
						}
					}
					if (Math.abs(altitude - this.right_median_altitude) > t) {
						if (altitude < this.right_median_altitude) {
							mustSearch[2] = false;
						} else {
							mustSearch[3] = false;
						}
					}

					for (int l = 0; l < 4; l++) {
						if (mustSearch[l]) {
							this.subTrees.get(l).thresholdSearch(query, t, res);
						}
					}

				}

			}

		}

		@SuppressWarnings("synthetic-access")
		private List<List<T>> divideByAltitude(List<T> data, boolean left) {
			List<List<T>> res = new ArrayList<>();
			List<T> topList = new ArrayList<>();
			List<T> bottomList = new ArrayList<>();

			double ab = this.l_r_dist;

			@SuppressWarnings("unchecked")
			ObjectWithDistance<T>[] objs = new ObjectWithDistance[data.size()];
			int ptr = 0;
			for (T dat : data) {
				double ac = metric.distance(dat, this.lHead);
				double bc = metric.distance(dat, this.rHead);
				double alt = getAltitude(ab, ac, bc);
				objs[ptr++] = new ObjectWithDistance<T>(dat, alt);
			}

			int halfway = data.size() / 2;
			Quicksort.placeMedian(objs);
			if (left) {
				this.left_median_altitude = objs[halfway].getDistance();
			} else {
				this.right_median_altitude = objs[halfway].getDistance();
			}

			for (int i = 0; i < data.size(); i++) {
				if (i <= halfway) {
					bottomList.add(objs[i].getValue());
				} else {
					topList.add(objs[i].getValue());
				}
			}

			res.add(topList);
			res.add(bottomList);
			return res;
		}

		protected void divideByProjection(List<T> data, List<T> lList,
				List<T> rList) {
			@SuppressWarnings("unchecked")
			ObjectWithDistance<T>[] objs = new ObjectWithDistance[data.size()];

			int ptr = 0;
			for (T d : data) {
				double dLeft = metric.distance(d, this.lHead);
				double dRight = metric.distance(d, this.rHead);
				double projectionDistance = projectionDistance(this.l_r_dist,
						dLeft, dRight);
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
							metric.distance(value, this.lHead));
				} else {
					rList.add(value);
					this.rCR = Math.max(this.rCR,
							metric.distance(value, this.rHead));
				}
			}
		}

	}

	private QuadTreeNode head;

	public HilbertQuadTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.head = new QuadTreeNode(data);
	}

	private boolean excludeCR(double dPivot, double threshold,
			double coveringRadius) {
		return dPivot > coveringRadius + threshold;
	}

	/**
	 * @param ab
	 * @param ac
	 * @param bc
	 * @return the altitude, ie distance above the line ab
	 */
	private static double getAltitude(double ab, double ac, double bc) {
		double cosCAB = (ab * ab + ac * ac - bc * bc) / (2 * ab * ac);
		double lhs = ac * cosCAB;
		return Math.sqrt(ac * ac - lhs * lhs);
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		this.head.thresholdSearch(query, t, res);
		return res;
	}

	@Override
	public String getShortName() {
		return "hqt";
	}

}
