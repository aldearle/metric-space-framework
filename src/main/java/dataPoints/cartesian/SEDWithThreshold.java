package dataPoints.cartesian;

import java.util.List;

import coreConcepts.MetricWithThreshold;

/**
 * 
 * SED implementation with threshold cutoff.
 * 
 * @author Richard Connor
 * 
 */
public class SEDWithThreshold<T extends CartesianPoint> extends
		SEDByComplexity<T> implements
		MetricWithThreshold<T> {

	static final double log2 = Math.log(2);
	private int noOfComparisons = 0;

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
		int n = points.size();
		double[] merged = getMergedVector(points, n);
		double multAcc = 1.0;

		for (CartesianPoint point : points) {
			double c = point.getComplexity();
			multAcc *= c;
		}

		double top = (new CartesianPoint(merged)).getComplexity();
		double bottom = Math.exp(Math.log(multAcc) / n);
		final double ratio = top / bottom;
		final double raw = (ratio - 1) * (1 / (double) (n - 1));
		return Math.pow(raw, 0.486);
	}

	@Override
	public String getMetricName() {
		return "SED with threshold cutoff"; //$NON-NLS-1$
	}

	@Override
	public double getThreshold(double distance) {

		double rawThreshold = Math.pow(Math.E, Math.log(distance) / 0.486);
		double simRequired = 2 * (1 - (Math.log(rawThreshold + 1) / Math.log(2)));
		return 2 - simRequired;
	}

	@Override
	public int noOfComparisons() {
		int res = this.noOfComparisons;
		this.noOfComparisons = 0;
		return res;
	}

	@Override
	public double thresholdDistance(T x, T y,
			double threshold) {
		double[] normalisedX = x.getNormalisedPoint();
		double[] normalisedY = y.getNormalisedPoint();
		double[] XlogTerms = x.getLog2Terms();
		double[] YlogTerms = y.getLog2Terms();

		double resAcc = 0;
		double x1Bar = 1.0;
		double y1Bar = 1.0;
		double bestResidue = 2;
		int arrayPointer = 0;

		List<Integer> magOrdering = x.magnitudeOrdering();

		while (arrayPointer < normalisedX.length
				&& (resAcc + bestResidue >= 2 - threshold)) {

			@SuppressWarnings("boxing")
			int thisDim = magOrdering.get(arrayPointer++);

			double x1rel = normalisedX[thisDim];
			double y1rel = normalisedY[thisDim];

			x1Bar -= x1rel;
			y1Bar -= y1rel;
			bestResidue = bestResidual(x1Bar, y1Bar);

			if (x1rel != 0 && y1rel != 0) {

				double term1 = XlogTerms[thisDim];
				double term2 = YlogTerms[thisDim];
				double term3 = (x1rel + y1rel) * Math.log((x1rel + y1rel))
						/ log2;

				resAcc += term1 + term2 + term3;
			}
		}
		this.noOfComparisons = arrayPointer;

		if (resAcc + bestResidue < 2 - threshold) {
			return -1;
		} else {
			if (resAcc > 2) {
				return calculateFinal(2);
			} else {
				return calculateFinal(resAcc);
			}
		}
	}

	private static double bestResidual(double pBar, double qBar) {
		double term1 = pBar * Math.log(pBar) / log2;
		double term2 = qBar * Math.log(qBar) / log2;
		double term3 = (pBar + qBar) * Math.log(pBar + qBar) / log2;
		return term3 - term1 - term2;
	}

	private static double calculateFinal(double resAcc) {
		assert resAcc <= 2.0;
		double raw = Math.pow(2, 1 - (resAcc / 2)) - 1;
		final double res = Math.pow(raw, 0.486);
		return res;
	}

	@SuppressWarnings("static-method")
	private double[] getMergedVector(List<T> points, int n) {
		double[] merged = null;
		for (int i = 0; i < n; i++) {
			double[] thisVec = points.get(i).getNormalisedPoint();
			if (i == 0) {
				merged = new double[thisVec.length];
			}
			for (int loc = 0; loc < merged.length; loc++) {
				merged[loc] += thisVec[loc] / n;
			}
		}
		return merged;
	}

}
