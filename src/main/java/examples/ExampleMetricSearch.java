package examples;

import java.util.List;

import searchStructures.SearchIndex;
import searchStructures.VPTree;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

/**
 * @author Richard
 *
 *         this is a class that shows you how to use the framework in outline,
 *         just a little help before all the documentation is in place (which
 *         might well be never...)
 */
public class ExampleMetricSearch {

	public static void main(String[] args) throws Exception {
		// use the SISAP "nasa" benchmark file
		final SisapFile testFile = TestLoad.SisapFile.nasa;
		// create a new test load using that
		TestLoad tl = new TestLoad(testFile);
		// and let's use 10% of the file as queries to query the remainder
		List<CartesianPoint> queries = tl.getQueries(tl.dataSize() / 10);
		// t is a search threshold that should return about 0.01% of the data...
		double t = TestLoad.getSisapThresholds(testFile)[0];
		// ... using Euclidean distance over the test points, so we'll use that
		// metric
		Metric<CartesianPoint> euc = new Euclidean<>();

		System.out.println("so... we're querying the " + testFile
				+ " benchmark file, running " + queries.size()
				+ " queries over " + tl.dataSize() + " data");
		System.out.println("using " + euc.getMetricName() + " distance in a "
				+ queries.get(0).getPoint().length + "-dimensional space");

		System.out.println("serial: "
				+ doQueriesStupidly(tl.getDataCopy(), queries, euc, t));

		System.out.println("indexed: "
				+ doQueriesCleverly(tl.getDataCopy(), queries, euc, t));

	}

	private static String doQueriesCleverly(List<CartesianPoint> data,
			List<CartesianPoint> queries, Metric<CartesianPoint> euc, double t) {
		int noOfRes = 0;
		// build a balanced vantage point tree for the data
		SearchIndex<CartesianPoint> vpt = new VPTree<>(data, euc);
		double t0 = System.currentTimeMillis();
		// and search all queries against it
		for (CartesianPoint q : queries) {
			// the search result is a list of values within the threshold
			noOfRes += vpt.thresholdSearch(q, t).size();
		}
		double t1 = System.currentTimeMillis();
		return "got " + noOfRes + " results in " + (t1 - t0) + " milliseconds";
	}

	private static String doQueriesStupidly(List<CartesianPoint> data,
			List<CartesianPoint> queries, Metric<CartesianPoint> euc, double t) {
		int noOfRes = 0;
		double t0 = System.currentTimeMillis();
		// serially, count the total number of data points within q of each
		// query
		for (CartesianPoint q : queries) {
			for (CartesianPoint p : data) {
				if (euc.distance(q, p) <= t) {
					noOfRes++;
				}
			}
		}
		double t1 = System.currentTimeMillis();
		return "got " + noOfRes + " results in " + (t1 - t0) + " milliseconds";
	}

}
