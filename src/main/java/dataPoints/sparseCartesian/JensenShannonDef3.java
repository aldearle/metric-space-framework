package dataPoints.sparseCartesian;

import coreConcepts.Metric;

/**
 * This implements the Jensen-Shannon distance according to a term rewrite in a
 * paper for SISAP 2013, with a threshold cutoff
 * 
 * @author Richard Connor
 * 
 * @param <T>
 *            the subclass of SparseCartesian
 */
public class JensenShannonDef3<T extends SparseCartesian> implements Metric<T> {

	private static final double LOG2 = Math.log(2);

	private static double xLogx(double x) {
		return x * (Math.log(x));
	}

	private double accThreshold;
	private double threshold;

	/**
	 * This is a threshold function; the constructor takes a threshold value,
	 * any distance asked for which would be over that threshold may be returned
	 * as -1
	 * 
	 * @param threshold
	 */
	public JensenShannonDef3(double threshold) {
		this.threshold = threshold;
		this.accThreshold = 2 * threshold * threshold;
	}

	/**
	 * @param v
	 * @param w
	 * @param xVals
	 * @param yVals
	 * @return
	 * @throws Exception
	 */
	protected double iterate(int[] v, int[] w, double[] vVals, double[] wVals)
			throws Exception {

		double acc = 0;
		double vMaxAcc = 0;

		int vPntr = 0;
		int wPntr = 0;
		for (int v_event_id : v) {

			final double v_i = vVals[vPntr];
			vMaxAcc += 2 * v_i;

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

			final double accAdjusted = acc / LOG2;
			if (accAdjusted < (vMaxAcc - this.accThreshold)) {
				throw new Exception("threshold exceeded");
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
		return "Jensen Shannon Definition 3 (" + this.threshold + ")";
	}

	@Override
	public double distance(T x, T y) {
		int[] xIds = x.getDims();
		int[] yIds = y.getDims();
		double[] xVals = x.getValues();
		double[] yVals = y.getValues();

		try {
			double sum = iterate(xIds, yIds, xVals, yVals);
			if (sum > 2) {
				sum = 2;
			}

			return Math.sqrt(1 - (sum / 2));
		} catch (Exception e) {
			return -1;
		}
	}
}
