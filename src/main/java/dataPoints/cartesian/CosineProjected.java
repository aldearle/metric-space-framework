package dataPoints.cartesian;

import coreConcepts.Metric;

/**
 * 
 * distance is the projection of the angle between two points onto a vertical
 * line (x=1) in 2D Cartesian space
 * 
 * this gives the same ordering as normal "Cosine Distance" but is not a
 * proper metric
 * 
 * it is defined only in positive Cartesian space (of arbitrary dimension) but
 * is undefined where the angle is 90 degrees, in which case this function
 * returns Double.MAX_VALUE which is a pretty safe thing to do...
 * 
 * @author Richard Connor
 * 
 */
public class CosineProjected implements Metric<CartesianPoint> {

	@Override
	public double distance(CartesianPoint point1, CartesianPoint point2) {

		double dotProduct = 0;
		for (int i = 0; i < point1.getPoint().length; i++) {
			dotProduct += point1.getPoint()[i] * point2.getPoint()[i];
		}
		double magProduct = point1.getMagnitude() * point2.getMagnitude();

		double cosine = dotProduct / magProduct;
		/*
		 * the height on the projection is...
		 * 
		 * the cosine equals the adjacent side over the hypoteneuse the adjacent
		 * side is 1 so hypoteneuse is one over cosine
		 */
		if (cosine == 0) {
			return 1;
		} else if (cosine >= 1) {
			return 0;
		} else {
			double hyp = 1 / cosine;
			double opp = Math.sqrt((hyp * hyp) - 1);

			if( opp > 2){
				opp = 2;
			}
			return opp / 2;
		}
	}

	@Override
	public String getMetricName() {
		return "cos";
	}

}
