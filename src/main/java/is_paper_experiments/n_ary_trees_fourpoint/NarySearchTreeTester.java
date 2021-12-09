package is_paper_experiments.n_ary_trees_fourpoint;

import is_paper_experiments.different_sats.HPT_random;
import is_paper_experiments.different_sats.SAT_distal;
import is_paper_experiments.different_sats.SAT_global;
import is_paper_experiments.different_sats.SAT_pure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import testloads.TestContext;
import testloads.TestContext.Context;
import util.Measurements;
import util.Range;
import util.Util_ISpaper;
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
public class NarySearchTreeTester {

	private static String[] criteria = { "buildDists", "queryDists_t0",
			"queryDists_t1", "queryDists_t2", "buildTime", "queryTime_t0",
			"queryTime_t1", "queryTime_t2" };

	private static List<NaryExclusionFactory<CartesianPoint, ?, ?>> getSATMechanismsForPaper(
			TestContext tc, Metric<CartesianPoint> cm, final boolean fourPoint,
			boolean satOut) {

		List<NaryExclusionFactory<CartesianPoint, ?, ?>> res = new ArrayList<>();
		// add exhaustive search to benchmark
		// res.add(new NoExclusionFactory<>(cm));

		res.add(new SAT_pure<CartesianPoint>(cm, fourPoint, satOut));
		res.add(new SAT_distal<>(cm, fourPoint, SAT_distal.Arity.unlimited,
				satOut));
		res.add(new SAT_distal<>(cm, fourPoint, SAT_distal.Arity.fixed, satOut));
		res.add(new SAT_distal<>(cm, fourPoint, SAT_distal.Arity.log, satOut));
		res.add(new SAT_global<>(cm, fourPoint, SAT_global.Arity.fixed, satOut));
		res.add(new SAT_global<>(cm, fourPoint, SAT_global.Arity.log, satOut));

		return res;
	}

	private static List<NaryExclusionFactory<CartesianPoint, ?, ?>> getHPTMechanismsForPaper(
			TestContext tc, Metric<CartesianPoint> cm, final boolean fourPoint,
			boolean satOut) {

		List<NaryExclusionFactory<CartesianPoint, ?, ?>> res = new ArrayList<>();

		boolean[] ffts = { true, false };
		for (boolean fft : ffts) {
			res.add(new HPT_random<>(cm, fourPoint, HPT_random.Arity.binary,
					fft));
			res.add(new HPT_random<>(cm, fourPoint, HPT_random.Arity.fixed, fft));
			res.add(new HPT_random<>(cm, fourPoint, HPT_random.Arity.log, fft));
		}

		return res;
	}

	//
	// private static List<NaryExclusionFactory<CartesianPoint, ?, ?>>
	// getMechanisms(
	// TestContext tc, Metric<CartesianPoint> cm, final boolean fourPoint) {
	//
	// List<NaryExclusionFactory<CartesianPoint, ?, ?>> res = new ArrayList<>();
	// // add exhaustive search to benchmark
	// res.add(new NoExclusionFactory<>(cm));
	// res.add(new PartitionExclusionFactory<>(cm, fourPoint));
	// res.add(new SAT2ExclusionFactory<CartesianPoint>(cm, false) {
	//
	// @Override
	// protected List<CartesianPoint> getReferencePoints(
	// List<CartesianPoint> data, CartesianPoint centre) {
	// int maxNeighbs = 3;
	// List<CartesianPoint> l = new ArrayList<>();
	// while (l.size() < maxNeighbs && data.size() > 0) {
	// l.add(data.remove(rand.nextInt(data.size())));
	// }
	// data.addAll(l);
	// return l;
	// }
	//
	// @Override
	// protected boolean useFourPointProperty() {
	// return fourPoint;
	// }
	//
	// @Override
	// protected boolean useSatProperty() {
	// return false;
	// }
	//
	// @Override
	// public String getName() {
	// return "nSAT_2_rand";
	// }
	//
	// });
	//
	// res.add(new SAT_pure(cm, fourPoint, false));
	// res.add(new SAT_distal(cm, fourPoint, SAT_distal.Arity.unlimited,
	// false));
	// res.add(new SAT_hybrid(cm, fourPoint, true, false));
	//
	// res.add(new SAT2ExclusionFactory<CartesianPoint>(cm, false) {
	//
	// @Override
	// protected List<CartesianPoint> getReferencePoints(
	// List<CartesianPoint> data, CartesianPoint centre) {
	//
	// ObjectWithDistance<CartesianPoint>[] owds = new ObjectWithDistance[data
	// .size()];
	// int ptr = 0;
	// for (CartesianPoint dat : data) {
	// double dist = this.metric.distance(centre, dat);
	// owds[ptr++] = new ObjectWithDistance<>(dat, dist);
	// }
	// Quicksort.sort(owds);
	//
	// List<CartesianPoint> referencePoints = new ArrayList<>();
	//
	// for (int i = owds.length - 1; i >= 0; i--) {
	// ObjectWithDistance<CartesianPoint> nowd = owds[i];
	// // }
	// // for (ObjectWithDistance<CartesianPoint> nowd : owds) {
	//
	// CartesianPoint next = nowd.getValue();
	// double pDist = nowd.getDistance();
	// // double pDist = owds[ptr--].getDistance();
	//
	// /*
	// * keep adding closest to neighbours until one of the
	// * neighbours is closer than the centre point - preserve
	// * invariant that all neighbours are closer to centre point
	// * than any other neighbour
	// */
	// boolean centreIsClosest = true;
	// for (CartesianPoint n : referencePoints) {
	// if (metric.distance(next, n) < pDist) {
	// centreIsClosest = false;
	// }
	// }
	// if (centreIsClosest) {
	// referencePoints.add(next);
	// }
	// }
	// return referencePoints;
	// }
	//
	// @Override
	// protected boolean useFourPointProperty() {
	// return fourPoint;
	// }
	//
	// @Override
	// protected boolean useSatProperty() {
	// return true;
	// }
	//
	// @Override
	// public String getName() {
	// return "SAT_diSat";
	// }
	// });
	//
	// return res;
	// }

	/**
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
			System.out.println("assertions enabled");
		}

		Context[] sisapFiles = { Context.colors, Context.nasa };

		Context file = sisapFiles[1];
		boolean fourPoint = true;
		boolean satOut = true;
		int iterations = 10;

		// TestContext tc = new TestContext(TestContext.Context.euc20,
		// 1000 * 1000 + 1000);
		TestContext tc = new TestContext(file);

		System.out.println("measuring SISAP " + file + " - "
				+ (!fourPoint ? "no " : "") + "four point");
		//
		// int noOfQueries = 1000;
		int noOfQueries = tc.getDataCopy().size() / 10;
		System.out.println(noOfQueries + "\tqueries");
		tc.setSizes(noOfQueries, 0);

		CountedMetric<CartesianPoint> cm = new CountedMetric<>(tc.metric());
		List<NaryExclusionFactory<CartesianPoint, ?, ?>> excMechs = getSATMechanismsForPaper(
				tc, cm, fourPoint, satOut);
		excMechs.addAll(getHPTMechanismsForPaper(tc, cm, fourPoint, satOut));

		cm.reset();

		measure(tc, cm, excMechs, iterations);

	}

	@SuppressWarnings("boxing")
	private static void measure(TestContext tc,
			CountedMetric<CartesianPoint> cm,
			List<NaryExclusionFactory<CartesianPoint, ?, ?>> excMechs,
			int iterations) {
		
		Map<String, Measurements> measurements = new TreeMap<>();
		for (NaryExclusionFactory<CartesianPoint, ?, ?> ef : excMechs) {
			measurements.put(ef.getName(), new Measurements("buildDists",
					"queryDists_t0", "queryDists_t1", "queryDists_t2",
					"buildTime", "queryTime_t0", "queryTime_t1",
					"queryTime_t2", "treeDepth"));
		}

		cm.reset();
		int testTreeSize = -1;
		int testResSize = -1;

		for (int i : new Range(0, iterations)) {
			for (NaryExclusionFactory<CartesianPoint, ?, ?> mech : excMechs) {

				long t0 = System.currentTimeMillis();
				final List<CartesianPoint> dataCopy = tc.getDataCopy();
				assert Util_ISpaper.isSet(dataCopy) : "data isn't a set!";

				NarySearchTree<CartesianPoint, ?, ?> tree = new NarySearchTree<>(
						dataCopy, mech);

				Measurements m = measurements.get(mech.getName());
				m.addCount("buildDists", cm.reset());
				m.addCount("buildTime", (int) (System.currentTimeMillis() - t0));
				m.addCount("treeDepth", tree.depth());

				if (testTreeSize == -1) {
					testTreeSize = tree.cardinality();
				} else {
					assert testTreeSize == tree.cardinality() : mech.getName()
							+ " inconsistent size";
				}

				List<CartesianPoint> res = new ArrayList<>();
				for (int thresh : Range.range(0, 3)) {
					t0 = System.currentTimeMillis();
					for (CartesianPoint q : tc.getQueries()) {
						res.addAll(tree.thresholdSearch(q,
								tc.getThresholds()[thresh]));
					}
					m.addCount("queryTime_t" + thresh,
							(int) (System.currentTimeMillis() - t0));
					m.addCount("queryDists_t" + thresh, cm.reset());
				}
				if (testResSize == -1) {
					testResSize = res.size();
				} else {
					assert testResSize == res.size() : mech.getName()
							+ " inconsistent results return (" + testResSize
							+ ":" + res.size() + ")";
				}
			}
			printHeaderRow();
			for (NaryExclusionFactory<?, ?, ?> ef : excMechs) {
				Measurements m = measurements.get(ef.getName());
				printf(ef.getName());
				for (String s : criteria) {
					print(m.getMean(s));
					print(m.getSD(s));
					print(m.getStdErrorOfMean(s));
				}
				System.out.println();
			}
			System.out.println("after iteration " + i);
		}

	}

	private static void printHeaderRow() {
		printf("name");
		for (String s : criteria) {
			print(s + "_mean");
			print(s + "_sd");
			print(s + "_sderr");
		}
		System.out.println();

	}

	private static void print(Object s) {
		System.out.print("\t" + s);
	}

	private static void printf(Object s) {
		System.out.print(s);
	}
}
