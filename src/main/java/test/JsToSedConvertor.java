package test;

import java.util.Map;

import util.Util;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.CartesianPointMetrics;

public class JsToSedConvertor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[] a1 = { 1, 0 };
		double[] a2 = { 1, 10 };

		CartesianPoint p1 = new CartesianPoint(a1);
		CartesianPoint p2 = new CartesianPoint(a2);

		Map<String, Metric<CartesianPoint>> cartMets = CartesianPointMetrics
				.getCartesianPointMetrics();
		Metric<CartesianPoint> sed = cartMets.get("sed");
		Metric<CartesianPoint> jsd = cartMets.get("jsd");

		double d1 = sed.distance(p1, p2);
		double d2 = jsd.distance(p1, p2);

		double d3 = Util.sedToJs(d1);
		double d4 = Util.jsToSed(d2);
		System.out.println(d1 + "; " + d4 + "; " + d2 + "; " + d3 + "; ");

	}

}
