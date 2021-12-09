package examples;

import java.util.List;

import searchStructures.SearchIndex;
import searchStructures.VPTree;
import testloads.TestContext;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

/**
 * @author Richard Connor
 * 
 *         a concise example showing some of the key features of the
 *         framework...
 *
 */
public class StartHere {

	/**
	 * @param a
	 * @throws Exception
	 *             if the data isn't present where expected
	 *
	 *             example use of framework: reports some results for querying
	 *             the SISAP nasa data set, with Euclidean distance, using a
	 *             vantage point tree structure
	 */
	public static void main(String[] a) throws Exception {
		// much of the code contains assertions, it's usually best to develop
		// with them switched on
		try {
			assert false : "assertions are enabled";
			System.out.println("assertions are not enabled");
		} catch (Throwable t) {
			System.out.println(t.getMessage());
		}

		// load the SISAP benchmark "nasa" file to perform tests
		TestContext tc = new TestContext(TestContext.Context.nasa);
		// removes 10% of the values from the context for queries, 0 for use as
		// global reference points
		tc.setSizes(4015, 10);
		// we'll use l_2 distance over the space
		Metric<CartesianPoint> euc = new Euclidean<>();
		// and we want to know how many distance calculations will be performed
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(euc);

		// create a Vantage Point tree, a good general indexing structure
		// the abstraction allows collections of different structures to allow
		// batch testing etc
		final List<CartesianPoint> data = tc.getDataCopy();
		System.out.println("data size is " + data.size());
		SearchIndex<CartesianPoint> index = new VPTree<>(data, cm);
		System.out.println("building " + index.getShortName() + " required "
				+ cm.reset() + " distance calcs");

		int res = 0;
		for (CartesianPoint q : tc.getQueries()) {
			// query the collection at the smallest of the standard thresholds,
			// add the number of results to the checking accumulator
			res += index.thresholdSearch(q, tc.getThresholds()[0]).size();
		}
		System.out.println("querying took a mean of " + cm.reset()
				/ tc.getQueries().size() + " distance calcs per query");
		System.out.println("(" + res + " results were returned)");
	}
}
