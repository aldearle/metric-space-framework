package n_point_surrogate;

import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;
import is_paper_experiments.dynamic_binary_partitions.MonotoneHyperplaneTree;
import is_paper_experiments.dynamic_binary_partitions.SearchTree;

import java.util.List;

import searchStructures.SearchIndex;
import testloads.CartesianThresholds;
import testloads.TestContext;
import util.Measurements;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannon;

public class SurrogateTester {

	public static void main(String[] args) throws Exception {

		try {
			assert false;
			System.out.println("assertions off");
		} catch (Throwable e) {
			System.out.println("assertions on");
		}

		measureSomeThingsProperly();
	}

	private static void measureSomeThingsProperly() throws Exception {
		Measurements m = new Measurements("mhtDists", "mhtTime", "surDists",
				"surTime", "recCount");
		TestContext tc = new TestContext(TestContext.Context.colors);
		tc.setSizes((tc.getDataCopy().size()) / 10 - 1000, 1000);

		int refPoints = 15;

		while (!m.allDone(0.01)) {
			doTest(m, tc, refPoints);
			System.out.println("still working...");
			m.spewResults();
		}
		System.out.println("colors, threshold t_0, " + refPoints
				+ " ref points, fft");
		m.spewResults();
	}

	private static void doTest(Measurements m, TestContext tc, int noOfRefPoints)
			throws Exception {
		assert tc != null;
		CartesianThresholds ct;
		final Metric<CartesianPoint> metric = new JensenShannon<>(false, false);
		CountedMetric<CartesianPoint> originalMetric = new CountedMetric<>(
				metric);
		// CountedMetric<CartesianPoint> recheckMetricT = new CountedMetric<>(
		// metric);
		// CountedMetric<CartesianPoint> recheckMetricF = new CountedMetric<>(
		// metric);
		CountedMetric<CartesianPoint> recheckMetricN = new CountedMetric<>(
				metric);

		List<CartesianPoint> refs = Util_ISpaper.getFFT(tc.getRefPoints(), metric,
				noOfRefPoints);
		// List<CartesianPoint> refs = Util.getRandom(tc.getRefPoints(), 10);
		// List<CartesianPoint> refs = tc.getRefPoints();
		// System.out.println(Util.minPivot(refs, metric));
		// System.out.println(Util.maxPivot(refs, metric));

		// SurrogateSpaceCreator<CartesianPoint> sscF = new
		// SurrogateSpaceCreator<>(
		// tc.getDataCopy(), recheckMetricF,
		// SurrogateSpaceCreator.Type.fourPoint);
		// SurrogateSpaceCreator<CartesianPoint> sscT = new
		// SurrogateSpaceCreator<>(
		// tc.getDataCopy(), recheckMetricT,
		// SurrogateSpaceCreator.Type.threePoint);
		SurrogateSpaceCreator<CartesianPoint> sscN = new SurrogateSpaceCreator<>(
				tc.getDataCopy(), recheckMetricN,
				SurrogateSpaceCreator.Type.nPoint);
		// sscT.addData3p(refs);
		// sscF.addData4p(refs);
		sscN.addDataNp(refs);
		// recheckMetricF.reset();
		// recheckMetricT.reset();
		recheckMetricN.reset();

		BinaryPartitionFactory<CartesianPoint> sw = new SimpleWidePartition<>(
				originalMetric);
		final MonotoneHyperplaneTree<CartesianPoint> mhpt = new MonotoneHyperplaneTree<>(
				originalMetric);
		mhpt.setFourPoint(true);
		mhpt.setPartitionStrategy(sw);

		SearchIndex<CartesianPoint> vpt = new SearchTree<>(tc.getDataCopy(),
				mhpt);
		//
		// SearchIndex<CartesianPoint> vpt = new VPTree<>(tc.getDataCopy(),
		// originalMetric);
		originalMetric.reset();

		final List<CartesianPoint> queries = tc.getQueries();
		int xres = 0;
		int yres = 0;
		int zres = 0;
		int z1res = 0;
		final double threshold = tc.getThreshold();

		long t0 = System.currentTimeMillis();
		for (int q : Range.range(0, queries.size())) {
			final CartesianPoint query = queries.get(q);
			List<CartesianPoint> z1 = sscN.thresholdSearch(query, threshold);
			z1res += z1.size();
		}
		m.addCount("surTime", (int) (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();
		for (int q : Range.range(0, queries.size())) {
			final CartesianPoint query = queries.get(q);
			List<CartesianPoint> x = vpt.thresholdSearch(query, threshold);
			xres += x.size();
		}
		m.addCount("mhtTime", (int) (System.currentTimeMillis() - t0));

		assert xres == z1res : "bad query outcome";
		System.out.println(xres);

		final int qSize = queries.size();

		m.addCount("mhtDists", originalMetric.reset() / qSize);
		m.addCount("surDists", sscN.countMetricCalls() / qSize);
		m.addCount("recCount", recheckMetricN.reset() / qSize);
	}

}
