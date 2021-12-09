package n_point_surrogate;

import java.util.ArrayList;
import java.util.List;

import testloads.TestContext;
import util.Measurements;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannon;

public class Test {

	enum Strategy {
		random, fft, corners
	};

	public static void main(String[] args) throws Exception {

		TestContext tc = new TestContext(1000 + 1000);
		tc.setSizes(0, 1000);
		List<CartesianPoint> data = tc.getDataCopy();
		Metric<CartesianPoint> euc = new Euclidean<>();
		Metric<CartesianPoint> jsd = new JensenShannon<>(false, false);
		Metric<CartesianPoint> met = euc;
		
		Measurements m = new Measurements();
		

		for (int count : Range.range(2, 10)) {

			List<CartesianPoint> refPoints = getRefPoints(tc.getRefPoints(),
					met, 3, Strategy.fft);

			CartesianPoint[] ps = { data.get(0), data.get(1) };
			System.out
					.println("actual distance\t" + met.distance(ps[0], ps[1]));

			double d1 = lwbDist3p(ps, refPoints.subList(0, count), met);
			double d2 = lwbDistNp(ps, refPoints.subList(0, count), met);
			System.out.println(d1 + "\t" + d2);

		}
	}

	private static List<CartesianPoint> getRefPoints(List<CartesianPoint> pool,
			Metric<CartesianPoint> met, int i, Strategy strat) {
		switch (strat) {
		case corners: {
			return corners(10, 10);
		}
		case fft: {
			return Util_ISpaper.getFFT(pool, met, i);
		}
		case random: {
			return Util_ISpaper.getRandom(pool, i);
		}
		default: {
			throw new RuntimeException("no such strategy");
		}
		}
	}

	private static double lwbDist3p(CartesianPoint[] ps,
			List<CartesianPoint> subList, Metric<CartesianPoint> m) {
		double max = 0;
		for (int i : Range.range(0, subList.size())) {
			double d1 = m.distance(ps[0], subList.get(i));
			double d2 = m.distance(ps[1], subList.get(i));
			max = Math.max(max, Math.abs(d1 - d2));
		}
		return max;
	}

	private static double lwbDistNp(CartesianPoint[] ps,
			List<CartesianPoint> subList, Metric<CartesianPoint> m)
			throws Exception {
		SimplexND<CartesianPoint> sim = new SimplexND<>(subList.size(), m,
				subList);
		double[] p1 = sim.formSimplex(ps[0]);
		double[] p2 = sim.formSimplex(ps[1]);
		return SimplexExclusion.l2(p1, p2);
	}

	private static List<CartesianPoint> corners(int dim, int noOfPoints) {
		List<CartesianPoint> corners = new ArrayList<>();
		for (int i : Range.range(0, noOfPoints)) {
			double[] n = new double[dim];
			n[i] = 1;
			corners.add(new CartesianPoint(n));
		}
		return corners;
	}
}
