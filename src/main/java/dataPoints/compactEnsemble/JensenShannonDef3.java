package dataPoints.compactEnsemble;

import coreConcepts.Metric;

/**
 * This implements the Jensen-Shannon distance according to the most basic
 * (iterative) definition ie 2H((x+y)/2) - H(x) = H(y) expanded to a single sum
 * term over the dimensions of x and y
 * 
 * @author Richard Connor
 * 
 * @param <T>
 *            the subclass of CompactEnsemble
 */
public class JensenShannonDef3<T extends CompactEnsemble> implements Metric<T> {

	private static final double LOG2 = Math.log(2);

	private static double xLogx(double x) {
		if (x == 0) {
			return 0;
		} else {
			return x * (Math.log(x));
		}
	}

	private double accThreshold;

	/**
	 * This is a threshold function; the constructor takes a threshold value,
	 * any distance asked for which would be over that threshold may be returned
	 * as -1
	 * 
	 * @param threshold
	 */
	public JensenShannonDef3(double threshold) {
		this.accThreshold = 2 * threshold * threshold;
	}

	/**
	 * This method iterates over two vectors constructed according to the
	 * EventToIntegerMap.
	 * 
	 * @param v
	 * @param w
	 * @throws Exception
	 */
	protected double iterate(int[] v, int[] w, int vCard, int wCard)
			throws Exception {

		double acc = 0;
		double vMaxAcc = 0;

		int wPntr = 0;
		for (int v_i_encoded : v) {

			int v_event = EventToIntegerMap.getEventCode(v_i_encoded);
			final double v_i = EventToIntegerMap.getCard(v_i_encoded)
					/ (double) vCard;

			vMaxAcc += 2 * v_i;

			while (wPntr < w.length
					&& EventToIntegerMap.getEventCode(w[wPntr]) < v_event) {
				/*
				 * here do anything required for events in w which are not in v
				 */

				wPntr++;
			}
			if (wPntr < w.length
					&& EventToIntegerMap.getEventCode(w[wPntr]) == v_event) {

				final double w_i = EventToIntegerMap.getCard(w[wPntr])
						/ (double) wCard;

				if (v_i == w_i) {
					acc += v_i * 2 * LOG2;
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
				 */

			}

			final double accAdjusted = acc / LOG2;
			if (accAdjusted < (vMaxAcc - this.accThreshold)) {
				throw new Exception("threshold exceeded");
			}

		}

		return acc / LOG2;
	}

	@Override
	public double distance(CompactEnsemble x, CompactEnsemble y) {
		int c1 = x.getCardinality();
		int c2 = y.getCardinality();

		int[] v = x.getEnsemble();
		int[] w = y.getEnsemble();

		try {
			double sum = iterate(v, w, c1, c2);

			if (sum > 2) {
				return 0;
			} else {
				return Math.sqrt(1 - (sum / 2));
			}

		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public String getMetricName() {
		return "Jensen Shannon Definition 1";
	}
}
