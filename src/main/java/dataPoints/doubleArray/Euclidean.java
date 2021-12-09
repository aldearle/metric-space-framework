package dataPoints.doubleArray;

import coreConcepts.Metric;

/**
 * @author Richard Connor
 * 
 */
public class Euclidean implements Metric<double[]> {

	@Override
	public double distance(double[] x, double[] y) {
		double acc = 0;
		for (int i = 0; i < x.length; i++) {
			final double diff = x[i] - y[i];
			acc += diff * diff;

		}
		return Math.sqrt(acc);
	}

	@Override
	public String getMetricName() {
		return "euc";
	}

}
