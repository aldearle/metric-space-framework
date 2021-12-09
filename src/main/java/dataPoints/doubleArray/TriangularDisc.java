package dataPoints.doubleArray;

import coreConcepts.Metric;

public class TriangularDisc implements Metric<double[]> {

	@Override
	public double distance(double[] x, double[] y) {
		double acc = 0;
		for (int i = 0; i < x.length; i++) {
			double t = x[i] - y[i];
			double bottom = x[i] + y[i];
			acc += (t * t) / bottom;
		}
		return Math.sqrt(acc);
	}

	@Override
	public String getMetricName() {
		return "triangularDisc";
	}

}
