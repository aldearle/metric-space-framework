package dataPoints.doubleArray;

import coreConcepts.Metric;

/**
 * @author Richard Connor
 *
 */
public class ManhattanDoubleArray implements Metric<double[]> {

	/* (non-Javadoc)
	 * @see coreConcepts.Metric#distance(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double distance(double[] x, double[] y) {
		double acc = 0;
		int pointer = 0;
		for( double xd : x){
			double yd = y[pointer++];
			acc += Math.abs(xd - yd);
		}
		return acc;
	}

	@Override
	public String getMetricName() {
		return "Manhattan distance"; //$NON-NLS-1$
	}

}
