package searchStructures.experimental.fplsh;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import testloads.TestContext;
import testloads.TestContext.Context;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannon;

public class ClassicLSTest {

	private static double[] colorsEucBounds = { 0, 1.4050343264699512 };
	private static double[] colorsJsdBounds = { 0.0, 0.9953048714864924 };

	// private static double[] colorsJsdBounds = { 0.008915130970081267,
	// 0.9953048714864924 };

	public static void main(String[] a) throws Exception {
		// TestContext tc = new TestContext(Context.colors);
		// // 4472 choose 2 is close to a million, giving 10^{-3} result ratio
		// tc.setSizes(4473, 1000);
		// Metric<CartesianPoint> jsd = new JensenShannon<>(false, true);
		// double[] bounds = getBounds(tc.getQueries(), jsd);
		// for (double d : bounds) {
		// System.out.println(d);
		// }
		//
		// List<CartesianPoint> refs = Util_ISpaper.getFFT(tc.getRefPoints(),
		// tc.metric(), 8);
		// LSFunction<CartesianPoint> ls = new AbsPermLSF<>(refs, tc.metric());
		// // LSFunction<CartesianPoint> ls = new ThreePointLSF<>(refs,
		// // tc.metric(),
		// // true);
		// // LSFunction<CartesianPoint> ls = new VoronoiLSF<>(refs,
		// tc.metric());
		// ls.setSample(tc.getData().subList(0, 1000));
		//
		// doBitOutput(tc, ls);
		// // doWordOutput(tc, ls);

		generateBatchHits();
	}

	private static void doBitOutput(TestContext tc,
			LSFunction<CartesianPoint> ls) {
		double[][] histo = getBitHistogram(tc.getQueries(), tc.metric(), ls);
		for (double[] ds : histo) {
			for (double d : ds) {
				System.out.print(d + "\t");
			}
			System.out.println();
		}
	}

	private static void doWordOutput(TestContext tc,
			LSFunction<CartesianPoint> ls) {
		double[] histo = getWordHistogram(tc.getQueries(), tc.metric(), ls);
		for (double d : histo) {
			System.out.println(d);
		}
	}

	private static double[][] getBitHistogram(List<CartesianPoint> queries,
			Metric<CartesianPoint> metric, LSFunction<CartesianPoint> ls) {
		int[] histo = new int[100];
		int[][] hits = new int[100][8];

		for (int i : Range.range(0, queries.size() - 1)) {
			List<Integer> v1 = ls.getBitClusters(queries.get(i), 1, 8);
			for (int j : Range.range(i + 1, queries.size())) {
				double d = metric.distance(queries.get(i), queries.get(j));
				int bucket = getBucket(d);
				List<Integer> v2 = ls.getBitClusters(queries.get(j), 1, 8);
				histo[bucket]++;
				for (int x : Range.range(0, 8)) {
					if (v1.get(x) == v2.get(x)) {
						hits[bucket][x]++;
					}
				}

			}
		}
		double[][] probs = new double[100][8];
		for (int i : Range.range(0, 100)) {
			for (int x : Range.range(0, 8)) {
				probs[i][x] = hits[i][x] / (double) histo[i];
			}
		}
		return probs;
	}

	private static double[] getWordHistogram(List<CartesianPoint> queries,
			Metric<CartesianPoint> metric, LSFunction<CartesianPoint> ls) {
		int[] histo = new int[100];
		int[] hits = new int[100];

		for (int i : Range.range(0, queries.size() - 1)) {
			List<Integer> v1 = ls.getBitClusters(queries.get(i), 8, 1);
			for (int j : Range.range(i + 1, queries.size())) {
				double d = metric.distance(queries.get(i), queries.get(j));
				int bucket = getBucket(d);
				List<Integer> v2 = ls.getBitClusters(queries.get(j), 8, 1);
				histo[bucket]++;
				if (v1.get(0) == v2.get(0)) {
					hits[bucket]++;
				}

			}
		}
		double[] probs = new double[100];
		for (int i : Range.range(0, 100)) {
			probs[i] = hits[i] / (double) histo[i];
		}
		return probs;
	}

	private static int[][] getWordHistograms(List<CartesianPoint> queries,
			Metric<CartesianPoint> metric, List<LSFunction<CartesianPoint>> lss) {
		int[][] histos = new int[100][lss.size() + 1];

		for (int lsFunc : Range.range(0, lss.size())) {
			LSFunction<CartesianPoint> ls = lss.get(lsFunc);
			for (int i : Range.range(0, queries.size() - 1)) {
				List<Integer> v1 = ls.getBitClusters(queries.get(i), 8, 1);
				for (int j : Range.range(i + 1, queries.size())) {
					double d = metric.distance(queries.get(i), queries.get(j));
					int bucket = getBucket(d);
					List<Integer> v2 = ls.getBitClusters(queries.get(j), 8, 1);
					if (lsFunc == 0) {
						histos[bucket][0]++;
					}
					if (v1.get(0) == v2.get(0)) {
						histos[bucket][lsFunc + 1]++;
					}
				}
			}
		}
		return histos;
	}

	private static int getBucket(double d) {
		double maxFactor = 100 / colorsJsdBounds[1];
		int thisDub = (int) Math.floor(d * maxFactor);
		if (thisDub == 100) {
			thisDub--;
		}
		return thisDub;
	}

	private static double[] getBounds(List<CartesianPoint> queries,
			Metric<CartesianPoint> metric) {
		double min = Double.MAX_VALUE;
		double max = 0;
		int eqs = 0;
		for (int i : Range.range(0, queries.size() - 1)) {
			for (int j : Range.range(i + 1, queries.size())) {
				double d = metric.distance(queries.get(i), queries.get(j));
				if (d != 0) {
					min = Math.min(min, d);
					max = Math.max(max, d);
				} else {
					eqs++;
				}
			}
		}
		double[] res = { min, max };
		System.out.println("there are " + eqs + " eqs");
		return res;
	}

	static void generateBatchHits() throws Exception {

		final TestContext tc = new TestContext(Context.colors);
		// 4472 choose 2 is close to ten million
		tc.setSizes(4473, 1000);

		// also for now have to change the bounds in getBucket... nasty but need
		// to remember
		Metric<CartesianPoint> metric = new JensenShannon<>(false, true);

		List<CartesianPoint> refs = Util_ISpaper.getFFT(tc.getRefPoints(),
				metric, 8);

		List<LSFunction<CartesianPoint>> fs = getFuncs(refs, tc.getData()
				.subList(0, 1000), metric);

		int[][] histos = getWordHistograms(tc.getQueries(), metric, fs);

		PrintWriter pw = new PrintWriter(
				"/Users/newrichard/Dropbox/sisapFourPointPaper/sisap17approxPaper/output/jsd_fft.csv");
		pw.print("hist,");
		for (LSFunction<CartesianPoint> f : fs) {
			pw.print(f.getName() + ",");
		}
		pw.println();

		for (int[] is : histos) {
			StringBuffer sb = new StringBuffer();
			for (int i : is) {
				sb.append(i + ",");
			}
			pw.println(sb.substring(0, sb.length() - 1).toString());
		}
		pw.close();

	}

	static List<LSFunction<CartesianPoint>> getFuncs(List<CartesianPoint> refs,
			List<CartesianPoint> sampleData, Metric<CartesianPoint> metric) {
		List<LSFunction<CartesianPoint>> res = new ArrayList<>();

		res.add(new AbsPermLSF<>(refs, metric));
		res.add(new FourPointLSF<>(refs, metric));
		res.add(new PivotLSF<>(refs, metric));
		res.add(new SingleSimplexLSF<>(refs, metric));
		res.add(new ThreePointLSF<>(refs, metric, true));
		res.add(new ThreePointLSF<>(refs, metric, false));
		res.add(new VoronoiLSF<>(refs, metric));

		for (LSFunction<CartesianPoint> f : res) {
			f.setSample(sampleData);
		}
		return res;
	}
}
