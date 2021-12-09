package sisap_2017_experiments.boundedVPT;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.CosineNormalised;
import dataPoints.cartesian.JensenShannon;
import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import searchStructures.SearchIndex;
import sisap_2017_experiments.NdimSimplex;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Range;
import util.Util_ISpaper;

import java.util.ArrayList;
import java.util.List;

public class SimBoundedVPT<T> extends SearchIndex<T> {

	List<T> refPoints;
	NdimSimplex<T> simplex;
	TreeNode root;

	private class TreeNode {
		double pivotDist;
		double[] apex;
		T datum;
		TreeNode left, right;

		TreeNode(List<T> data) {
			this.datum = data.get(0);
			this.apex = SimBoundedVPT.this.simplex.getApex(this.datum);
			if (data.size() > 1) {
				@SuppressWarnings("unchecked")
				ObjectWithDistance<T>[] owds = new ObjectWithDistance[data
						.size() - 1];
				for (int i : Range.range(0, data.size() - 1)) {
					final T dat = data.get(i + 1);
					owds[i] = new ObjectWithDistance<>(dat,
							SimBoundedVPT.this.metric.distance(dat, this.datum));
				}
				Quicksort.placeMedian(owds);
				this.pivotDist = owds[owds.length / 2].getDistance();
				List<T> leftList = new ArrayList<>();
				List<T> rightList = new ArrayList<>();
				for (int i : Range.range(0, owds.length / 2)) {
					leftList.add(owds[i].getValue());
				}
				for (int i : Range.range(owds.length / 2, owds.length)) {
					rightList.add(owds[i].getValue());
				}
				if (leftList.size() != 0) {
					this.left = new TreeNode(leftList);
				}
				if (rightList.size() != 0) {
					this.right = new TreeNode(rightList);
				}
			}
		}

		void search(T query, double threshold, double[] qApex, List<T> res) {
			// double d = metric.distance(query, this.datum);
			double[] bs = NdimSimplex.getBounds(qApex, this.apex);
			if (bs[0] <= threshold) {
				double d = metric.distance(query, this.datum);
				bs[0] = d;
				bs[1] = d;
				if (d <= threshold) {
					res.add(this.datum);
				}

			}
			if (this.left != null) {
				if (!(bs[0] > this.pivotDist + threshold)) {
					this.left.search(query, threshold, qApex, res);
				}
			}
			if (this.right != null) {
				if (!(bs[1] < this.pivotDist - threshold)) {
					this.right.search(query, threshold, qApex, res);
				}
			}
		}
	}

	protected SimBoundedVPT(List<T> data, Metric<T> metric, int dimension) {
		super(data, metric);
		this.refPoints = Util_ISpaper.getFFT(data.subList(0, 1000), metric,
				dimension);
		for (T ref : this.refPoints) {
			data.remove(ref);
		}
		this.simplex = new NdimSimplex<>(metric, this.refPoints);
		this.root = new TreeNode(data);
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> sols = new ArrayList<>();
		double[] qdists = new double[this.refPoints.size()];
		for (int i : Range.range(0, this.refPoints.size())) {
			final double d = this.metric.distance(query, this.refPoints.get(i));
			if (d <= t) {
				sols.add(this.refPoints.get(i));
			}
			qdists[i] = d;
		}
		double[] apex = this.simplex.getApex(qdists);
		this.root.search(query, t, apex, sols);
		return sols;
	}

	@Override
	public String getShortName() {
		return "simBoundedVPT";
	}

	public static void main(String[] a) throws Exception {
		TestContext tc = new TestContext(Context.colors);
		tc.setSizes(tc.dataSize() / 10, 0);

		Metric<CartesianPoint> metric = new JensenShannon<>(false, true);
		double queryThreshold = 0.13; // 0.138 gives 180k instead of 120k
										// results

		metric = new CosineNormalised<>();
		queryThreshold = 0.032;

		SimBoundedVPT<CartesianPoint> vpt = new SimBoundedVPT<>(tc.getData(),
				metric, 10);

		long t0 = System.currentTimeMillis();
		int tots = 0;
		for (CartesianPoint q : tc.getQueries()) {
			List<CartesianPoint> res = vpt.thresholdSearch(q, queryThreshold);
			tots += res.size();
		}
		System.out.println(System.currentTimeMillis() - t0);
		System.out.println(tots);
		t0 = System.currentTimeMillis();
		tots = 0;
		for (CartesianPoint q : tc.getQueries()) {
			List<CartesianPoint> res = vpt
					.thresholdSearch(q, queryThreshold);
			tots += res.size();
		}
		System.out.println(System.currentTimeMillis() - t0);
		System.out.println(tots);

	}

}
