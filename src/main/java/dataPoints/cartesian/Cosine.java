package dataPoints.cartesian;

import coreConcepts.Metric;

/**
 * 
 * distance is the angle between two points, normalised into the range [0,1],
 * this implementation is only defined in the positive domain of Cartesian space
 * 
 * this gives the same ordering as normal "Cosine Distance" but is also a
 * distance metric
 * 
 * @author Richard Connor
 * 
 */
public class Cosine<T extends CartesianPoint> implements Metric<T> {

	@Override
	public double distance(T point1, T point2) {

		double dotProduct = 0;
		for (int i = 0; i < point1.getPoint().length; i++) {
			dotProduct += point1.getPoint()[i] * point2.getPoint()[i];
		}
		double magProduct = point1.getMagnitude() * point2.getMagnitude();

		if (magProduct == 0) {
			return 1;
			// throw new RuntimeException(
			// "can't calculate cosine distance from origin");
		}

		double rawCosDist = dotProduct / magProduct;

		rawCosDist = Math.max(-1, Math.min(1, rawCosDist));

		/*
		 * if the data contains negative values then the angle can range between
		 * 0 and pi, so this function is bounded in [0,2] rather than [0,1]
		 */
		double angDist = Math.acos(rawCosDist) / (Math.PI / 2);

		return angDist;
	}

	@Override
	public String getMetricName() {
		return "cos";
	}

}
