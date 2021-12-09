package test;

import java.util.List;

import sisap_2017_experiments.NdimSimplex;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Range;
import util.Util_ISpaper;
import dataPoints.cartesian.CartesianPoint;

public class NsmpUpbPlay {

	public static void main(String[] a) throws Exception {
		int testSize = 1000;
		int dim = 15;
		TestContext tc = new TestContext(Context.colors);
		tc.setSizes(0, dim * 100);
//		List<CartesianPoint> refPoints = Util_ISpaper.getFFT(tc.getRefPoints(),
//				tc.metric(), dim);
		List<CartesianPoint> refPoints = Util_ISpaper.getRandom(tc.getRefPoints(), dim);
		NdimSimplex<CartesianPoint> sim = new NdimSimplex<>(tc.metric(),
				refPoints);
		List<CartesianPoint> dat = tc.getData();
		double[] dists = new double[testSize];
		double[] lwbs = new double[testSize];
		double[] upbs = new double[testSize];
		for (int i : Range.range(0, testSize)) {
			CartesianPoint p1 = dat.get(i * 2);
			CartesianPoint p2 = dat.get(i * 2 + 1);
			dists[i] = tc.metric().distance(p1, p2);

			double[] ap1 = sim.getApex(p1);
			double[] ap2 = sim.getApex(p2);
			lwbs[i] = NdimSimplex.l2Flex(ap1, ap2);
			double ySum = ap1[dim - 1] + ap2[dim - 1];
			double baseDiff = NdimSimplex.l2(ap1, ap2, dim - 1);
			upbs[i] = Math.sqrt(ySum * ySum + baseDiff * baseDiff);

			System.out.println(dists[i] + "\t" + lwbs[i] + "\t" + upbs[i]);
		}
		// System.out.println(tc.getThreshold());

	}
}
