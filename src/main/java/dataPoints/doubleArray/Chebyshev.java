package dataPoints.doubleArray;

import util.Range;
import coreConcepts.Metric;

public class Chebyshev implements Metric<double[]> {

	@Override
	public double distance(double[] x, double[] y) {
		double res = 0;
		for (int i : Range.range(0, x.length)) {
			res = Math.max(res, Math.abs(x[i] - y[i]));
		}
		return res;
	}

	@Override
	public String getMetricName() {
		return "cheby";
	}

}
