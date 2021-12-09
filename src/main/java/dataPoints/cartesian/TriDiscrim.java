package dataPoints.cartesian;

import coreConcepts.Metric;

/**
 * @author newrichard
 *
 * @param <T>
 */
public class TriDiscrim<T extends CartesianPoint> implements Metric<T> {

	private static double root2 = Math.sqrt(2);

	@Override
	public double distance(T x, T y) {
		return probDistance(x.getNormalisedPoint(), y.getNormalisedPoint());
	}

	static private double probDistance(double[] x, double[] y) {
		double acc = divergenceOver2(x, y);
		return Math.sqrt(Math.abs(acc));
	}

	public static double divergence(double[] x, double[] y) {
		double acc = 0;
		for (int i = 0; i < x.length; i++) {
			final double x_i = x[i];
			final double y_i = y[i];
			if (x_i == 0 || y_i == 0) {
				acc += (x_i + y_i);
			} else {
				double bottom = x_i + y_i;
				double topRoot = x_i - y_i;
				acc += (topRoot * topRoot) / bottom;
			}
		}
		return acc;
	}

	/**
	 * redefined divergence according to unpublished paper on four metrics...
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static double divergenceOver2(double[] x, double[] y) {
		double acc = 0;
		for (int i = 0; i < x.length; i++) {
			final double x_i = x[i];
			final double y_i = y[i];
			final double thing = (x_i * y_i) / (x_i + y_i);
			if (!Double.isNaN(thing)) {
				acc += thing;
			}
		}
		return 1 - acc * 2;
	}

	@Override
	public String getMetricName() {
		return "tri";
	}

	public static void main(String[] args) {
		double[] p = { 1, 1, 1 };
		double[] q = { 1, 0, 0 };

		TriDiscrim<CartesianPoint> t = new TriDiscrim<>();
		final CartesianPoint p1 = new CartesianPoint(p);
		final CartesianPoint p2 = new CartesianPoint(q);
		double[] n1 = p1.getNormalisedPoint();
		double[] n2 = p2.getNormalisedPoint();

		System.out.println(divergence(n1, n2));
		System.out.println(divergenceOver2(n1, n2));
		System.out.println(t.distance(p1, p2));

	}

}
