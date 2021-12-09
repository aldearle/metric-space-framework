package dataPoints.sparseCartesian;

import coreConcepts.Metric;

/**
 * This implements the Jensen-Shannon distance according to a term rewrite in a
 * paper by me! It should be v fast...
 * 
 * @author Richard Connor
 * 
 * @param <T>
 *            the subclass of SparseCartesian
 */
public class JensenShannonDef2<T extends SparseCartesian> implements Metric<T> {

	private static final double LOG2 = Math.log(2);

	private static double xLogx(double x) {
		return x * (Math.log(x));
	}

	/**
	 * @param v
	 * @param w
	 * @param xVals
	 * @param yVals
	 * @return
	 */
	protected static double iterate(int[] v, int[] w, double[] vVals,
			double[] wVals) {

		double acc = 0;

		int vPntr = 0;
		int wPntr = 0;
		for (int v_event_id : v) {

			final double v_i = vVals[vPntr];

			while (wPntr < w.length && w[wPntr] < v_event_id) {
				/*
				 * here do anything required for events in w which are not in v
				 */
				wPntr++;

			}
			if (wPntr < w.length && w[wPntr] == v_event_id) {

				final double w_i = wVals[wPntr];

				if (v_i == w_i) {
					acc += 2 * v_i * LOG2;
				} else {

					final double vLogv = xLogx(v_i);
					final double wLogw = xLogx(w_i);
					final double sum = xLogx(v_i + w_i);

					acc += sum - vLogv - wLogw;
				}

				wPntr++;
			} else {
				/*
				 * here do anything required for events in v which are not in w
				 * - in this case nothing at all
				 */

			}
			vPntr++;
		}
		/*
		 * no requirement to examine the residual terms in w otherwise would
		 * need to do something here
		 */

		return acc / LOG2;
	}

	@Override
	public String getMetricName() {
		return "Jensen Shannon Definition 2";
	}

	@Override
	public double distance(T x, T y) {
		int[] xIds = x.getDims();
		int[] yIds = y.getDims();
		double[] xVals = x.getValues();
		double[] yVals = y.getValues();

		double sum = iterate(xIds, yIds, xVals, yVals);
		if (sum > 2) {
			sum = 2;
		}

		return Math.sqrt(1 - (sum / 2));
	}
}
