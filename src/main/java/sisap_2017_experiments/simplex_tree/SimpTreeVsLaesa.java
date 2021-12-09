package sisap_2017_experiments.simplex_tree;

import java.util.List;

import searchStructures.SearchIndex;
import searchStructures.VPTree;
import sisap_2017_experiments.laesa.Laesa;
import sisap_2017_experiments.laesa.NpointFPIndexedLaesa;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Util_ISpaper;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

public class SimpTreeVsLaesa {

	public static void main(String[] a) throws Exception {

		TestContext tc = new TestContext(Context.nasa);
		int refNo = (int) Math.floor(Math.log(tc.dataSize()) / Math.log(2) + 2);
		System.out.println(refNo);
		tc.setSizes(tc.dataSize() / 10, 1000);
		List<CartesianPoint> refs = Util_ISpaper.getFFT(tc.getRefPoints(),
				tc.metric(), refNo);
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(tc.metric());

		final List<CartesianPoint> treeData = tc.getData();
		for (CartesianPoint d : tc.getRefPoints()) {
			if (!refs.contains(d)) {
				treeData.add(d);
			}
		}
		SimplexRefPointTree<CartesianPoint> t = new SimplexRefPointTree<>(
				treeData, refs, cm);
		cm.reset();
		SearchIndex<CartesianPoint> bestLaesa = getIndex(cm, treeData, refs);
		cm.reset();

		int totRes = 0;

		long t0 = System.currentTimeMillis();
		for (CartesianPoint q : tc.getQueries()) {
			List<CartesianPoint> res = t.thresholdSearch(q, tc.getThreshold());
			totRes += res.size();
		}
		System.out.println("done tree: " + totRes + "\t" + cm.reset()
				/ tc.getQueries().size() + "\t"
				+ (System.currentTimeMillis() - t0));

		totRes = 0;
		t0 = System.currentTimeMillis();
		for (CartesianPoint q : tc.getQueries()) {
			List<CartesianPoint> res = bestLaesa.thresholdSearch(q,
					tc.getThreshold());
			totRes += res.size();
		}
		System.out.println("done laes: " + totRes + "\t" + cm.reset()
				/ tc.getQueries().size() + "\t"
				+ (System.currentTimeMillis() - t0));
		totRes = 0;
		t0 = System.currentTimeMillis();
		for (CartesianPoint q : tc.getQueries()) {
			List<CartesianPoint> res = t.thresholdSearch(q, tc.getThreshold());
			totRes += res.size();
		}
		System.out.println("done tree: " + totRes + "\t" + cm.reset()
				/ tc.getQueries().size() + "\t"
				+ (System.currentTimeMillis() - t0));

		totRes = 0;
		t0 = System.currentTimeMillis();
		for (CartesianPoint q : tc.getQueries()) {
			List<CartesianPoint> res = bestLaesa.thresholdSearch(q,
					tc.getThreshold());
			totRes += res.size();
		}
		System.out.println("done laes: " + totRes + "\t" + cm.reset()
				/ tc.getQueries().size() + "\t"
				+ (System.currentTimeMillis() - t0));

		SearchIndex<CartesianPoint> vpt = new VPTree<>(treeData, cm);
		cm.reset();
		totRes = 0;
		t0 = System.currentTimeMillis();
		for (CartesianPoint q : tc.getQueries()) {
			List<CartesianPoint> res = vpt
					.thresholdSearch(q, tc.getThreshold());
			totRes += res.size();
		}
		System.out.println("done vpt: " + totRes + "\t" + cm.reset()
				/ tc.getQueries().size() + "\t"
				+ (System.currentTimeMillis() - t0));
	}

	private static <T> SearchIndex<T> getIndex(Metric<T> metric, List<T> data,
			List<T> refPoints) {
		NpointFPIndexedLaesa<T> res = new NpointFPIndexedLaesa<>(metric,
				refPoints.size());
		SearchIndex<T> si = Laesa.getSearchIndex(res, metric, data, refPoints);
		return si;
	}

}
