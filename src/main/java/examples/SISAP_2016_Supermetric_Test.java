package examples;

import generic_partition_tree.MonotonicTree;
import generic_partition_tree.UnbalHilTree;

import java.util.List;

import searchStructures.SATGeneric;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.CountedMetric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;


/**
 * @author Richard Connor
 * 
 *         an example test to show how to reproduce results in a paper submitted
 *         to SISAP 2016
 *
 */
public class SISAP_2016_Supermetric_Test {

	public static void main(String[] args) throws Exception {

		// select the SISAP colors file
		final SisapFile sFile = TestLoad.SisapFile.nasa;
		TestLoad tl = new TestLoad(sFile);
		// and the standard query thresholds
		double[] thresholds = TestLoad.getSisapThresholds(sFile);

		// select 10% of the file as queries
		List<CartesianPoint> queries = tl.getQueries(tl.dataSize() / 10);
		// and the rest as data
		List<CartesianPoint> data = tl.getDataCopy();

		// a counted Euclidean metric
		CountedMetric<CartesianPoint> m = new CountedMetric<>(new Euclidean<>());

		// sorry this is really quite hacky but I needed a solution late at
		// night and Java isn't good for this....
		MonotonicTree.setTreeType(MonotonicTree.TreeType.monotonic);
		MonotonicTree.setStrategy(MonotonicTree.Strategy.furthest);
		// now construct the fastest 4-point index we know
		UnbalHilTree<CartesianPoint> hil = new UnbalHilTree<>(data, m);

		// and a distal SAT to compare with
		SATGeneric<CartesianPoint> sat = new SATGeneric(tl.getDataCopy(), m,
				SATGeneric.Strategy.distal);

		// we don't care for now about build costs so reset the distance count
		m.reset();

		// it's always worth checking that faster indexes actually return all
		// the correct results!
		int totalResults = 0;
		for (CartesianPoint q : queries) {
			List<CartesianPoint> results = hil
					.thresholdSearch(q, thresholds[0]);
			totalResults += results.size();
		}
		System.out.println("at the lowest threshold, HilMonGHT took "
				+ m.reset() / queries.size() + " distance calcs per query ("
				+ totalResults + " total results)");

		totalResults = 0;
		for (CartesianPoint q : queries) {
			List<CartesianPoint> results = sat
					.thresholdSearch(q, thresholds[0]);
			totalResults += results.size();
		}
		System.out.println("at the lowest threshold, DiSAT took "
				+ m.reset() / queries.size() + " distance calcs per query ("
				+ totalResults + " total results)");

	}
}
