package dataPoints.cartesian;

import coreConcepts.Metric;

/**
 * @author Richard
 * 
 *         a form of Manhattan distance which is a supermetric
 *
 * @param <T>
 */
public class ManhattanH<T extends CartesianPoint> implements Metric<T> {

	@Override
	public double distance(T x, T y) {
		double[] ys = y.getPoint();
		double acc = 0;
		int ptr = 0;
		for (double xVal : x.getPoint()) {
			acc += Math.abs(xVal - ys[ptr++]);
		}
		return Math.sqrt(acc);
	}

	@Override
	public String getMetricName() {
		return "manH";
	}

}
