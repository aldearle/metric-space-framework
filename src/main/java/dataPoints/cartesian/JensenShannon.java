package dataPoints.cartesian;

import coreConcepts.Metric;

public class JensenShannon<T extends CartesianPoint> implements Metric<T> {

	private static final double LOG_2 = Math.log(2);
	private boolean logOpt;
	private boolean entropyOpt;

	public JensenShannon(boolean logOpt, boolean entropyOpt) {
		this.logOpt = logOpt;
		this.entropyOpt = entropyOpt;
	}

	@Override
	public double distance(T v, T w) {
		double[] vNorm = v.getNormalisedPoint();
		double[] wNorm = w.getNormalisedPoint();
		double[] vLogs = null;
		double[] wLogs = null;
		if (this.logOpt) {
			vLogs = v.getLog2Terms();
			wLogs = w.getLog2Terms();
		}

		double acc = divergence(vNorm, wNorm, vLogs, wLogs);
		if (this.entropyOpt) {
			acc += (v.getEntropy() / LOG_2);
			acc += (w.getEntropy() / LOG_2);
		}
		return Math.sqrt(1 - acc / 2);
	}

	public static double divergence(double[] vNorm, double[] wNorm) {
		double acc = 0;
		for (int i = 0; i < vNorm.length; i++) {
			final double v_i = vNorm[i];
			if (v_i != 0) {
				final double w_i = wNorm[i];
				if (w_i != 0) {
					double sum = v_i + w_i;
					double logsum = Math.log(sum);
					acc += sum * logsum;

					acc -= v_i * (Math.log(v_i));
					acc -= w_i * (Math.log(w_i));
				}
			}
		}
		return 2 * Math.log(2) - acc;
	}

	protected double divergence(double[] vNorm, double[] wNorm, double[] vLogs,
			double[] wLogs) {
		double acc = 0;
		for (int i = 0; i < vNorm.length; i++) {
			final double v_i = vNorm[i];
			if (v_i != 0 || entropyOpt) {
				final double w_i = wNorm[i];
				if (w_i != 0 || entropyOpt) {
					double sum = v_i + w_i;
					if (sum != 0) {
						double logsum = Math.log(sum) / LOG_2;
						acc += sum * logsum;
					}
					if (this.logOpt) {
						acc += vLogs[i];
						acc += wLogs[i];
					} else if (!this.entropyOpt) {
						acc -= v_i * (Math.log(v_i) / LOG_2);
						acc -= w_i * (Math.log(w_i) / LOG_2);
					}
				}
			}
		}

		return acc;
	}

	@Override
	public String getMetricName() {
		return "jsd";
	}

}
