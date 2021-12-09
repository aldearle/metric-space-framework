package dataPoints.cartesian;

import coreConcepts.Metric;

public class NormalisedEuclidean<T extends CartesianPoint> implements Metric<T> {

	@Override
	public double distance(T x, T y) {

		double[] ys = y.getPoint();
		double acc = 0;
		int ptr = 0;
		for (double xVal : x.getPoint()) {
			final double diff = xVal - ys[ptr++];
			acc += diff * diff;
		}
		return Math.sqrt(acc) / Math.sqrt(ys.length);
	}

	@Override
	public String getMetricName() {
		return "euc";
	}

}
