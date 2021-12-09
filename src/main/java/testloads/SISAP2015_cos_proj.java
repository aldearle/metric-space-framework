package testloads;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import searchStructures.GHTree;
import searchStructures.SATGeneric;
import searchStructures.SATGeneric.Strategy;
import searchStructures.SearchIndex;
import searchStructures.SerialSearch;
import searchStructures.VPTree;
import testloads.TestLoad.SisapFile;
import coreConcepts.CountedMetric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.SEDByComplexity;

public class SISAP2015_cos_proj {

	public static final void main(String[] a) throws Exception {
		// printSisapSeriesDistances();
		// testResults();
		// testSatSemantics();
		testSat();
	}

	private static void testSatSemantics() throws Exception {
		final SisapFile sisapFile = SisapFile.nasa;
		TestLoad testLoad = new TestLoad(sisapFile);

		System.out
				.println("test load size is " + testLoad.getDataCopy().size());
		List<CartesianPoint> queries = testLoad.getQueries(100);

		System.out
				.println("test load size is " + testLoad.getDataCopy().size());
		CountedMetric<CartesianPoint> metric = new CountedMetric<>(
				new Euclidean<>());

		SerialSearch<CartesianPoint> ser = new SerialSearch<>(
				testLoad.getDataCopy(), metric);
		VPTree<CartesianPoint> vpt = new VPTree<>(testLoad.getDataCopy(),
				metric);
		System.out
				.println("test load size is " + testLoad.getDataCopy().size());
		SATGeneric<CartesianPoint> sat = new SATGeneric<>(
				testLoad.getDataCopy(), metric, SATGeneric.Strategy.original);

		System.out
				.println("test load size is " + testLoad.getDataCopy().size());
		System.out.println("sat size is " + sat.size());

		int no = 0;
		for (CartesianPoint q : queries) {
			testReturns(ser, sat, q, 0.5);
			System.out.println("done " + ++no);
		}
	}

	private static void testSat() throws Exception {
		final SisapFile sisapFile = SisapFile.colors;
		TestLoad testLoad = new TestLoad(sisapFile);

		List<CartesianPoint> testData = testLoad
				.getQueries(testLoad.dataSize() / 10);

		CountedMetric<CartesianPoint> metric = new CountedMetric<>(
				new Euclidean<>());

		VPTree<CartesianPoint> vpt = new VPTree<>(testLoad.getDataCopy(),
				metric);
		SATGeneric<CartesianPoint> sat = new SATGeneric<>(
				testLoad.getDataCopy(), metric, SATGeneric.Strategy.distal);
		GHTree<CartesianPoint> ght = new GHTree(testLoad.getDataCopy(), metric);
		ght.setCrExclusionEnabled(true);
		ght.setVorExclusionEnabled(true);

		System.out.println("metric\tthesh1\tthresh2\tthresh3");
		System.out.print("vpt");
		List<Set<CartesianPoint>> results = printMetricOverSisapSet(sisapFile,
				testData, metric, vpt);
		sat.setCosineTestEnabled(true);
		System.out.print("sat_dist_c");
		checkLists(results,
				printMetricOverSisapSet(sisapFile, testData, metric, sat));
		sat.setCosineTestEnabled(false);
		System.out.print("sat_dist_v");
		checkLists(results,
				printMetricOverSisapSet(sisapFile, testData, metric, sat));

		SATGeneric<CartesianPoint> sat2 = new SATGeneric<>(
				testLoad.getDataCopy(), metric, SATGeneric.Strategy.original);

		sat2.setCosineTestEnabled(true);
		System.out.print("sat_orig_c");
		checkLists(results,
				printMetricOverSisapSet(sisapFile, testData, metric, sat2));
		sat2.setCosineTestEnabled(false);
		System.out.print("sat_orig_v");
		checkLists(results,
				printMetricOverSisapSet(sisapFile, testData, metric, sat2));

		System.out.print("ght_v");
		checkLists(results,
				printMetricOverSisapSet(sisapFile, testData, metric, ght));

		ght.setCosExclusionEnabled(true);
		System.out.print("ght_c");
		checkLists(results,
				printMetricOverSisapSet(sisapFile, testData, metric, ght));
	}

	public static void testReturns(SearchIndex<CartesianPoint> index1,
			SearchIndex<CartesianPoint> index2, CartesianPoint query,
			double threshold) {
		Set<CartesianPoint> s1 = new HashSet<>();
		s1.addAll(index1.thresholdSearch(query, threshold));
		Set<CartesianPoint> s2 = new HashSet<>();
		s2.addAll(index2.thresholdSearch(query, threshold));

		if (s1.size() != s2.size()) {
			System.out.println("s1 size is " + s1.size() + " and s2 size is "
					+ s2.size());
		}
		int errors = 0;
		for (CartesianPoint p : s1) {
			if (!s2.contains(p)) {
				errors++;
			}
		}
		if (errors != 0) {
			System.out.println("there were " + errors + " individual errors");
		}
	}

	protected static <T> List<Set<T>> printMetricOverSisapSet(
			final SisapFile colors, List<T> queries, CountedMetric<T> metric,
			SearchIndex<T> sat) {
		metric.reset();

		List<Set<T>> res = new ArrayList<>();
		for (double t : TestLoad.getSisapThresholds(colors)) {
			for (T p : queries) {
				Set<T> r = new HashSet<>();
				r.addAll(sat.thresholdSearch(p, t));
				res.add(r);
			}
			int nDistances = metric.reset();
			double avg = ((double) nDistances) / queries.size();

			System.out.print("\t" + avg);
		}
		System.out.println();
		return res;
	}

	protected static void testResults() throws Exception {
		final int datasize = 1000 * 100;
		final int noOfQueries = 90;
		final int dims = 10;

		// final double threshold = 0.235107454; // Euclidean, 10 dims
		// final double threshold = 0.420376768; // Euclidean, 14 dim
		// final double threshold = 0.0732813023760952; // Cosine, 10 dims
		// final double threshold = 0.29; // Euc 8 dim for SISAP test
		final double threshold = 0.061688694; // SED for 10 dim

		TestLoad tl = new TestLoad(dims, datasize + noOfQueries, false);
		List<CartesianPoint> qs = tl.getQueries(noOfQueries);
		final CountedMetric<CartesianPoint> metric = new CountedMetric<>(
				new SEDByComplexity<>());

		GHTree<CartesianPoint> ght = new GHTree<>(tl.getDataCopy(), metric);
		VPTree<CartesianPoint> vpt = new VPTree<>(tl.getDataCopy(), metric);

		metric.reset();
		// get results with no exclusion enabled
		System.out.print("nothing enabled");
		List<Set<CartesianPoint>> l = printNoOfDists(threshold, qs, metric, ght);
		// System.out.println("dim\tbuild dists\tqDists");
		ght.setCrExclusionEnabled(true);
		System.out.print("cr enabled");
		checkLists(l, printNoOfDists(threshold, qs, metric, ght));

		ght.setVorExclusionEnabled(true);
		System.out.print("vor/cs enabled");
		checkLists(l, printNoOfDists(threshold, qs, metric, ght));

		ght.setCosExclusionEnabled(true);
		ght.setVorExclusionEnabled(false);
		System.out.print("cos/cs enabled");
		checkLists(l, printNoOfDists(threshold, qs, metric, ght));

		ght.setCrExclusionEnabled(false);
		ght.setCosExclusionEnabled(false);

		ght.setVorExclusionEnabled(true);
		System.out.print("vor enabled");
		checkLists(l, printNoOfDists(threshold, qs, metric, ght));

		ght.setCosExclusionEnabled(true);
		ght.setVorExclusionEnabled(false);
		System.out.print("cos enabled");
		checkLists(l, printNoOfDists(threshold, qs, metric, ght));

		System.out.print("vpt");
		checkLists(l, printNoOfDists(threshold, qs, metric, vpt));

		SATGeneric<CartesianPoint> sat = new SATGeneric<>(tl.getDataCopy(),
				metric, Strategy.original);
		metric.reset();
		System.out.print("sat");
		checkLists(l, printNoOfDists(threshold, qs, metric, sat));

		System.out.println("there were " + getNoOfRes(l) + " results in all");
	}

	private static int getNoOfRes(List<Set<CartesianPoint>> l) {
		int res = 0;
		for (Set<?> s : l) {
			res += s.size();
		}
		return res;
	}

	private static void checkLists(List<Set<CartesianPoint>> l1,
			List<Set<CartesianPoint>> l2) throws Exception {
		try {
			if (l1.size() != l2.size()) {
				throw new Exception("lists are different sizes");
			} else {
				for (int i = 0; i < l1.size(); i++) {
					Set<CartesianPoint> s1 = l1.get(i);
					Set<CartesianPoint> s2 = l2.get(i);
					if (!(s1.size() == s2.size())) {
						throw new Exception("sets have different sizes ("
								+ s1.size() + ", " + s2.size()
								+ ") at position " + i);
					}
					for (CartesianPoint p1 : s1) {
						if (!s2.contains(p1)) {
							throw new Exception(
									"sets have different members at position "
											+ i);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void printSisapSeriesDistances() throws Exception {

		SisapFile[] files = { SisapFile.colors, SisapFile.nasa };
		for (SisapFile file : files) {
			System.out.println(file);
			TestLoad data = new TestLoad(file);
			List<CartesianPoint> qs1 = data.getQueries(data.dataSize() / 10);

			CountedMetric<CartesianPoint> euc = new CountedMetric<>(
					new Euclidean<>());

			VPTree<CartesianPoint> vpt = new VPTree<>(data.getDataCopy(), euc);
			// GHTree<CartesianPoint> ght = new GHTree<>(data.getDataCopy(),
			// euc);
			SATGeneric<CartesianPoint> sat = new SATGeneric<>(
					data.getDataCopy(), euc, Strategy.distal);

			// ght.setCrExclusionEnabled(true);

			for (double t : TestLoad.getSisapThresholds(file)) {
				printSatSearchCosts(qs1, euc, sat, vpt, t);
				// printSearchCosts(qs1, euc, ght, sat, t);
				// printSearchTimes(qs1, vpt, ght, t);
				System.out.println();
			}
		}
	}

	/**
	 * prints a series of tests suitable for plotting via matlab
	 */
	@SuppressWarnings("boxing")
	private static void printGeneratedSeriesDistances() {
		int datasize = 1000 * 1000;
		int noOfQueries = 1000;

		CartesianThresholds ct = new CartesianThresholds();
		printHeaders(ct);
		for (int dim : CartesianThresholds.dims) {
			System.out.print(dim);
			// construct data load and indexes
			TestLoad tl = new TestLoad(dim, datasize + noOfQueries, false);
			List<CartesianPoint> qs = tl.getQueries(noOfQueries);
			final CountedMetric<CartesianPoint> metric = new CountedMetric<>(
					new Euclidean<>());

			printCountForTestLoad(ct, dim, tl, qs, metric);
		}

	}

	protected static void printCountForTestLoad(CartesianThresholds ct,
			int dim, TestLoad tl, List<CartesianPoint> qs,
			final CountedMetric<CartesianPoint> metric) {
		SerialSearch<CartesianPoint> ser = new SerialSearch<>(tl.getDataCopy(),
				metric);
		GHTree<CartesianPoint> ght = new GHTree<>(tl.getDataCopy(), metric);
		ght.setCrExclusionEnabled(true);
		VPTree<CartesianPoint> vpt = new VPTree<>(tl.getDataCopy(), metric);
		metric.reset();

		for (int perMil : CartesianThresholds.perMil) {
			double threshold = ct.get(dim).get("euc").get(perMil);

			printSearchCosts(qs, metric, ght, vpt, threshold);
		}
		System.out.println();
	}

	protected static void printSearchCosts(List<CartesianPoint> qs,
			final CountedMetric<CartesianPoint> metric,
			GHTree<CartesianPoint> ght, SearchIndex<CartesianPoint> benchmark,
			double threshold) {
		System.out.print("\t" + ((double) metric.reset() / qs.size()));

		doQueries(benchmark, qs, threshold);
		System.out.print("\t" + ((double) metric.reset() / qs.size()));

		ght.setCosExclusionEnabled(false);
		ght.setVorExclusionEnabled(true);
		doQueries(ght, qs, threshold);
		System.out.print("\t" + ((double) metric.reset() / qs.size()));

		ght.setCosExclusionEnabled(true);
		ght.setVorExclusionEnabled(false);
		doQueries(ght, qs, threshold);
		System.out.print("\t" + ((double) metric.reset() / qs.size()));

		ght.setVorExclusionEnabled(true);
		ght.setCosExclusionEnabled(true);
		doQueries(ght, qs, threshold);
		System.out.print("\t" + ((double) metric.reset() / qs.size()));
	}

	protected static void printSatSearchCosts(List<CartesianPoint> qs,
			final CountedMetric<CartesianPoint> metric,
			SATGeneric<CartesianPoint> sat,
			SearchIndex<CartesianPoint> benchmark, double threshold) {

		metric.reset();

		doQueries(benchmark, qs, threshold);
		System.out.print("\t" + ((double) metric.reset() / qs.size()));

		sat.setCosineTestEnabled(false);
		doQueries(sat, qs, threshold);
		System.out.print("\t" + ((double) metric.reset() / qs.size()));

		sat.setCosineTestEnabled(true);
		doQueries(sat, qs, threshold);
		System.out.print("\t" + ((double) metric.reset() / qs.size()));
	}

	/**
	 * prints a series of tests suitable for plotting via matlab
	 */
	@SuppressWarnings("boxing")
	private static void printGeneratedSeriesTimes() {
		int datasize = 1000 * 1000;
		int noOfQueries = 1000;

		CartesianThresholds ct = new CartesianThresholds();
		printHeaders(ct);
		for (int dim : CartesianThresholds.dims) {
			System.out.print(dim);
			// construct data load and indexes
			TestLoad tl = new TestLoad(dim, datasize + noOfQueries, false);
			TestLoad tl2 = new TestLoad(dim, datasize + noOfQueries, false);
			List<CartesianPoint> qs = tl.getQueries(noOfQueries);
			List<CartesianPoint> qs2 = tl2.getQueries(noOfQueries);
			final CountedMetric<CartesianPoint> metric = new CountedMetric<>(
					new Euclidean<>());

			// SerialSearch<CartesianPoint> ser = new SerialSearch<>(tl,
			// metric);
			VPTree<CartesianPoint> vpt = new VPTree<>(tl.getDataCopy(), metric);
			GHTree<CartesianPoint> ght = new GHTree<>(tl2.getDataCopy(), metric);
			ght.setCrExclusionEnabled(true);
			metric.reset();

			for (int perMil : CartesianThresholds.perMil) {
				double threshold = ct.get(dim).get("euc").get(perMil);

				printSearchTimes(qs, vpt, ght, threshold);
			}
			System.out.println();
		}

	}

	protected static void printSearchTimes(List<CartesianPoint> qs,
			VPTree<CartesianPoint> vpt, GHTree<CartesianPoint> ght,
			double threshold) {
		long t0 = System.currentTimeMillis();

		System.out.print("\t" + (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();

		// warmup over vpt structure
		doQueries(vpt, qs, threshold);

		t0 = System.currentTimeMillis();
		doQueries(vpt, qs, threshold);
		System.out.print("\t" + (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();

		// warmup over ght structure
		doQueries(ght, qs, threshold);

		ght.setCosExclusionEnabled(false);
		ght.setVorExclusionEnabled(true);
		t0 = System.currentTimeMillis();
		doQueries(ght, qs, threshold);
		System.out.print("\t" + (System.currentTimeMillis() - t0));

		ght.setCosExclusionEnabled(true);
		ght.setVorExclusionEnabled(false);
		t0 = System.currentTimeMillis();
		doQueries(ght, qs, threshold);
		System.out.print("\t" + (System.currentTimeMillis() - t0));

		ght.setVorExclusionEnabled(true);
		ght.setCosExclusionEnabled(true);
		t0 = System.currentTimeMillis();
		doQueries(ght, qs, threshold);
		System.out.print("\t" + (System.currentTimeMillis() - t0));
	}

	private static <T> void doQueries(SearchIndex<T> vpt, List<T> qs,
			double threshold) {
		for (T q : qs) {
			List<T> r = vpt.thresholdSearch(q, threshold);
		}
	}

	private static void printHeaders(CartesianThresholds ct) {
		System.out.print("dim");
		for (int perMil : ct.get(CartesianThresholds.dims[0])
				.get(CartesianThresholds.metrics[0]).keySet()) {
			System.out.print("\tser_" + perMil);
			System.out.print("\tvpt_" + perMil);
			System.out.print("\tght_v_" + perMil);
			System.out.print("\tght_c_" + perMil);
			System.out.print("\tght_b_" + perMil);
		}
		System.out.println();
	}

	protected static List<Set<CartesianPoint>> countAverageDistances(
			final double threshold, List<CartesianPoint> qs,
			final CountedMetric<CartesianPoint> euc,
			SearchIndex<CartesianPoint> ght) {
		List<Set<CartesianPoint>> res = new ArrayList<>();
		for (CartesianPoint q : qs) {
			Set<CartesianPoint> r = new HashSet<>();
			r.addAll(ght.thresholdSearch(q, threshold));
			res.add(r);
			System.out.print("\t" + euc.reset());
		}
		System.out.println();
		return res;
	}

	protected static List<Set<CartesianPoint>> printNoOfDists(
			final double threshold, List<CartesianPoint> qs,
			final CountedMetric<CartesianPoint> euc,
			SearchIndex<CartesianPoint> ght) {
		List<Set<CartesianPoint>> res = new ArrayList<>();
		for (CartesianPoint q : qs) {
			Set<CartesianPoint> r = new HashSet<>();
			r.addAll(ght.thresholdSearch(q, threshold));
			res.add(r);
			System.out.print("\t" + euc.reset());
		}
		System.out.println();
		return res;
	}
}
