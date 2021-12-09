package testloads;

import java.util.Map;

import searchStructures.Tester;
import searchStructures.Tester.PowerTestResult;
import searchStructures.Tester.TestResult;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannonViaSed;
import dataPoints.cartesian.TriDiscrim;

public class HilbertPaper {

	public static void main(String[] args) throws Exception {

		Metric<CartesianPoint> euc = new Euclidean<>();
		Metric<CartesianPoint> tri = new TriDiscrim<>();
		Metric<CartesianPoint> jsd = new JensenShannonViaSed<>();
		Metric<CartesianPoint> metric = euc;
		// String[] indices = { "ght_v", "ght_h", "gmht_v", "gmht_h", "bghm_v",
		// "bghm_h", "bvpt" };
		// testSats(metric);
		// getPower(euc, false);
		// getPower(euc, false);
		// getPowerForLatexTable(euc);
		// System.out.println("\\hline\\n");
		// getPowerForLatexTable(jsd);
		// getPowerForLatexTable(tri);

		// testSats(euc);

		// getIdimsAndThresholdsForLatexTable(euc);
		// getIdimsAndThresholdsForLatexTable(jsd);
		// getIdimsAndThresholdsForLatexTable(tri);

		// testHTreesForPaper(euc);
		// testHTreesForPaper(tri);

		briefTestSomeStructures(1000 * 1000, euc, "temp1", "temp2");
	}

	protected static void getPower(Metric<CartesianPoint> metric,
			boolean gaussian) {
		int dataSize = 1000 * 1000;
		int pivotSelections = 400;
		for (int dim : CartesianThresholds.dims) {
			Tester t = new Tester(dim, dataSize, 2 * pivotSelections, 1,
					gaussian);
			double[] threshes = t.getThresholds(metric);
			final double idim = t.getIdim(metric);
			for (int th = 0; th < threshes.length; th++) {
				PowerTestResult r = t.testExclusionPower(metric, threshes[th]);
				System.out.print(metric.getMetricName() + "_" + dim + "_"
						+ CartesianThresholds.perMil[th]
						+ (gaussian ? "_g" : "_n"));
				System.out.print("\t" + idim);
				System.out.print("\t" + perCent(dataSize, r.cosExcs));
				System.out.print("\t" + perCent(dataSize, r.hypExcs));
				System.out.print("\t" + perCent(dataSize, r.pivExcs));
				System.out.print("\t" + perCent(dataSize, r.cosExcsStd));
				System.out.print("\t" + perCent(dataSize, r.hypExcsStd));
				System.out.println("\t" + perCent(dataSize, r.pivExcsStd));
			}
		}
	}

	protected static void getPowerForLatexTable(Metric<CartesianPoint> metric) {
		int dataSize = 1000 * 100;
		int pivotSelections = 400;
		for (int dim : CartesianThresholds.dims) {
			Tester t = new Tester(dim, dataSize, 2 * pivotSelections, 1, false);
			double[] threshes = t.getThresholds(metric);
			final double idim = t.getIdim(metric);
			int[] ts = { 0, 2, 4 };
			String[] fields = new String[11];
			fields[0] = metric.getMetricName() + "\\_" + dim;
			fields[1] = roundDouble(idim, 3);
			for (int th : ts) {
				PowerTestResult r = t.testExclusionPower(metric, threshes[th]);
				fields[2 + (th / 2)] = perCent(dataSize, r.hypExcs);
				// fields[2 + (th / 2)] += " ("
				// + Float.toString(perCent(dataSize, pivotSelections,
				// r.hypExcsStd)) + ")";
				fields[5 + (th / 2)] = perCent(dataSize, r.cosExcs);
				// fields[5 + (th / 2)] += " ("
				// + Float.toString(perCent(dataSize, pivotSelections,
				// r.cosExcsStd)) + ")";
				fields[8 + (th / 2)] = perCent(dataSize, r.pivExcs);
				// fields[8 + (th / 2)] += " ("
				// + Float.toString(perCent(dataSize, pivotSelections,
				// r.pivExcsStd)) + ")";
			}
			boolean first = true;
			for (String s : fields) {
				System.out.print((!first ? "&" : "") + s + "\t");
				first = false;
			}
			System.out.println("\\\\\n\\hline\n");
		}

	}

	protected static String roundDouble(final double d, int noOfPlaces) {
		if (d >= 10000 || noOfPlaces > 5) {
			throw new RuntimeException("round double is giving a bad output!");
		}
		final double mult = Math.pow(10, noOfPlaces);
		double d1 = Math.round(d * mult);
		float f = (float) (d1 / mult);
		String fl = Float.toString(f);
		while (fl.length() < noOfPlaces + 2) {
			fl += "0";
		}
		return fl;
	}

	protected static void getIdimsAndThresholdsForLatexTable(
			Metric<CartesianPoint> metric) {
		int dataSize = 1000 * 100;
		for (int dim : CartesianThresholds.dims) {
			Tester t = new Tester(dim, dataSize, 1, 1, false);
			double[] threshes = t.getThresholds(metric);
			final double idim = t.getIdim(metric);

			System.out.print(metric.getMetricName() + "\\_" + dim);
			System.out.print("\t&" + roundDouble(idim, 2));
			for (double th : threshes) {
				System.out.print("\t&" + roundDouble(th, 5));
			}
			System.out.println("\\\\\n\\hline\\n");
		}

	}

	static public String perCent(int data, double results) {
		return roundDouble(results * 100 / data, 2);
	}

	protected static void testSats(Metric<CartesianPoint> metric) {
		String[] indices = { "sat_v", "sat_h", "gmht_v", "gmht_h" };
		for (int dim : CartesianThresholds.dims) {
			Tester t = new Tester(dim, 1000 * 10, 1000, 100, false);

			for (String ind : indices) {
				Map<String, TestResult> res = t.testSomeIndices(metric, ind);
				for (String s : res.keySet()) {
					TestResult tr = res.get(s);
					System.out.println(tr);
				}
			}
		}
	}

	protected static void briefTestSomeStructures(int dataSize,
			Metric<CartesianPoint> metric, String... indices) {
		// String[] indices = { "ght_v", "gmht_v", "ght_h", "gmht_h" };
		int[] dimsToTest = { 6, 8, 10, 12, 14 };//
		int[] thresholdsToPrint = { 0, 2, 4 };
		for (int dim : dimsToTest) {
			Tester t = new Tester(dim, dataSize, 100, 1, false);
			Map<String, TestResult> res = t.testSomeIndices(metric, indices);

			System.out.print(metric.getMetricName() + "\\_" + dim);
			for (String index : indices) {
				TestResult thisOne = res.get(index);
				System.out.print("\t" + thisOne.getIndexName());
				for (int th : thresholdsToPrint) {
					System.out.print("\t&"
							+ perCent(dataSize, thisOne.meanDists[th]));
				}
			}

			System.out.println("\\\\");
			System.out.println("\\hline");
		}
	}
}
