package testloads;

import java.util.List;
import java.util.Map;

import searchStructures.GHTree;
import searchStructures.SearchIndex;
import searchStructures.SerialSearch;
import testloads.TestLoad.SisapFile;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.CartesianPointMetrics;
import dataPoints.cartesian.TriDiscrim;

public class NaiveTester {

	public static void main(String[] a) throws Exception {
		printSisapThresholds();
		// printGeneratedEuclideanThresholds();
		// quickGHTtest(d);
	}

	protected static void printSisapThresholds() throws Exception {
		final int querySetSize = 100;

		SisapFile[] files = { SisapFile.colors, SisapFile.nasa };
		for (SisapFile file : files) {
			TestLoad data = new TestLoad(file);
			List<CartesianPoint> qs = data.getQueries(querySetSize);
			System.out.println("test thresholds in " + file);
			double[] percent = { 0.0001, 0.000316, 0.001, 0.00316, 0.01 };
			int[] rets = new int[percent.length];
			for (int i = 0; i < percent.length; i++) {
				rets[i] = (int) (data.getDataCopy().size() * percent[i]);
				System.out.println("setting a " + file + " threshold to "
						+ rets[i] + " value returns");
			}
			printThresholds(data, qs, rets);
		}
	}

	protected static void printGeneratedEuclideanThresholds() {
		final int dataSize = 1000 * 1000;
		final int querySetSize = 100;

		int[] dims = { 6, 8, 10, 12, 14 };
		for (int dim : dims) {
			TestLoad d = new TestLoad(dim, dataSize + querySetSize, false);
			List<CartesianPoint> qs = d.getQueries(querySetSize);
			int[] rets = { 1, 2, 4, 8, 16, 32 };
			System.out.println("test thresholds in " + dim + " dimensions");
			printThresholds(d, qs, rets);
		}
	}

	private static void printThresholds(TestLoad load,
			List<CartesianPoint> queries, int[] rets) {
		Map<String, Metric<CartesianPoint>> ms = new CartesianPointMetrics<>();
		// for (String s : ms.keySet()) {
		// System.out.println(s);
		// }
		System.out.print("results:");
		for (int i : rets) {
			System.out.print("\t" + i);
		}
		System.out.println();
		for (String s : ms.keySet()) {
			Metric<CartesianPoint> m = ms.get(s);
			List<Double> thresh = load.findThresholdByPercent(m, queries, rets,
					0.2);
			System.out.print(s);
			for (double d : thresh) {
				System.out.print("\t" + d);
			}
			System.out.println();
		}
	}

	protected static void quickGHTtest(List<CartesianPoint> d) {
		// final double threshold = 0.08;
		// final double threshold = 0.22;
		// final double threshold = 0.45;
		final double threshold = 0.08;

		CartesianPoint query = d.get(0);
		d.remove(0);

		CountedMetric<CartesianPoint> euc = new CountedMetric<>(
				new TriDiscrim<>());

		SearchIndex<CartesianPoint> serial = new SerialSearch<>(d, euc);

		List<CartesianPoint> x = serial.thresholdSearch(query, threshold);
		System.out.println("done:" + x.size() + ":" + euc.reset());

		testIndex(d, query, euc, threshold);
	}

	protected static void testIndex(List<CartesianPoint> d,
			CartesianPoint query, CountedMetric<CartesianPoint> euc,
			final double threshold) {
		GHTree<CartesianPoint> ght = new GHTree<>(d, euc);

		System.out.print("nothing enabled: ");
		testGHT(query, euc, threshold, ght);

		ght.setCrExclusionEnabled(true);
		System.out.print("cr enabled: ");
		testGHT(query, euc, threshold, ght);

		ght.setCrExclusionEnabled(false);
		ght.setVorExclusionEnabled(true);
		System.out.print("vor enabled: ");
		testGHT(query, euc, threshold, ght);

		ght.setCrExclusionEnabled(true);
		System.out.print("both enabled: ");
		testGHT(query, euc, threshold, ght);

		ght.setCosExclusionEnabled(true);
		ght.setCrExclusionEnabled(false);
		ght.setVorExclusionEnabled(false);
		System.out.print("just cos enabled: ");
		testGHT(query, euc, threshold, ght);

		ght.setCrExclusionEnabled(true);
		System.out.print("cos and cr enabled: ");
		testGHT(query, euc, threshold, ght);

		ght.setVorExclusionEnabled(true);
		ght.setCrExclusionEnabled(false);
		System.out.print("cos and vor enabled: ");
		testGHT(query, euc, threshold, ght);

		ght.setCrExclusionEnabled(true);
		System.out.print("all enabled: ");
		testGHT(query, euc, threshold, ght);
	}

	protected static void testGHT(CartesianPoint query,
			CountedMetric<CartesianPoint> euc, final double threshold,
			GHTree<CartesianPoint> ght) {
		euc.reset();
		List<CartesianPoint> y = ght.thresholdSearch(query, threshold);
		System.out.println("done:" + y.size() + ":" + euc.reset());
	}

}
