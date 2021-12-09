package dataPoints.cartesian;

import coreConcepts.Metric;

public class LebesqueInf<T extends CartesianPoint> implements Metric<T> {

	private int dims;

	public LebesqueInf() {
		this.dims = -1;
	}

	public LebesqueInf(int dims) {
		this.dims = dims;
	}

	@Override
	public double distance(T x, T y) {
		if (dims == -1) {
			dims = x.getPoint().length;
		}
		double[] xs = x.getPoint();
		double[] ys = y.getPoint();
		double acc = 0;
		for (int i = 0; i < this.dims; i++) {

			double diff = xs[i] - ys[i];
			if (diff < 0) {
				diff = -diff;
			}
			if (diff > acc) {
				acc = diff;
			}
		}

		return acc;
	}

	@Override
	public String getMetricName() {
		return "chb";
	}

}
