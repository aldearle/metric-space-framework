package searchStructures.experimental;

import java.util.ArrayList;
import java.util.List;

import n_point_surrogate.SimplexND;
import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import searchStructures.SearchIndex;
import testloads.TestContext;
import util.Range;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

public class HybridVPT<T> extends SearchIndex<T> {

	TreeNode root;

	private class TreeNode {
		boolean isHeadNode;
		T pivot;
		T parent;
		TreeNode left, right;
		double mu;
		SimplexND<T> fourPoint;
		double maxLeftXOffset;
		double minRightXOffset;
		double maxLeftYOffset;
		double minRightYOffset;

		TreeNode(List<T> data) {
			this.isHeadNode = true;
			init(data);
		}

		TreeNode(List<T> data, T parent) {
			this.isHeadNode = false;
			if (data.size() > 1) {
				this.parent = parent;
				init(data);
			} else {
				this.pivot = data.get(0);
			}
		}

		private void init(List<T> data) {
			int pivPos = -1;
			if (this.isHeadNode || true) {
				pivPos = rand.nextInt(data.size());
			} else {
				double maxDist = 0;
				for (int i : Range.range(0, data.size())) {
					double d = metric.distance(this.parent, data.get(i));
					if (d >= maxDist) {
						maxDist = d;
						pivPos = i;
					}
				}
			}
			this.pivot = data.get(pivPos);
			/*
			 * nb not putting pivot into data set, for now
			 */
			ObjectWithDistance<T>[] owds = new ObjectWithDistance[data.size() - 1];
			boolean pastPivot = false;
			for (int i : Range.range(0, data.size())) {
				if (i != pivPos) {
					T datum = data.get(i);
					owds[pastPivot ? i - 1 : i] = new ObjectWithDistance<>(
							datum, metric.distance(this.pivot, datum));
				} else {
					pastPivot = true;
				}
			}

			int pivotSwitch = Math.min(Integer.MAX_VALUE, owds.length / 2);
			Quicksort.placeOrdinal(owds, pivotSwitch);
			// Quicksort.placeMedian(owds);

			this.mu = owds[pivotSwitch].getDistance();

			if (!isHeadNode) {
				assert this.parent != null;
				try {
					this.fourPoint = new SimplexND<>(2, metric, this.pivot,
							this.parent);
				} catch (Exception e) {
					System.out.println("couldn't create 3D base simplex");
				}
			}

			List<T> leftList = new ArrayList<>();
			List<T> rightList = new ArrayList<>();
			for (int i : Range.range(0, pivotSwitch)) {
				final T s = owds[i].getValue();
				if (this.fourPoint != null) {
					double[] dists = { owds[i].getDistance(),
							metric.distance(s, parent) };
					double[] ap = this.fourPoint.formSimplex(dists);
					this.maxLeftXOffset = Math.max(maxLeftXOffset, ap[0]);
					this.maxLeftYOffset = Math.max(maxLeftYOffset, ap[1]);
				}
				leftList.add(s);
			}
			this.minRightXOffset = Double.MAX_VALUE;
			this.minRightYOffset = Double.MAX_VALUE;
			for (int i : Range.range(pivotSwitch, owds.length)) {
				final T s = owds[i].getValue();
				if (this.fourPoint != null) {
					double[] dists = { owds[i].getDistance(),
							metric.distance(s, parent) };
					double[] ap = this.fourPoint.formSimplex(dists);
					this.minRightXOffset = Math.min(minRightXOffset, ap[0]);
					this.minRightYOffset = Math.min(minRightYOffset, ap[1]);
				}
				rightList.add(owds[i].getValue());
			}

			if (leftList.size() > 0) {
				this.left = new TreeNode(leftList, this.pivot);
			}
			if (rightList.size() > 0) {
				this.right = new TreeNode(rightList, this.pivot);
			}
		}

		public void thresholdSearch(List<T> res, T query, double t,
				double parentDist) {
			double d = metric.distance(query, this.pivot);
			if (d <= t) {
				res.add(this.pivot);
			}
			if (this.left != null && !(d > mu + t)) {
				if (this.fourPoint == null || !excludeLeft(d, parentDist, t)) {
					this.left.thresholdSearch(res, query, t, d);
				}
			}
			if (this.right != null && !(d < mu - t)) {
				if (this.fourPoint == null || !excludeRight(d, parentDist, t)) {
					this.right.thresholdSearch(res, query, t, d);
				}
			}
		}

		private boolean excludeLeft(double d, double parentDist,
				double threshold) {
			double[] dists = { d, parentDist };
			double[] ap = this.fourPoint.formSimplex(dists);
			return ap[0] > this.maxLeftXOffset + threshold
					|| ap[1] > this.maxLeftYOffset + threshold;
		}

		private boolean excludeRight(double d, double parentDist,
				double threshold) {
			double[] dists = { d, parentDist };
			double[] ap = this.fourPoint.formSimplex(dists);
			return ap[0] < this.minRightXOffset - threshold
					|| ap[1] < this.minRightYOffset - threshold;
		}
	}

	protected HybridVPT(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.rand.setSeed(System.currentTimeMillis());
		this.root = new TreeNode(data);
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		this.root.thresholdSearch(res, query, t, 0);
		return res;
	}

	@Override
	public String getShortName() {
		return "hVPT";
	}

	public static void main(String[] a) throws Exception {
		TestContext tc = new TestContext(TestContext.Context.colors);
		tc.setSizes(tc.getDataCopy().size() / 10, 0);
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(tc.metric());
		HybridVPT<CartesianPoint> tree = new HybridVPT<>(tc.getDataCopy(), cm);
		cm.reset();
		int resSize = 0;
		for (CartesianPoint q : tc.getQueries()) {
			resSize += tree.thresholdSearch(q, tc.getThreshold()).size();
		}
		System.out.println("done with " + resSize + " results using "
				+ cm.reset() / tc.getQueries().size() + " distances per query");
	}
}
