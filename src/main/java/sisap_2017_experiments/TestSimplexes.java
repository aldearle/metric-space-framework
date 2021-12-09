package sisap_2017_experiments;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannon;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Range;
import util.Util_ISpaper;

import java.util.List;

public class TestSimplexes {

	public static void main(String[] a) throws Exception {

		// checkBounds();
		TestContext tc = new TestContext(Context.nasa);
		tc.setSizes(0, 100);

		final Metric<CartesianPoint> metric = new JensenShannon<>(false, false);
		final List<CartesianPoint> refs = Util_ISpaper.getFFT(
				tc.getRefPoints(), metric, 20);
		NdimSimplex<CartesianPoint> s = new NdimSimplex<>(metric, refs);
		final List<CartesianPoint> data = tc.getData();
		double[] dists = new double[refs.size()];
		CartesianPoint rand = data.get(2345);
		int dPtr = 0;
		for (CartesianPoint p : refs) {
			dists[dPtr++] = metric.distance(p, rand);
		}

		int tests = 0;
		long t0 = System.currentTimeMillis();
		for (int m : Range.range(0, 1000)) {
			final CartesianPoint p1 = data.get(m);
			for (int n : Range.range(0, 1000)) {
				final CartesianPoint p2 = data.get(n);
				tests++;

				// double d = metric.distance(p1, p2);
				double[] d = s.getApex(dists);
				double[] d2 = s.getApex(dists);
				// double dd = NdimSimplex.l2(d, d2);
				double[] ddd = s.getBounds(d, d2);
			}
		}
		long t1 = System.currentTimeMillis();
		System.out.println("took " + (t1 - t0) + " for " + tests + " tests");

	}

	private static void checkBounds() throws Exception {
		TestContext tc = new TestContext(Context.euc10, 1000 * 2 + 10);
		tc.setSizes(0, 10);
		List<CartesianPoint> dat = tc.getData();
		NdimSimplex sim = new NdimSimplex(tc.metric(), tc.getRefPoints());
		for (int i : Range.range(0, 1000)) {
			CartesianPoint t1 = dat.get(i * 2);
			CartesianPoint t2 = dat.get(i * 2 + 1);
			double d = tc.metric().distance(t1, t2);
			double[] a1 = sim.getApex(t1);
			double[] a2 = sim.getApex(t2);
			double[] b = sim.getBounds(a1, a2);
			System.out.println(b[0] + "\t" + d + "\t" + b[1]);
		}
	}
}
