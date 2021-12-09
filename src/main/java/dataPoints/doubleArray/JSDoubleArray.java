package dataPoints.doubleArray;

import coreConcepts.Metric;

/**
 * 
 * The most basic implementation of JS over two arrays of doubles
 * 
 * @author Richard Connor
 * 
 */
public class JSDoubleArray implements Metric<double[]> {

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
	@Override
	public double distance(final double[] x, final double[] y) {

		assert x.length == y.length : "arrays are different length in JensenShannon"; //$NON-NLS-1$
		assert sum(x) == 1 : "J-S array arguments must be normalised"; //$NON-NLS-1$
		assert sum(y) == 1 : "J-S array arguments must be normalised"; //$NON-NLS-1$

		double accumulator = naturalJS(x, y);
		/*
		 * allow for rounding errors, if this goes over 1.0 very bad things
		 * might happen!
		 */
		assert accumulator / (Math.log(2) * 2) <= 1.0 : "rounding error has given mathematically impossible value to accumulator"; //$NON-NLS-1$

		double d = Math.min(accumulator / (Math.log(2) * 2), 1);
		// return Math.sqrt(1 - d);
		return Math.pow(1 - d, 0.5);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return the JS similarity of the two (normalised) arguments using natural
	 *         logs
	 */
	public static double naturalJS(final double[] x, final double[] y) {
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
		return accumulator;
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
	public String getMetricName() {
		return "JS double array"; //$NON-NLS-1$
	}

}
