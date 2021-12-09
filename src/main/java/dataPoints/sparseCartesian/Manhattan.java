package dataPoints.sparseCartesian;

import coreConcepts.Metric;

public class Manhattan<T extends SparseCartesian> implements Metric<T> {

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
				final double w_i = wVals[wPntr];
				acc += w_i;

				wPntr++;
			}
			if (wPntr < w.length && w[wPntr] == v_event_id) {

				final double w_i = wVals[wPntr];

				acc += Math.abs(v_i - w_i);

				wPntr++;
			} else {
				/*
				 * here do anything required for events in v which are not in w
				 */

				acc += v_i;
			}
			vPntr++;
		}
		while (wPntr < w.length) {
			/*
			 * examine the residual terms in w
			 */

			final double w_i = wVals[wPntr];
			acc += w_i;

			wPntr++;
		}

		return acc;
	}

	@Override
	public String getMetricName() {
		return "Manhattan";
	}

	@Override
	public double distance(T x, T y) {

		int[] xIds = x.getDims();
		int[] yIds = y.getDims();
		double[] xVals = x.getValues();
		double[] yVals = y.getValues();

		double sum = iterate(xIds, yIds, xVals, yVals);

		return sum;
	}
}
