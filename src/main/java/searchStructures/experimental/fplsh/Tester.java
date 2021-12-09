package searchStructures.experimental.fplsh;

import java.util.ArrayList;
import java.util.List;

import testloads.TestContext;
import testloads.TestContext.Context;
import util.Measurements;
import util.OrderedList;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

public class Tester {

	private TestContext tc;
	private List<CartesianPoint[]> sims;
	private List<CartesianPoint[]> dissims;
	private double simThreshold;

	Tester(TestContext tc) {
		this.tc = tc;

		// this.sims = getSimilarPairs();
		// this.dissims = getDissimilarPairs();
		//
		// assert sims.size() == dissims.size() : sims.size() + " sims and "
		// + dissims.size() + " dissims";

		Measurements m = new Measurements("pivTP", "pivFP", "tppTP", "tppFP",
				"fppTP", "fppFP", "simTP", "simFP", "tppbTP", "tppbFP");

		while (!m.allDone(0.02)) {

			// List<CartesianPoint> refs = Util_ISpaper.getRandom(
			// tc.getRefPoints(), 8);
			List<CartesianPoint> refs = Util_ISpaper.getFFT(tc.getRefPoints(),
					tc.metric(), 16);

			LSFunction<CartesianPoint> bp1 = new SingleSimplexLSF<>(refs,
					tc.metric());
			LSFunction<CartesianPoint> bp2 = new PivotLSF<>(refs, tc.metric());
			LSFunction<CartesianPoint> bp3 = new FourPointLSF<>(refs,
					tc.metric());
			LSFunction<CartesianPoint> bp4 = new ThreePointLSF<>(refs,
					tc.metric(), true);
			LSFunction<CartesianPoint> bp5 = new ThreePointLSF<>(refs,
					tc.metric(), false);

			LSFunction[] funcs = { bp1, bp2, bp3, bp4, bp5 };
			for (LSFunction func : funcs) {

				func.setSample(getRandomPoints());

				boolean[][] goodhits = new boolean[sims.size()][2];
				boolean[][] badhits = new boolean[dissims.size()][2];

				int allGood = 0;
				int allBad = 0;

				int clusterSize = 8;
				int wordsSize = 1;

				for (int i : Range.range(0, sims.size())) {
					List<Integer> x0 = func.getBitClusters(sims.get(i)[0],
							clusterSize, wordsSize);
					List<Integer> y0 = func.getBitClusters(sims.get(i)[1],
							clusterSize, wordsSize);
					List<Integer> x1 = func.getBitClusters(dissims.get(i)[0],
							clusterSize, wordsSize);
					List<Integer> y1 = func.getBitClusters(dissims.get(i)[1],
							clusterSize, wordsSize);

					boolean goodHit = true;
					boolean badHit = true;
					for (int j : Range.range(0, wordsSize)) {
						if (x0.get(j) == y0.get(j)) {
							goodhits[i][j] = true;
						} else {
							goodHit = false;
						}
						if (x1.get(j) == y1.get(j)) {
							badhits[i][j] = true;
						} else {
							badHit = false;
						}
					}
					if (goodHit) {
						allGood++;
					}
					if (badHit) {
						allBad++;
					}

				}

				int[] totalGoodHits = new int[wordsSize];
				int[] totalBadHits = new int[wordsSize];
				for (int i : Range.range(0, sims.size())) {
					for (int j : Range.range(0, wordsSize)) {
						if (goodhits[i][j]) {
							totalGoodHits[j]++;
						}
						if (badhits[i][j]) {
							totalBadHits[j]++;
						}
					}
				}

				m.addCount(func.getName() + "TP", allGood);
				m.addCount(func.getName() + "FP", allBad);
			}
			m.spewResults();
		}
	}

	public static void main(String[] a) throws Exception {

		// final TestContext tc = new TestContext(Context.colors);
		// // 4472 choose 2 is close to a million, giving 10^{-3} result ratio
		// tc.setSizes(4473, 1000);
		//
		// Tester t = new Tester(tc);


	}

	/**
	 * @return returns the 1000 closest pairs from the 4473 \choose 2 queries
	 *         selected
	 */
	@SuppressWarnings("boxing")
	public List<CartesianPoint[]> getSimilarPairs() {
		OrderedList<CartesianPoint[], Double> ol = new OrderedList<>(1000);
		List<CartesianPoint[]> res = new ArrayList<>();
		final List<CartesianPoint> queries = this.tc.getQueries();
		for (int i : Range.range(0, queries.size() - 1)) {
			for (int j : Range.range(i + 1, queries.size())) {
				final double d = this.tc.metric().distance(queries.get(i),
						queries.get(j));
				if (d < this.tc.getThreshold()) {
					CartesianPoint[] pair = { queries.get(i), queries.get(j) };
					ol.add(pair, d);
					res.add(pair);
				}
			}
		}
		this.simThreshold = ol.getThreshold();
		return ol.getList();
	}

	/**
	 * @return returns 1000 random pairs which are not close by defined
	 *         thresholds
	 */
	public List<CartesianPoint[]> getDissimilarPairs() {
		List<CartesianPoint[]> res = new ArrayList<>();
		final List<CartesianPoint> queries = this.tc.getQueries();
		for (int i : Range.range(0, queries.size() - 1)) {
			for (int j : Range.range(i + 1, queries.size())) {
				if (res.size() < 1000) {
					final double d = this.tc.metric().distance(queries.get(i),
							queries.get(j));
					if (d > this.simThreshold) {
						CartesianPoint[] pair = { queries.get(i),
								queries.get(j) };
						res.add(pair);
					}
				}
			}
		}
		return res;
	}


	/**
	 * @return returns 1000 random pairs which are not close by defined
	 *         thresholds
	 */
	public List<CartesianPoint> getRandomPoints() {
		return Util_ISpaper.getRandom(this.tc.getData(), 1000);
	}
}
