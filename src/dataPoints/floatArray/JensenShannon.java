package dataPoints.floatArray;

import coreConcepts.Metric;

public class JensenShannon implements Metric<float[]> {

	private boolean normalised;

	public JensenShannon(boolean normalised) {
		this.normalised = normalised;
	}

	public static double jsd(final float[] x, final float[] y) {

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
		 * allow for rounding errors, if this goes over 1.0 very bad things might
		 * happen!
		 */
		assert accumulator
				/ (Math.log(2) * 2) <= 1.0 : "rounding error has given mathematically impossible value to accumulator"; //$NON-NLS-1$

		if (Double.isNaN(accumulator)) {
			throw new RuntimeException("Nan found in Jensen-Shannon");
		}
		return Math.sqrt(1 - Math.min(accumulator / (Math.log(2) * 2), 1));

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

	private static float[] normalise(float[] x) {
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

	@Override
	public String getMetricName() {
		return "jsd";
	}

	@Override
	public double distance(float[] x, float[] y) {
		if (this.normalised) {
			return jsd(x, y);
		} else {
			return (jsd(normalise(x), normalise(y)));
		}
	}

}
