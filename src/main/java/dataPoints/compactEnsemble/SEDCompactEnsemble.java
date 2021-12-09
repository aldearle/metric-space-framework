package dataPoints.compactEnsemble;

import util.ConstantsAndArith;
import coreConcepts.Metric;

public class SEDCompactEnsemble<T extends CompactEnsemble> implements Metric<T> {

	
	private static double overBarXlogX(double f) {
		return -f * (Math.log(f) / Math.log(2));
	}

	private static double sedAccumulator(int[] fp1, int[] fp2, int card1,
			int card2) {
		double resAcc = 0;
		int fp2pntr = 0;

		for (int x1 : fp1) {

			int event = x1 >> 16;

			while (fp2pntr < fp2.length && (fp2[fp2pntr] >> 16) < event) {
				fp2pntr++;
			}
			if (fp2pntr < fp2.length && (fp2[fp2pntr] >> 16) == event) {

				double x1rel = (x1 % ConstantsAndArith.sixteenBitModMask) / (double) card1;

				double y1rel = (fp2[fp2pntr] % ConstantsAndArith.sixteenBitModMask) / (double) card2;

				resAcc += overBarXlogX(x1rel) + overBarXlogX(y1rel)
						- overBarXlogX(x1rel + y1rel);

				fp2pntr++;
			}

		}

		if (resAcc > 2) {
			resAcc = 2;
		}
		return resAcc;
	}

	@Override
	public double distance(T x, T y) {
		int[] fp1 = x.getEnsemble();
		int[] fp2 = y.getEnsemble();
		int card1 = x.getCardinality();
		int card2 = y.getCardinality();

		double resAcc = sedAccumulator(fp1, fp2, card1, card2);
		double raw = Math.pow(2, 1 - (resAcc / 2)) - 1;
		return Math.pow(raw, 0.486);
	}

	@Override
	public String getMetricName() {
		return "SED Compact Ensemble";
	}

}
