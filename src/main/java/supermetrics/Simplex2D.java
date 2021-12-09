package supermetrics;

import java.util.List;

import n_point_surrogate.SimplexExclusion;
import coreConcepts.Metric;

public class Simplex2D<T> extends SimplexExclusion<T> {

	public Simplex2D(int dimension, Metric<T> metric, T... refPoints)
			throws Exception {
		super(dimension, metric, refPoints);
	}

	@Override
	public double[] formSimplex(T p) {
		double[] res = new double[2];
		double base = this.metric.distance(this.referencePoints[0],
				this.referencePoints[1]);
		double d1 = this.metric.distance(p, this.referencePoints[0]);
		double d2 = this.metric.distance(p, this.referencePoints[1]);

		/*
		 * this below is wrong, because is doesn't allow for the apex to be to
		 * the left of the origin!
		 */
		double area = getArea(base, d1, d2);
		// A = 1/2 b * h
		// so h = 2A / b
		// res[1] = (area / base) * 2;
		// so d1 (or d2)
		// res[0] = Math.sqrt(d2 * d2 - res[1] * res[1]);

		res[0] = (d1 * d1 + base * base - d2 * d2) / (2 * base);
		res[1] = Math.sqrt(d1 * d1 - res[0] * res[0]);
		// %a^2 = b^2 + c^2 - 2bc cosA
		// % so.... cosA = (b^2 + c^2 - a^2)/2bc
		// x_offset = (d1 * d1 + pDist * pDist - d2 * d2) / (2 * pDist);
		// y_offset = sqrt(d1*d1 - x_offset * x_offset);
		return res;
	}

	/**
	 * @param a
	 *            side 1 length
	 * @param b
	 *            side 2 length
	 * @param c
	 *            side 3 length
	 * @return the area of the triangle bounded by these three side lengths
	 */
	protected static double getArea(double a, double b, double c) {
		double p = (a + b + c) / 2;
		return Math.sqrt(p * (p - a) * (p - b) * (p - c));
	}

	public static void main(String[] a) {
		System.out.println(getArea(5, 12, 13));
	}

	@Override
	public float[] formSimplexF(T p) {
		// TODO Auto-generated method stub
		return null;
	}

}
