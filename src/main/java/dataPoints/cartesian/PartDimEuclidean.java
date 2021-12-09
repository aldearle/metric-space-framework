package dataPoints.cartesian;

import java.util.Collection;

import coreConcepts.Metric;

public class PartDimEuclidean<T extends CartesianPoint> implements Metric<T> {

	Collection<Integer> dims;

	public PartDimEuclidean(Collection<Integer> dims) {
		this.dims = dims;
	}

	@Override
	public double distance(T x, T y) {
		double[] xs = x.getPoint();
		double[] ys = y.getPoint();
		double acc = 0;
		for (int i : this.dims) {
			final double diff = xs[i] - ys[i];
			acc += diff * diff;
		}
		return Math.sqrt(acc);
	}

	@Override
	public String getMetricName() {
		return "euc_part";
	}

}
