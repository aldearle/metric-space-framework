package dataPoints.floatArray;

import coreConcepts.Metric;

public class Triangular implements Metric<float[]> {

	@Override
	public double distance(float[] x, float[] y) {
		double acc = 0;
		for (int i = 0; i < x.length; i++) {
			double top = x[i] - y[i];
			double bottom = x[i] + y[i];
			acc += (top * top) / bottom;
		}
		return Math.sqrt(acc / 2);
	}

	@Override
	public String getMetricName() {
		return "tri";
	}

}
