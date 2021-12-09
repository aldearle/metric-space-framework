package searchStructures.experimental.fplsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import n_point_surrogate.SimplexExclusion;
import n_point_surrogate.SimplexND;
import supermetrics.Simplex1D;
import testloads.TestContext;
import util.Range;
import util.Util_ISpaper;
import dataPoints.cartesian.CartesianPoint;

public class NpointLSH {

	public static void main(String[] a) throws Exception {

		TestContext tc = new TestContext(TestContext.Context.colors);
		tc.setSizes(1000, 1000);
		List<CartesianPoint> data = tc.getQueries();

		int dim = 3;
		int noOfRefs = 10;
		List<CartesianPoint> refs = Util_ISpaper.getFFT(tc.getRefPoints(), tc.metric(),
				noOfRefs);

		/*
		 * using 3 dimensions chosen from n reference points so miss one out
		 * each time
		 */

		Map<String, Integer> hits = new HashMap<String, Integer>() {
			@Override
			public Integer put(String s, Integer i) {
				Integer r = super.put(s, i);
				if (r != null) {
					super.put(s, r + i);
				}
				return -1;
			}
		};

		int bitCounter = 0;
		int wordWidth = 12;
		int clusters = 10;
		List<Map<Integer, Integer>> hashMap = new ArrayList<>();

		boolean[][] bitMap = new boolean[data.size()][wordWidth * clusters];

		List<CartesianPoint> refi = new ArrayList<>();
		refi.add(null);
		refi.add(null);
		refi.add(null);
		for (int i : Range.range(0, refs.size() - 2)) {
			refi.set(0, refs.get(i));
			for (int j : Range.range(i + 1, refs.size() - 1)) {
				refi.set(1, refs.get(j));
				for (int k : Range.range(j + 1, refs.size())) {
					refi.set(2, refs.get(k));

					SimplexExclusion<CartesianPoint> sim = new SimplexND<>(dim,
							tc.metric(), refi);

					testSimplex(tc, data, sim, hits);
				}
			}
		}
		for (String s : hits.keySet()) {
			System.out.println(s + "\t" + hits.get(s));
		}

		// testByDimension(tc, data);

	}

	private static void testByDimension(TestContext tc,
			List<CartesianPoint> data) throws Exception {
		int dim = 1;
		List<CartesianPoint> refs = Util_ISpaper.getFFT(tc.getRefPoints(), tc.metric(),
				dim);
		SimplexExclusion<CartesianPoint> sim = null;
		if (dim == 1) {
			CartesianPoint[] r = { refs.get(0) };
			sim = new Simplex1D<>(1, tc.metric(), r);
		} else {
			sim = new SimplexND<>(dim, tc.metric(), refs);
		}

		testSimplex(tc, data, sim, null);
	}

	private static void testSimplex(TestContext tc, List<CartesianPoint> data,
			SimplexExclusion<CartesianPoint> sim, Map<String, Integer> hits) {
		double[][] surs = new double[data.size()][0];
		for (int i : Range.range(0, data.size())) {
			surs[i] = sim.formSimplex(data.get(i));
		}

		double[] mean = getMean(surs);
		double meanDistToMean = meanDist(mean, surs);

		int counted = 0;
		int correct = 0;
		for (int i : Range.range(0, data.size() - 1)) {
			for (int j : Range.range(i + 1, data.size())) {
				double dist = tc.metric().distance(data.get(i), data.get(j));
				if (dist < tc.getThreshold()) {
					hits.put(i + ";" + j, 0);
					counted++;
					double surDist1 = SimplexND.l2(surs[i], mean);
					double surDist2 = SimplexND.l2(surs[j], mean);
					boolean close1 = surDist1 < meanDistToMean;
					boolean close2 = surDist2 < meanDistToMean;

					final boolean match = close1 == close2;
					if (match) {
						hits.put(i + ";" + j, 1);
						correct++;
					}
					// System.out.println(dist + "\t" + match);
				}
			}
		}
		System.out.println(counted + ":" + correct + " ("
				+ (correct / (float) counted) + ")");
	}

	private static double meanDist(double[] mean, double[][] surs) {
		double acc = 0;
		for (double[] sur : surs) {
			acc += SimplexND.l2(mean, sur);
		}
		return acc / surs.length;
	}

	static double[] getMean(double[][] vecs) {
		double[] acc = new double[vecs[0].length];
		for (double[] p : vecs) {
			for (int i : Range.range(0, p.length)) {
				acc[i] += p[i];
			}
		}
		for (int i : Range.range(0, acc.length)) {
			acc[i] = acc[i] / vecs.length;
		}
		return acc;
	}
}
