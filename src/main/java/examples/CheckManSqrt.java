package examples;

import java.util.List;

import searchStructures.GHMTree;
import testloads.CartesianThresholds;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Manhattan;

public class CheckManSqrt {

	public static void main(String[] args) throws Exception {

		// set parameters for Cartesian tests
		int noOfQueries = 1000;
		final int dataSize = 1000 * 1000;
		int manDim = 10;

		TestLoad tl = new TestLoad(manDim, dataSize + noOfQueries, false);
		// set the query threshold to return 1 per million for Manhattan
		// distance in 10 dimensions
		final double thresh = CartesianThresholds
				.getThreshold("man", manDim, 1);

		// alternatively, use of of the SISAP benchmark files
		final SisapFile sisapFile = TestLoad.SisapFile.colors;
		TestLoad tl1 = new TestLoad(sisapFile);
		double thresh1 = TestLoad.getSisapThresholds(sisapFile)[0];

		// getQueries removes the queries from the testload
		List<CartesianPoint> qs = tl.getQueries(noOfQueries);
		final Metric<CartesianPoint> man = new Manhattan<>();

		// create an extension of Manhattan
		Metric<CartesianPoint> manRoot = new Metric<CartesianPoint>() {

			@Override
			public double distance(CartesianPoint x, CartesianPoint y) {
				return Math.sqrt(man.distance(x, y));
			}

			@Override
			public String getMetricName() {
				return "manRoot";
			}
		};

		// counts the number of distance applications used on the metric
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(man);
		CountedMetric<CartesianPoint> cm2 = new CountedMetric<>(manRoot);

		// getDataCopy() returns a clean copy; some indexing structures may
		// update the
		// lists as they are passed in
		checkMet(tl.getDataCopy(), qs, thresh, cm, false);
		checkMet(tl.getDataCopy(), qs, thresh, cm, true);
		checkMet(tl.getDataCopy(), qs, Math.sqrt(thresh), cm2, false);
		checkMet(tl.getDataCopy(), qs, Math.sqrt(thresh), cm2, true);

	}

	protected static <T> void checkMet(List<T> dat, List<T> qs,
			final double thresh, CountedMetric<T> cm, boolean fourPoint) {
		// generic monotonous hyperplane tree; usually the most efficient
		// indexing structure
		GHMTree<T> t = new GHMTree<>(dat, cm);

		// reset the counted metric before any queries are performed
		cm.reset();

		// need to set some exclusion criteria or else all the data will be
		// searched
		t.setCrExclusionEnabled(true);
		t.setVorExclusionEnabled(true);
		// old name, should be Hilbert; only use this if the metric has the
		// four-point property!
		t.setCosExclusionEnabled(fourPoint);

		// count the number of results returned for all queries; a useful check,
		// unless we're very unlucky it demonstrates that experimental search
		// strucutres are working correctly!
		int res = 0;
		for (T q : qs) {
			res += t.thresholdSearch(q, thresh).size();
		}
		System.out.println(res + " results, performed " + cm.reset()
				+ " distance calcs out of max possible " + qs.size()
				* dat.size());
	}
}
