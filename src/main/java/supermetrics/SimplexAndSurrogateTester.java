package supermetrics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import n_point_surrogate.SimplexExclusion;
import n_point_surrogate.SimplexND;
import testloads.CartesianThresholds;
import testloads.TestContext;
import testloads.TestLoad;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

public class SimplexAndSurrogateTester {

	public static void main(String[] args) throws Exception {
		int dimension = 5;

		TestContext tc = new TestContext(TestContext.Context.colors,
				5000 + dimension);
		tc.setSizes(0, dimension);
		CartesianPoint[] refs = getRefPoints(dimension, tc);
		SimplexExclusion<CartesianPoint> se = new SimplexND<CartesianPoint>(
				dimension, tc.metric(), refs);
		List<CartesianPoint> dat = tc.getDataCopy();
		for (int i = 0; i < 5000; i += 2) {
			CartesianPoint p1 = dat.get(i);
			CartesianPoint p2 = dat.get(i + 1);
			double[] ap1 = se.formSimplex(p1);
			double[] ap2 = se.formSimplex(p2);
			System.out.println(tc.metric().distance(p1, p2) + "\t"
					+ SimplexExclusion.l2(ap1, ap2));
		}

		// se.addData(tc.getData());
		// printDistancesFromApex(dimension, se);
	}

	protected static void printDistancesFromApex(int dimension,
			SimplexExclusion<CartesianPoint> se) {
		double[] ap = se.getApexCentroid();
		SimplexGeneral.display("apex mean", ap);
		double[] sds = se.getApexSDs(ap);
		SimplexGeneral.display("apex sdevs", sds);
		double[] origin = new double[dimension];

		double[][] apexPoints = se.getApexPoints();
		double[] apDists = new double[apexPoints.length];
		for (int i = 0; i < apDists.length; i++) {
			apDists[i] = SimplexExclusion.l2(ap, apexPoints[i]);
		}

		for (double d : apDists) {
			System.out.println(d);
		}
	}

	protected static CartesianPoint[] getRefPoints(int dimension, TestContext tc) {
		CartesianPoint[] refs = new CartesianPoint[dimension];
		int ptr = 0;
		for (CartesianPoint p : tc.getRefPoints()) {
			refs[ptr++] = p;
		}
		return refs;
	}

	protected static void testDistanceSpreads() throws Exception,
			FileNotFoundException {
		TestContext tc = new TestContext(TestContext.Context.nasa, 110);
		tc.setSizes(0, 10);

		List<CartesianPoint> dat = tc.getDataCopy().subList(0, 100);
		SimplexExclusion<CartesianPoint> se1 = new Simplex1D<>(1, tc.metric(),
				tc.getRefPoints().get(0));
		SimplexExclusion<CartesianPoint> se2 = new SimplexND<>(2, tc.metric(),
				getRefPoints(2, tc));
		SimplexExclusion<CartesianPoint> se3 = new SimplexND<>(3, tc.metric(),
				getRefPoints(3, tc));
		SimplexExclusion<CartesianPoint> se4 = new SimplexND<>(4, tc.metric(),
				getRefPoints(4, tc));
		SimplexExclusion<CartesianPoint> se5 = new SimplexND<>(5, tc.metric(),
				getRefPoints(5, tc));
		SimplexExclusion<CartesianPoint> se6 = new SimplexND<>(6, tc.metric(),
				getRefPoints(6, tc));
		SimplexExclusion<CartesianPoint> se10 = new SimplexND<>(10,
				tc.metric(), getRefPoints(10, tc));
		se1.addData(dat);
		se2.addData(dat);
		se3.addData(dat);
		se4.addData(dat);
		se5.addData(dat);
		se6.addData(dat);
		se10.addData(dat);
		double[][] s1points = se1.getApexPoints();
		double[][] s2points = se2.getApexPoints();
		double[][] s3points = se3.getApexPoints();
		double[][] s4points = se4.getApexPoints();
		double[][] s5points = se5.getApexPoints();
		double[][] s6points = se6.getApexPoints();
		double[][] s10points = se10.getApexPoints();

		PrintWriter pw = new PrintWriter(
				"/Volumes/Data/simplexDists/results.dat");

		for (int i = 0; i < s1points.length - 1; i++) {
			for (int j = i + 1; j < s1points.length; j++) {
				double x = SimplexExclusion.l2(s1points[i], s1points[j]);
				double y = SimplexExclusion.l2(s2points[i], s2points[j]);
				double z = SimplexExclusion.l2(s3points[i], s3points[j]);
				double z1 = SimplexExclusion.l2(s4points[i], s4points[j]);
				double z2 = SimplexExclusion.l2(s5points[i], s5points[j]);
				double z3 = SimplexExclusion.l2(s6points[i], s6points[j]);
				double z4 = SimplexExclusion.l2(s10points[i], s10points[j]);

				pw.println(x + "\t" + y + "\t" + z + "\t" + z1 + "\t" + z2
						+ "\t" + z3 + "\t" + z4);
			}
		}
		pw.close();

		System.out.println(dat.size());
		System.out.println(tc.getThreshold());
	}

	protected static void testExclusions() throws Exception {
		int dimension = 60;

		for (int dim = dimension; dim < dimension + 1; dim++) {
			for (int noOfRefs = 3; noOfRefs <= 100; noOfRefs++) {

				TestLoad tl = new TestLoad(dim, 2000 + noOfRefs, false);
				List<CartesianPoint> refs = tl.getQueries(noOfRefs);
				List<CartesianPoint> qs = tl.getQueries(1000);
				List<CartesianPoint> dat = tl.getDataCopy();
				Metric<CartesianPoint> euc = new Euclidean<>();
				double threshold = CartesianThresholds.getThreshold("euc", dim,
						1);

			}
		}
	}

	private static void printMeanAndSD() {

	}

	protected static int count(boolean[] ns) {
		int exc = 0;
		for (boolean n : ns) {
			if (!n)
				exc++;
		}
		return exc;
	}

	private static void test(List<CartesianPoint> refs,
			List<CartesianPoint> qs, List<CartesianPoint> dat,
			Metric<CartesianPoint> euc, double threshold) throws Exception {

		Simplex1D<CartesianPoint> s1a = new Simplex1D<>(1, euc, refs.get(0));
		Simplex1D<CartesianPoint> s1b = new Simplex1D<>(1, euc, refs.get(1));
		Simplex1D<CartesianPoint> s1c = new Simplex1D<>(1, euc, refs.get(2));
		Simplex2D<CartesianPoint> s2a = new Simplex2D<>(2, euc, refs.get(0),
				refs.get(1));
		Simplex2D<CartesianPoint> s2b = new Simplex2D<>(2, euc, refs.get(1),
				refs.get(2));
		Simplex2D<CartesianPoint> s2c = new Simplex2D<>(2, euc, refs.get(0),
				refs.get(2));
		Simplex3D<CartesianPoint> s3 = new Simplex3D<>(3, euc, refs.get(0),
				refs.get(1), refs.get(2));

		SimplexExclusion[] ss = { s1a, s1b, s1c, s2a, s2b, s2c, s3 };
		boolean[][] bs = new boolean[7][dat.size()];
		int ptr = 0;
		for (SimplexExclusion s : ss) {
			s.addData(dat);
			s.trackExclusions(bs[ptr++], qs.get(0), threshold);
		}

		// s1.trackExclusions(bs, qs.get(0), threshold);
		// s2.trackExclusions(bs, qs.get(0), threshold);
		System.out.println("s1a\ts1b\ts1c\ts2a\ts2b\ts2v\ts3");
		for (int i = 0; i < dat.size(); i++) {
			for (int j = 0; j < 7; j++) {
				System.out.print(bs[j][i] + "\t");
			}
			System.out.println();
		}

		// outputSimpleExclusionScores(dim, qs, threshold, s1, s2, s3);
	}

	protected static void outputSimpleExclusionScores(final int dim,
			List<CartesianPoint> qs, double threshold,
			Simplex1D<CartesianPoint> s1, Simplex2D<CartesianPoint> s2,
			Simplex3D<CartesianPoint> s3) {
		System.out.print(dim + "dim\t");
		System.out.print(test(qs, threshold, s1) / 1000);
		System.out.print("\t");
		System.out.print(test(qs, threshold, s2) / 1000);
		System.out.print("\t");
		System.out.print(test(qs, threshold, s3) / 1000);
		System.out.print("\t");
		System.out.print(threshold);
		System.out.println();
	}

	private static void get3Dpoints(int dim) throws Exception {
		int noOfPoints = 500;
		TestLoad tl = new TestLoad(dim, noOfPoints + 4, false);

		// TestLoad tl = new TestLoad(TestLoad.SisapFile.colors);
		List<CartesianPoint> refs = tl.getQueries(3);
		List<CartesianPoint> qs = tl.getQueries(1);
		List<CartesianPoint> dat = tl.getQueries(500);

		Metric<CartesianPoint> euc = new Euclidean<>();
		Simplex2D<CartesianPoint> base = new Simplex2D(2, euc, refs.get(0),
				refs.get(1));
		double[] apex = base.formSimplex(refs.get(2));
		System.out.println("0\t0\t0");
		System.out.println(euc.distance(refs.get(0), refs.get(1)) + "\t0\t0");
		System.out.println(apex[0] + "\t" + apex[1] + "\t" + 0);

		Simplex3D<CartesianPoint> s = new Simplex3D<>(3, euc, refs.get(0),
				refs.get(1), refs.get(2));

		double[][] points3d = new double[dat.size()][3];
		int ptr = 0;
		for (CartesianPoint p : dat) {
			points3d[ptr++] = s.formSimplexTest(p, false);
		}
		for (double[] p : points3d) {
			System.out.println(p[0] + "\t" + p[1] + "\t" + p[2]);
		}

	}

	protected static double test(List<CartesianPoint> qs, double threshold,
			SimplexExclusion<CartesianPoint> s1) {
		int exclusions = 0;
		for (CartesianPoint q : qs) {
			exclusions += s1.countExclusions(q, threshold);
		}
		return (double) exclusions / qs.size();
	}
}
