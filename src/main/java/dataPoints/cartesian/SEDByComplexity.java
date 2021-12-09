package dataPoints.cartesian;

import java.util.List;

import coreConcepts.Metric;

/**
 * Implementation of SED as first defined over CartesianPoint
 * 
 * @author Richard Connor
 * 
 * @param <T>
 *            the type of the objects, any subclass of CartesianPoint
 */
public class SEDByComplexity<T extends CartesianPoint> implements Metric<T> {

	/**
	 * the power to which the "raw" sed calculation is raised to give the
	 * triangle inequality property
	 */
	public static final double FINAL_POWER = 0.483;// 6842144826767;

	@Override
	public double distance(T x, T y) {

		double[] normalisedX = x.getNormalisedPoint();
		double[] normalisedY = y.getNormalisedPoint();
		double[] merge = new double[x.getPoint().length];
		for (int i = 0; i < merge.length; i++) {
			merge[i] = (normalisedX[i] + normalisedY[i]) / 2;
		}

		double e1 = x.getComplexity();
		double e2 = y.getComplexity();
		double e3 = (new CartesianPoint(merge)).getComplexity();

		double t1 = Math.max(0, Math.min(1, (e3 / Math.sqrt(e1 * e2)) - 1));

		if (Double.isNaN(t1)) {
			return 1;
			// throw new RuntimeException("SED trapped a NaN ");
		}

		final double pow = Math.pow(t1, FINAL_POWER);
		return pow;
	}

	/**
	 * Divergence over a list of points. Shouldn't be in this class, just
	 * temporary for convenience!
	 * 
	 * @param points
	 *            The objects over which divergence is returned.
	 * @return The divergence value.
	 */
	public double divergence(List<T> points) {
		/*
		 * divergence is the complexity of the merger over the geometric average
		 * of the complexities, times 1/(n-1)
		 */
		double[] merged = getMergedVector(points);
		double multAcc = 1.0;

		for (CartesianPoint point : points) {
			double c = point.getComplexity();
			multAcc *= c;
		}

		double top = (new CartesianPoint(merged)).getComplexity();
		double bottom = Math.exp(Math.log(multAcc) / points.size());
		final double ratio = top / bottom;
		final double raw = (ratio - 1) * (1 / (double) (points.size() - 1));
		return Math.pow(raw, 0.486);
	}

	@Override
	public String getMetricName() {
		return "sed"; //$NON-NLS-1$
	}

	private double[] getMergedVector(List<T> points) {
		double[] merged = null;
		for (int i = 0; i < points.size(); i++) {
			double[] thisVec = points.get(i).getNormalisedPoint();
			if (i == 0) {
				merged = new double[thisVec.length];
			}
			for (int loc = 0; loc < merged.length; loc++) {
				merged[loc] += thisVec[loc] / points.size();
			}
		}
		return merged;
	}

}
