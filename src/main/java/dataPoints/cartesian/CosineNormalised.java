package dataPoints.cartesian;

import coreConcepts.Metric;

/**
 * 
 * distance is the Euclidean distance between the end points of normalised
 * vectors
 * 
 * a variant proposed by Lucia Vadicamo, documented in arXiv:1604.08640
 * 
 * @author Richard Connor
 * 
 */
public class CosineNormalised<T extends CartesianPoint> implements Metric<T> {

	private static double oneOverRoot2 = 1 / Math.sqrt(2);

	@Override
	public double distance(CartesianPoint point1, CartesianPoint point2) {
		double p1mag = point1.getMagnitude();
		double p2mag = point2.getMagnitude();

		double[] p1 = point1.getPoint();
		double[] p2 = point2.getPoint();

		double magAcc = 0;
		for (int i = 0; i < p1.length; i++) {
			final double p1n = p1[i] / p1mag;
			final double p2n = p2[i] / p2mag;
			final double p12diff = p1n - p2n;
			magAcc += p12diff * p12diff;
		}

		return Math.sqrt(magAcc) * oneOverRoot2;
	}

	@Override
	public String getMetricName() {
		return "cos";
	}

	public static double convertFromCosine(double cos) {
		double ang = cos * (Math.PI / 2);
		return 2 * Math.sin(ang / 2) * oneOverRoot2;
	}

	public static void main(String[] a) {
		double[] arr1 = { 2, 0, 4, 0, 6 };
		double[] arr2 = { 0, 3, 0, 5, 0 };
		CartesianPoint p1 = new CartesianPoint(arr1);
		CartesianPoint p2 = new CartesianPoint(arr2);
		CosineNormalised<CartesianPoint> cosN = new CosineNormalised<>();
		Cosine<CartesianPoint> cos = new Cosine<>();

		final double d1 = cos.distance(p1, p2);
		System.out.println(d1);
		System.out.println(cosN.distance(p1, p2));
		System.out.println(convertFromCosine(d1));
	}
}
