package dataPoints.floatArray;

import coreConcepts.Metric;

/**
 * @author Richard Connor
 * 
 *         Tshebyshev, or Chebyshev, or Lebesque_\infty distance
 * 
 */
public class Euclidean implements Metric<float[]> {

	@Override
	public double distance(float[] x, float[] y) {
		double acc = 0;
		for (int i = 0; i < x.length; i++) {
			final float diff = x[i] - y[i];
			acc += diff * diff;

		}
		return Math.sqrt(acc);
	}

	@Override
	public String getMetricName() {
		return "euc";
	}

}
