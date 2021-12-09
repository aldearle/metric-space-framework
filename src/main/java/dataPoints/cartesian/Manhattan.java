package dataPoints.cartesian;

import coreConcepts.Metric;

public class Manhattan<T extends CartesianPoint> implements Metric<T>{

	@Override
	public double distance(T x, T y) {
		double[] ys = y.getPoint();
		double acc = 0;
		int ptr = 0;
		for( double xVal : x.getPoint()){
			acc += Math.abs(xVal - ys[ptr++]);
		}
		return acc;
	}

	@Override
	public String getMetricName() {
		return "man";
	}

}
