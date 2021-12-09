package dataPoints.cartesian;

import coreConcepts.Metric;

public class GeneralSymmetric1 implements Metric<CartesianPoint> {

	private double s;

	public GeneralSymmetric1(double s) {
		if (s == 0 || s == 1) {
			throw new RuntimeException("GSM not defined at " + s);
		}
		this.s = s;
	}

	@Override
	public double distance(CartesianPoint x, CartesianPoint y) {
		double[] xs = x.getNormalisedPoint();
		double[] ys = y.getNormalisedPoint();
		double acc = 0;
		for (int i = 0; i < xs.length; i++) {
			double p_i = xs[i];
			double q_i = ys[i];
			double term1 = (Math.pow(p_i, this.s) + Math.pow(q_i, this.s)) / 2;
			final double subTerm = (p_i + q_i) / 2;
			double term2 = Math.pow(subTerm, 1 - this.s);
			final double term = (term1 * term2);
			// System.out.println(term1 + ";" + term2);
			// System.out.println(p_i + "; " + q_i + "; " + term);
			acc += term;
		}
		return Math.sqrt(acc - 1);
	}

	@Override
	public String getMetricName() {
		return "GenSym1_" + this.s;
	}

	public static void main(String[] a) {
		Metric<CartesianPoint> m1 = new GeneralSymmetric1(-1);
		Metric<CartesianPoint> m2 = new TriDiscrim<>();

		double[] p1 = { 0.00000000001, 4, 3, 2, 1 };
		double[] p2 = { 0.000000001, 4, 3, 2, 1 };

		final double d1 = m1.distance(new CartesianPoint(p1),
				new CartesianPoint(p2));
		final double d2 = m2.distance(new CartesianPoint(p1),
				new CartesianPoint(p2));
		System.out.println(d1 + "; " + d2 + "; " + d1 / d2);
	}

}
