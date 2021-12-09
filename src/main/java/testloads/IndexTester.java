package testloads;

import java.util.ArrayList;
import java.util.List;

import searchStructures.AltitudeTree;
import searchStructures.GHTree;
import searchStructures.HilbertHyperplaneTree;
import searchStructures.HilbertQuadTree;
import searchStructures.SearchIndex;
import searchStructures.VPTree;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.ManhattanH;
import dataPoints.cartesian.TriDiscrim;

public class IndexTester {

	public static void main(String[] a) throws Exception {

		Metric<CartesianPoint> metric = new ManhattanH();
		Metric<CartesianPoint> m2 = new TriDiscrim<>();
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(metric);
		// for (int dims = 6; dims < 15; dims += 2) {
		// System.out.println("doing " + dims + " dims");
		// testCartesian(dims, cm, "euc", false, false);
		// }

		testSisap(cm, true, false);

	}

	protected static void testSisap(CountedMetric<CartesianPoint> cm,
			boolean times, boolean checkResults) throws Exception {
		final TestLoad.SisapFile sisapFile = TestLoad.SisapFile.colors;
		double[] ts = TestLoad.getSisapThresholds(sisapFile);
		final TestLoad testLoad = new TestLoad(sisapFile);
		List<CartesianPoint> queries = testLoad
				.getQueries(testLoad.dataSize() / 10);

		testIndices(testLoad, queries, ts, cm, times, checkResults);
	}

	protected static void testCartesian(final int dims,
			CountedMetric<CartesianPoint> cm, String metric, boolean times,
			boolean checkResults) {

		TestLoad testLoad = new TestLoad(dims, 100 * 1000, false);
		List<CartesianPoint> queries = testLoad
				.getQueries(testLoad.dataSize() / 10);
		CartesianThresholds ct = new CartesianThresholds();
		double thresh1 = ct.get(dims).get(metric).get(1);
		double thresh2 = ct.get(dims).get(metric).get(2);
		double thresh4 = ct.get(dims).get(metric).get(4);
		final double[] ts = { thresh1, thresh2, thresh4,
				ct.get(dims).get(metric).get(8),
				ct.get(dims).get(metric).get(16),
				ct.get(dims).get(metric).get(32) };

		testIndices(testLoad, queries, ts, cm, times, checkResults);
	}

	protected static void testIndices(TestLoad testLoad,
			List<CartesianPoint> queries, final double[] ts,
			CountedMetric<CartesianPoint> cm, boolean times,
			boolean checkResults) {
		AltitudeTree<CartesianPoint> alt = new AltitudeTree<>(
				testLoad.getDataCopy(), cm);
		HilbertHyperplaneTree<CartesianPoint> hht = new HilbertHyperplaneTree<>(
				testLoad.getDataCopy(), cm);
		VPTree<CartesianPoint> vpt = new VPTree<>(testLoad.getDataCopy(), cm);
		GHTree<CartesianPoint> ghtS = new GHTree<>(testLoad.getDataCopy(), cm);
		GHTree<CartesianPoint> ghtH = new GHTree<>(testLoad.getDataCopy(), cm);
		HilbertQuadTree<CartesianPoint> hqt = new HilbertQuadTree<>(
				testLoad.getDataCopy(), cm);
		// GHTree<CartesianPoint> ght2 = new GHTree<>(testLoad.getDataCopy(),
		// cm);
		// GNAT<CartesianPoint> gnat = new GNAT<>(testLoad.getDataCopy(), cm);
		ghtH.setCosExclusionEnabled(true);
		ghtH.setCrExclusionEnabled(true);
		ghtH.setVorExclusionEnabled(true);

		ghtS.setVorExclusionEnabled(true);
		ghtS.setCrExclusionEnabled(true);
		ghtS.setCosExclusionEnabled(false);
		// ght.setVorExclusionEnabled(true);
		//
		// ght2.setCosExclusionEnabled(false);
		// ght2.setCrExclusionEnabled(true);
		// ght2.setVorExclusionEnabled(true);

		List<SearchIndex<CartesianPoint>> indices = new ArrayList<>();
		// indices.add(vpt);
		// indices.add(gnat);
		indices.add(alt);
		indices.add(vpt);
		indices.add(hht);
		indices.add(hqt);
		indices.add(ghtH);
		// indices.add(alt);

		cm.reset();

		System.out.print("metric");
		for (int i = 0; i < ts.length; i++) {
			System.out.print("\tt" + i);
		}
		System.out.println();
		int[] resses = new int[queries.size() * ts.length];
		boolean first = true;
		for (SearchIndex<CartesianPoint> si : indices) {
			System.out.print(si.getShortName());
			int resPtr = 0;
			for (double thresh : ts) {
				long t0 = System.currentTimeMillis();
				long dists = 0;
				for (CartesianPoint query : queries) {
					List<CartesianPoint> res = si
							.thresholdSearch(query, thresh);
					if (checkResults) {
						if (first) {
							resses[resPtr++] = res.size();
						} else {
							if (resses[resPtr++] != res.size()) {
								System.out.println("error: first search found "
										+ resses[resPtr - 1]
										+ ", this one found " + res.size());
							}
						}
					}
					dists += cm.reset();
				}
				if (times) {
					System.out.print("\t" + (System.currentTimeMillis() - t0));
				} else {
					System.out.print("\t" + dists / ((double) queries.size()));
				}
			}
			System.out.println();
			first = false;
		}
	}
}
