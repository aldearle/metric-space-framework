package dataPoints.doubleArray;

import coreConcepts.Metric;

/**
 * 
 * The most basic implementation of SED over two arrays of doubles
 * 
 * @author Richard Connor
 * 
 */
public class SEDDoubleArray implements Metric<double[]> {

	/**
	 * Returns (half of) the (base 2) Jensen-Shannon similarity. Subtract this
	 * from one and take the square root, you have a proper metric.
	 * 
	 * @param x
	 *            the first array
	 * @param y
	 *            the second array, which must be the same length as the first
	 *            or else it will go wrong without explicitly raising an
	 *            exception
	 * @return the Jensen-Shannon normalised similarity
	 */
	public static double JensenShannon(final double[] x, final double[] y) {

		assert x.length == y.length : "arrays are different length in JensenShannon"; //$NON-NLS-1$
		assert sum(x) == 1 : "J-S array arguments must be normalised"; //$NON-NLS-1$
		assert sum(y) == 1 : "J-S array arguments must be normalised"; //$NON-NLS-1$

		double accumulator = 0;
		int ptr = 0;
		for (double d1 : x) {
			final double d2 = y[ptr++];

			if (d1 != 0 && d2 != 0) {
				final double sum = d1 + d2;
				accumulator -= xLogx(d1);
				accumulator -= xLogx(d2);
				accumulator += xLogx(sum);
			}

		}
		/*
		 * allow for rounding errors, if this goes over 1.0 very bad things
		 * might happen!
		 */
		assert accumulator / (Math.log(2) * 2) <= 1.0 : "rounding error has given mathematically impossible value to accumulator"; //$NON-NLS-1$

		return Math.min(accumulator / (Math.log(2) * 2), 1);
	}

	private static double sum(double[] x) {
		double acc = 0;
		for (double d : x) {
			acc += d;
		}
		return acc;
	}

	private static double xLogx(double d) {
		return d * Math.log(d);
	}

	@Override
	public double distance(final double[] x, final double[] y) {
		assert x.length == y.length : "arrays are different length in SED call"; //$NON-NLS-1$

		return distanceS(x, y);
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public static double distanceS(final double[] x, final double[] y) {
		final double raw = Math.pow(2, 1 - JensenShannon(x, y)) - 1;
		assert raw >= 0.0 && raw <= 1.0 : "this really shouldn't happen as long as JS is properly bounded"; //$NON-NLS-1$

		return Math.pow(raw, 0.486);
	}

	@Override
	public String getMetricName() {
		return "SED double array"; //$NON-NLS-1$
	}

}
