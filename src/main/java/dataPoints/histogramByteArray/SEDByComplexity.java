package dataPoints.histogramByteArray;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

/**
 * @author Richard Connor
 *
 * @param <T>
 */
public class SEDByComplexity<T extends HistogramByteArray> implements Metric<T> {

	@Override
	public double distance(T x, T y) {

		double e1 = x.getComplexity();
		double e2 = y.getComplexity();

		double e3 = HistogramByteArray.getMergedComplexity(x, y);

		double t1 = Math.max(0, Math.min(1, (e3 / Math.sqrt(e1 * e2)) - 1));

		if (Double.isNaN(t1)) {
			throw new RuntimeException("SED trapped a NaN ");
		}

		final double pow = Math.pow(t1, 0.486);
		return pow;
	}

	@Override
	public String getMetricName() {
		return "SED: histogram byte array by complexity";
	}
}
