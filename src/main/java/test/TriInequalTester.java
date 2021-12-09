package test;

import coreConcepts.DataSet;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Manhattan;
import dataPoints.cartesian.TriDiscrim;
import dataSets.generators.CartesianPointGenerator;

public class TriInequalTester {

	private static DataSet<CartesianPoint> gen = new CartesianPointGenerator(2,
			false);
	private static Metric<CartesianPoint> m = new TriDiscrim<>();
	// private static Metric<CartesianPoint> m = new GeneralSymmetric1(1.1);
	private static Metric<CartesianPoint> man = new Manhattan<>();

	public static void main(String[] a) throws Exception {
		simple();

		complex();
	}

	protected static void complex() throws Exception {
		for (int i = 0; i < 1000 * 1000; i++) {

			CartesianPoint p = gen.randomValue();
			CartesianPoint q = gen.randomValue();
			CartesianPoint r = gen.randomValue();
			double d1 = m.distance(p, q);
			double d2 = m.distance(q, r);
			double d3 = m.distance(p, r);
			if (d1 > d2 + d3 || d2 > d1 + d3 || d3 > d1 + d2) {
				System.out.println(p.toString());
				System.out.println(q.toString());
				System.out.println(r.toString());
				System.out.println(d1 + "; " + d2 + "; " + d3);
				throw new Exception("Bad case found");
			}
		}
		System.out.println("no bad cases");
	}

	protected static void simple() throws Exception {
		double[] s1 = { 0.1, 0.9 };
		double[] s2 = { 0.9, 0.1 };
		double[] s3 = { 0.5, 0.5 };
		CartesianPoint p = new CartesianPoint(s1);
		CartesianPoint q = new CartesianPoint(s2);
		CartesianPoint r = new CartesianPoint(s3);

		// CartesianPoint p = gen.randomValue();
		// CartesianPoint q = gen.randomValue();
		// CartesianPoint r = gen.randomValue();
		double d1 = m.distance(p, q);
		double d2 = m.distance(q, r);
		double d3 = m.distance(p, r);
		if (d1 > d2 + d3 || d2 > d1 + d3 || d3 > d1 + d2) {
			System.out.println(p.toString());
			System.out.println(q.toString());
			System.out.println(r.toString());
			System.out.println(d1 + "; " + d2 + "; " + d3);
			throw new Exception("Bad case found");
		}
		System.out.println("no bad case found");
	}
}
