package dataPoints.floatArray;

import coreConcepts.Metric;

/**
 * 
 * The most basic implementation of SED over two arrays of doubles
 * 
 * @author Richard Connor
 * 
 */
public class SedFloatArray implements Metric<float[]> {

	private boolean normalised;

	public SedFloatArray(boolean normalised) {
		this.normalised = normalised;
	}

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
	public static double JensenShannon(final float[] x, final float[] y) {

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

		if (Double.isNaN(accumulator)) {
			System.out.println("acc is a NaN");
		}
		return Math.min(accumulator / (Math.log(2) * 2), 1);
	}

	private static double sum(float[] x) {
		double acc = 0;
		for (double d : x) {
			if (d < 0) {
				d = -d;
			}
			acc += d;
		}
		return acc;
	}

	private static double xLogx(double d) {
		return d * Math.log(d);
	}

	@Override
	public double distance(float[] x, float[] y) {
		assert x.length == y.length : "arrays are different length in SED call"; //$NON-NLS-1$
		if (!normalised) {
			x = normalise(x);
			y = normalise(y);
		}
		return distanceS(x, y);
	}

	private float[] normalise(float[] x) {
		double xSum = sum(x);
		if (xSum == 0) {
			throw new RuntimeException("bad vector");
		}
		float[] res = new float[x.length];
		int ptr = 0;
		for (float f : x) {
			if (f < 0) {
				f = -f;
			}
			res[ptr++] = (float) (f / xSum);
		}
		return res;
	}

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	public static double distanceS(final float[] x, final float[] y) {

		final double raw = Math.pow(2, 1 - JensenShannon(x, y)) - 1;
		assert raw >= 0.0 && raw <= 1.0 : "this really shouldn't happen as long as JS is properly bounded"; //$NON-NLS-1$

		if (Double.isNaN(raw)) {
			throw new RuntimeException("threw a NaN");
		}
		final double pow = Math.pow(raw, 0.486);

		return pow;
	}

	@Override
	public String getMetricName() {
		return "Sed_float_array"; //$NON-NLS-1$
	}

}
