package is_paper_experiments.dynamic_binary_partitions;

import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.RandomPartition;
import is_paper_experiments.binary_partitions.SimpleWidePartition;

import java.util.ArrayList;
import java.util.List;

import testloads.TestContext;
import util.Range;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

/**
 * 
 * utility class that is likely to change as different experiments are run
 * 
 * @author Richard Connor
 *
 */
public class SearchTreeTester {

	private static List<BinaryExclusionFactory<CartesianPoint, ?, ?>> getMechanismsPCA(
			TestContext tc, Metric<CartesianPoint> cm) {

		List<BinaryExclusionFactory<CartesianPoint, ?, ?>> res = new ArrayList<>();
		BinaryPartitionFactory<CartesianPoint> sw = new SimpleWidePartition<>(
				cm);
		BinaryPartitionFactory<CartesianPoint> ran = new RandomPartition<>(cm);
		BinaryPartitionFactory<CartesianPoint> part = sw;

		final MonotoneHyperplaneTree<CartesianPoint> mhpt = new MonotoneHyperplaneTree<>(
				cm);
		mhpt.setFourPoint(true);
		mhpt.setPartitionStrategy(part);
		res.add(mhpt);

		final MonotonePcaHyperplaneTree<CartesianPoint> m2 = new MonotonePcaHyperplaneTree<>(
				cm);
		m2.setFourPoint(true);
		m2.setPartitionStrategy(part);
		res.add(m2);
		//
		// final MonotonePcaHyperplaneTree_Lucia<CartesianPoint> m2L = new
		// MonotonePcaHyperplaneTree_Lucia<>(
		// cm);
		// m2L.setFourPoint(true);
		// m2L.setPartitionStrategy(part);
		// res.add(m2L);

		final MonotoneBalancedHyperplaneTree<CartesianPoint> bal = new MonotoneBalancedHyperplaneTree<>(
				cm);
		bal.setFourPoint(true);
		bal.setPartitionStrategy(part);
		res.add(bal);

		// final VantagePointTree<CartesianPoint> vpt = new
		// VantagePointTree<>(cm);
		// res.add(vpt);

		// // add a HPT using the four point property and random reference point
		// // selection
		// final HyperplaneTree<CartesianPoint> hpt1 = new HyperplaneTree<>(cm);
		// hpt1.setFourPoint(true);
		// hpt1.setPartitionStrategy(part);
		// res.add(hpt1);

		return res;
	}

	private static List<BinaryExclusionFactory<CartesianPoint, ?, ?>> getMechanisms(
			TestContext tc, Metric<CartesianPoint> cm) {

		boolean fourPoint = false;
		List<BinaryExclusionFactory<CartesianPoint, ?, ?>> res = new ArrayList<>();

		// add exhaustive search to benchmark
		final ExhaustiveSearchTree<CartesianPoint> est = new ExhaustiveSearchTree<>(
				cm);
		res.add(est);
		// add balanced vantage point tree search to benchmark
		final VantagePointTree<CartesianPoint> vpt = new VantagePointTree<>(cm);
		res.add(vpt);

		// add a HPT using the four point property and random reference point
		// selection
		final HyperplaneTree<CartesianPoint> hpt1 = new HyperplaneTree<>(cm);
		hpt1.setFourPoint(fourPoint);
		hpt1.setPartitionStrategy(new RandomPartition<>(cm));
		res.add(hpt1);

		// add a HPT using the four point property and simple, widely spaced
		// reference point selection
		final HyperplaneTree<CartesianPoint> hpt2 = new HyperplaneTree<>(cm);
		hpt2.setFourPoint(fourPoint);
		hpt2.setPartitionStrategy(new SimpleWidePartition<>(cm));
		res.add(hpt2);

		// add a MHPT using the four point property and random reference point
		// selection
		final MonotoneHyperplaneTree<CartesianPoint> mhpt = new MonotoneHyperplaneTree<>(
				cm);
		mhpt.setFourPoint(fourPoint);
		mhpt.setPartitionStrategy(new SimpleWidePartition<>(cm));
		res.add(mhpt);

		// add a MHPT using the four point property and wide reference point
		// selection
		final MonotoneHyperplaneTree<CartesianPoint> mhpt2 = new MonotoneHyperplaneTree<>(
				cm);
		mhpt2.setFourPoint(fourPoint);
		mhpt2.setPartitionStrategy(new SimpleWidePartition<>(cm));
		res.add(mhpt2);

		return res;
	}

	/**
	 * kick off whatever is currently here...!
	 * 
	 * @param a
	 *            not used
	 * @throws Exception
	 *             if test context can't be created
	 */
	public static void main(String[] a) throws Exception {
		try {
			assert false;
			System.out.println("assertions not enabled");
		} catch (Throwable t) {
			System.out.println("assertions are enabled");
		}

		int noOfQueries = -1;

		// TestContext tc = new TestContext(TestContext.Context.euc10);
		// noOfQueries = 1000;
		// System.out.println("euc10");

		 TestContext tc = new TestContext(TestContext.Context.nasa);
		 noOfQueries = tc.getDataCopy().size() / 10;
		 System.out.println("nasa");
		//
		
		// TestContext tc = new TestContext(TestContext.Context.colors);
		// System.out.println("colors");
		// noOfQueries = tc.getDataCopy().size() / 10;

		tc.setSizes(noOfQueries, 0);
		double[] ts = tc.getThresholds();

		CountedMetric<CartesianPoint> cm = new CountedMetric<>(tc.metric());
		// List<BinaryExclusionFactory<CartesianPoint, ?, ?>> excMechs =
		// getMechanisms(
		// tc, cm);
		List<BinaryExclusionFactory<CartesianPoint, ?, ?>> excMechs = getMechanismsPCA(
				tc, cm);

		cm.reset();

		for (BinaryExclusionFactory<CartesianPoint, ?, ?> mech : excMechs) {
			System.out.print(mech.getName() + "\t");
			System.out.print("t0    \t");
			System.out.print("n_t0    \t");
			System.out.print("t1    \t");
			System.out.print("n_t1    \t");
			System.out.print("t2    \t");
			System.out.print("n_t2    \t");
		}
		System.out.println();

		for (int i : Range.range(0, 20)) {
			for (BinaryExclusionFactory<CartesianPoint, ?, ?> mech : excMechs) {

				SearchTree<CartesianPoint, ?, ?> tree = new SearchTree<>(
						tc.getDataCopy(), mech);
				cm.reset();
				System.out.print("\t");
				for (double t : ts) {
					List<CartesianPoint> res = new ArrayList<>();
					for (CartesianPoint q : tc.getQueries()) {
						res.addAll(tree.thresholdSearch(q, t));
					}
					System.out.print(cm.reset() / noOfQueries + "\t");
					System.out.print(res.size() + "\t");
				}
			}
			System.out.println();
		}

		System.out.println("done");
	}
}
