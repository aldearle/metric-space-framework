package examples;

import java.util.List;

import searchStructures.GHMTree;
import testloads.CartesianThresholds;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Cosine;
import dataPoints.cartesian.CosineNormalised;

public class CheckCosNorm {

	public static void main(String[] args) throws Exception {

		// set parameters for Cartesian tests
		int noOfQueries = 1000;
		final int dataSize = 1000 * 1000;
		int dim = 10;

		TestLoad tl = new TestLoad(dim, dataSize + noOfQueries, false);
		// set the query threshold to return 4 per million for Cosine
		// distance in 10 dimensions
		double thresh = CartesianThresholds.getThreshold("cos", dim, 1);
		// calculate an equivalent threshold for cos/normalised
		double thresh2 = CosineNormalised.convertFromCosine(thresh);
		System.out.println(thresh);

		// getQueries removes the queries from the testload
		List<CartesianPoint> qs = tl.getQueries(noOfQueries);
		final Metric<CartesianPoint> cos1 = new Cosine<>();

		// create an extension of Cosine to give the normalised value
		Metric<CartesianPoint> cos2 = new Metric<CartesianPoint>() {

			@Override
			public double distance(CartesianPoint x, CartesianPoint y) {
				return CosineNormalised.convertFromCosine(cos1.distance(x, y));
			}

			@Override
			public String getMetricName() {
				return "cosNorm";
			}
		};

		// counts the number of distance applications used on the metric
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(cos1);
		CountedMetric<CartesianPoint> cm2 = new CountedMetric<>(cos2);

		// getDataCopy() returns a clean copy; some indexing structures may
		// update the
		// lists as they are passed in
		checkMet(tl.getDataCopy(), qs, thresh, cm, false);
		checkMet(tl.getDataCopy(), qs, thresh, cm, true);
		checkMet(tl.getDataCopy(), qs, thresh2, cm2, false);
		checkMet(tl.getDataCopy(), qs, thresh2, cm2, true);

	}

	protected static <T> void checkMet(List<T> dat, List<T> qs,
			final double thresh, CountedMetric<T> cm, boolean fourPoint) {
		// need to get this in case the indexing structure updates the list
		int dataSize = dat.size();

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
		// structures are working correctly!
		int res = 0;
		for (T q : qs) {
			res += t.thresholdSearch(q, thresh).size();
		}
		System.out.println(res + " results, performed " + cm.reset()
				/ qs.size() + " distance calcs per query out of max possible "
				+ dataSize);
	}
}
