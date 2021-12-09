package test;

import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannonViaSed;

public class FrancoAlbertoPaperTest {

	public static void main(String[] args) {
		double[] v1 = { 0, 0.3, 0, 0.2, 0.5, 0 };
		double[] v2 = { 0, 0.1, 0, 0, 0, 0.9 };

		CartesianPoint c1 = new CartesianPoint(v1);
		CartesianPoint c2 = new CartesianPoint(v2);

		JensenShannonViaSed js = new JensenShannonViaSed();

		final double dist = js.distance(c1, c2);
		System.out.println("distance: " + dist);

		double d = (1 - (dist * dist))*2;

		System.out.println("test: " + d);
	}

}
