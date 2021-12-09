package test;

import coreConcepts.DataSet;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannonViaSed;
import dataPoints.cartesian.SEDByComplexity;
import dataPoints.compactEnsemble.EventToIntegerMap;
import dataPoints.doubleArray.JSDoubleArray;
import dataPoints.doubleArray.ManhattanDoubleArray;
import dataPoints.doubleArray.SEDDoubleArray;
import dataSets.generators.CartesianPointGenerator;

/**
 * @author Richard Connor
 * 
 */
public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// testJS();
			// testForTri();
			testEventIntegerMap();

		} catch (AssertionError e) {
			System.out.println(e.getMessage());
		}
	}

	private static void testEventIntegerMap() {
		try {
			String a = "lkjsdflkj";
			String b = "hello";
			String c = "world";

			EventToIntegerMap<String> eim = new EventToIntegerMap<String>();
			int x = eim.toEncodedEvent(a, 3);
			int y1 = eim.toEncodedEvent(b, 1);
//			int y2 = eim.toEncodedEvent(b, 12334566);
			int y = eim.toEncodedEvent(b, 1);
			int z = eim.toEncodedEvent(c, 13);

			System.out.println(EventToIntegerMap.getEventCode(x) + ":"
					+ EventToIntegerMap.getCard(x));
			System.out.println(EventToIntegerMap.getEventCode(y) + ":"
					+ EventToIntegerMap.getCard(y));
			System.out.println(EventToIntegerMap.getEventCode(z) + ":"
					+ EventToIntegerMap.getCard(z));
		} catch (Throwable t) {
			System.out.println("error: " + t.getMessage());
		}

	}

	private static void testJS() {
		double[] a = { 0, 1 };
		double[] b = { 1, 0 };
		double[] c = { 0.5, 0.5 };
		@SuppressWarnings("unused")
		double[] d = { 0.5, 0.5, 0 };

		Metric<double[]> js = new JSDoubleArray();
		Metric<double[]> sed1 = new SEDDoubleArray();

		System.out.println("Jensen-Shannon distances");
		System.out.println(js.distance(a, a));
		System.out.println(js.distance(a, c));
		System.out.println(js.distance(b, c));

		System.out.println("SED distances:");
		System.out.println(sed1.distance(a, a));
		System.out.println(sed1.distance(a, c));
		System.out.println(sed1.distance(c, b));

		System.out.println("testing Array vs CartesianPoint");
		Metric<CartesianPoint> sed2 = new SEDByComplexity();

		System.out.println(sed1.distance(a, c));
		System.out.println(sed2.distance(new CartesianPoint(a),
				new CartesianPoint(c)));

	}

	private static void testCartesianGenerator() {

		DataSet<CartesianPoint> p = new CartesianPointGenerator(2, false);
		System.out.println(p.randomValue());
	}

	private static void testForTri() {

		DataSet<CartesianPoint> p = new CartesianPointGenerator(2, false);
		Metric<CartesianPoint> sed = new SEDByComplexity();
		Metric<CartesianPoint> js = new JensenShannonViaSed();
		ManhattanDoubleArray ms = new ManhattanDoubleArray();

		long t0 = System.currentTimeMillis();

		int x = 0;
		while (System.currentTimeMillis() - t0 < 120) {
			CartesianPoint a = p.randomValue();
			CartesianPoint b = p.randomValue();
			CartesianPoint c = p.randomValue();
			double d1 = Math.pow(js.distance(a, b), 1.01);
			double d2 = Math.pow(js.distance(b, c), 1.01);
			double d3 = Math.pow(js.distance(a, c), 1.01);
			if ((d1 + d2 < d3) || (d2 + d3 < d1) || (d1 + d3 < d2)) {
				System.out.println("ooops - case " + x);
				System.out.println(d1 + ";" + d2 + ";" + d3);
				System.out.println(a + ";" + b + ";" + c);
				System.out.println(js.distance(a, b) + ";;" + js.distance(b, c)
						+ ";;" + js.distance(a, c));
			}
			x++;
		}
		System.out.println("finished ok");
	}

}
