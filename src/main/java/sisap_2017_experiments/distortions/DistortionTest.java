package sisap_2017_experiments.distortions;

import java.util.List;
import java.util.Set;

import sisap_2017_experiments.NdimSimplex;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannon;
import dataPoints.cartesian.PartDimEuclidean;

public class DistortionTest {
	public static class Distortion {
		/*
		 * ratio here should be original distance / surrogate distance so if you
		 * multiply a surrogate distance by it, you get the original back
		 * 
		 * if surrogate is lwb of original, all ratios are over 1.0; minRatio
		 * occurs when distances are closest to each other as a proportion so
		 * will typically be close to 1.0
		 * 
		 * max when they are furthest apart as a proportion so can be huge
		 * 
		 * so minRatio is the smallest number which, when multiplied by a
		 * surrogate distance, is less than or equal the original
		 * 
		 * so 1/minRatio is the largest number which, when multiplied by an
		 * original distance, is always less than the corresponding surrogate
		 * distance... ie r
		 * 
		 * and D is the smallest number such that r.D.sur(x',y') = d(x,y)
		 */
		double minRatio = Double.MAX_VALUE;
		double maxRatio = 0;

		/*
		 * so R is the smallest number such that r.sur(x',y') = d(x,y)
		 */
		double getR() {
			return minRatio;
		}

		/*
		 * and D is the smallest number such that r.D.sur(x',y') = d(x,y)
		 */
		double getD() {
			return maxRatio / minRatio;
		}
	}

	public static void main(String[] a) throws Exception {
		TestContext tc = new TestContext(Context.colors, 1100);
		tc.setSizes(100, 1000);

		int dim = tc.getQueries().get(0).getPoint().length;
		Metric<CartesianPoint> euc = new JensenShannon<>(false, true);

		System.out.println("dim\tsimD\tjlfD\tsimfMD\tjlfMD");
		for (int noOfRefPoints : Range.range(1, 50)) {
			double simAcc = 0;
			double jlfAcc = 0;
			double simMeanAcc = 0;
			double jlfMeanAcc = 0;
			int iters = 10;
			for (int i : Range.range(0, iters)) {
				List<CartesianPoint> refs = Util_ISpaper.getFFT(
						tc.getRefPoints(), euc, noOfRefPoints + 1);
				refs = Util_ISpaper.getRandom(tc.getRefPoints(),
						noOfRefPoints + 1);
				Set<Integer> dims = Util_ISpaper.getRandomInts(noOfRefPoints,
						dim);
				Metric<CartesianPoint> eucP = new PartDimEuclidean<>(dims);
				Metric<CartesianPoint> sim = getSimplexMetric(euc, refs);
				Distortion simD = getDistortion(tc.getQueries(), euc, sim);
				Distortion jlfD = getDistortion(tc.getQueries(), euc, eucP);
				simAcc += simD.getD();
				jlfAcc += jlfD.getD();
				simMeanAcc += getMeanDistortion(tc.getQueries(), euc, sim,
						simD.getR());
				jlfMeanAcc += getMeanDistortion(tc.getQueries(), euc, eucP,
						jlfD.getR());
			}
			System.out.print(noOfRefPoints + "\t" + simAcc / iters);
			System.out.print("\t" + jlfAcc / iters);
			System.out.print("\t" + simMeanAcc / iters);
			System.out.println("\t" + jlfMeanAcc / iters);
		}

	}

	public static <T> Distortion getDistortion(List<T> data,
			Metric<T> original, Metric<T> surrogate) {

		Distortion d = new Distortion();
		for (int i : Range.range(0, data.size() - 1)) {
			T first = data.get(i);
			for (int j : Range.range(i + 1, data.size())) {
				T second = data.get(j);

				double d1 = original.distance(first, second);
				double d2 = surrogate.distance(first, second);
				if (d2 == 0) {
					assert d1 == 0;
				}
				if (d2 != 0) {
					assert d1 >= d2;
					double ratio = d1 / d2; // should be greater than 1
					d.maxRatio = Math.max(d.maxRatio, ratio);
					d.minRatio = Math.min(d.minRatio, ratio);
				}
			}
		}
		return d;

	}

	public static <T> double getMeanDistortion(List<T> data,
			Metric<T> original, Metric<T> surrogate, double r) {
		double distAcc = 0;
		int noOfTests = 0;
		for (int i : Range.range(0, data.size() - 1)) {
			T first = data.get(i);
			for (int j : Range.range(i + 1, data.size())) {
				T second = data.get(j);

				double d1 = original.distance(first, second);
				double d2 = surrogate.distance(first, second);
				noOfTests++;
				if (d2 == 0) {
					assert d1 == 0;
				}
				if (d2 != 0) {
					assert d1 >= d2;
					double ratio = d1 / (d2 * r);
					distAcc += ratio;
				}
			}
		}
		return distAcc / noOfTests;
	}

	private static <T> Metric<T> getSimplexMetric(final Metric<T> metric,
			List<T> refPoints) {
		final NdimSimplex<T> sim = new NdimSimplex<>(metric, refPoints);
		return new Metric<T>() {

			@Override
			public double distance(T x, T y) {
				double[] xs = sim.getApex(x);
				double[] ys = sim.getApex(y);

				return NdimSimplex.l2Flex(xs, ys);
			}

			@Override
			public String getMetricName() {
				return "sim";
			}
		};
	}
}
