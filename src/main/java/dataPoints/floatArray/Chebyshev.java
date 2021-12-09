package dataPoints.floatArray;

import coreConcepts.Metric;
import coreConcepts.MetricWithThreshold;

/**
 * @author Richard Connor
 * 
 *         Tshebyshev, or Chebyshev, or Lebesque_\infty distance
 * 
 */
public class Chebyshev implements MetricWithThreshold<float[]> {

	public double distance2(float[] x, float[] y) {
		double res = 0;
		for (int i = 0; i < x.length; i++) {
			res = Math.max(res, Math.abs(x[i] - y[i]));
		}
		return res;
	}

	public double distanceP(float[] x, float[] y) {
		double max = 0;
		double min = 0;
		for (int i = 0; i < x.length; i++) {
			// acc = Math.max(acc, Math.abs(d - ys[ptr++]));
			double diff = x[i] - y[i];
			if (diff < min) {
				min = diff;
			} else if (diff > max) {
				max = diff;
			}
		}

		if (max > -min)
			return max;
		else
			return -min;
	}

	public double distance(float[] x, float[] y) {
		double max = 0;
		for (int i = 0; i < x.length; i++) {
			double diff = x[i] - y[i];
			if (diff < 0) {
				diff = -diff;
			}
			if (diff > max) {
				max = diff;
			}
		}

		return max;
	}

	@Override
	public String getMetricName() {
		return "ChbFloatArray";
	}

	@Override
	public double thresholdDistance(float[] x, float[] y, double threshold) {
		double max = 0;
		int ptr = 0;
		while (ptr < x.length && max < threshold) {
			double diff = x[ptr] - y[ptr++];
			if (diff < 0) {
				diff = -diff;
			}
			if (diff > max) {
				max = diff;
			}

		}
		if (max < threshold) {
			return max;
		} else {
			return -1;
		}
	}

	@Override
	public double getThreshold(double distance) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int noOfComparisons() {
		// TODO Auto-generated method stub
		return 0;
	}

}
