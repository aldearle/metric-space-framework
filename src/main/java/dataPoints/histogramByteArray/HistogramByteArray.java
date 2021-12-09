package dataPoints.histogramByteArray;


public class HistogramByteArray {

	private static int byteValueTotal = -1;
	private static int numberOfBytes = -1;
	private static double[] doubleLogTerms = new double[511];

	private byte[] point;
	private double complexity = -1;

	/**
	 * @param initialValue
	 */
	public HistogramByteArray(byte[] initialValue) {
		this.point = initialValue;
		if (numberOfBytes == -1) {
			// this is the first call in the current invocation
			numberOfBytes = initialValue.length;
			byteValueTotal = getTotal(initialValue);
			/*
			 * the maximum value is 255; the maximum sum is therefore 510
			 */

			for (int i = 0; i < 511; i++) {
				final double d = ((double) i) / ((double) byteValueTotal * 2);
				doubleLogTerms[i] = -d * Math.log(d);
			}

		} else {
			// and all the others must share these values!
			assert numberOfBytes == initialValue.length : "HistogramByteArray constructed with inconsistent array length";
			assert byteValueTotal == getTotal(initialValue) : "HistogramByteArray constructed with inconsistent array total";
		}

	}

	public byte[] getPoint() {
		return this.point;
	}

	private static double getComplexity(byte[] array) {
		double entropyAcc = 0;
		for (byte b : array) {
			final int val = 2 * (b & 0xFF);
			if (val != 0) {
				entropyAcc += doubleLogTerms[val];
			}
		}
		return Math.pow(Math.E, entropyAcc);
	}

	/**
	 * @param x
	 * @param y
	 * @return the complexity of the merged byte vector
	 */
	public static double getMergedComplexity(HistogramByteArray x, HistogramByteArray y) {
		double entropyAcc = 0;
		
		for (int i = 0; i < x.point.length; i++) {
			final int val = (x.point[i] & 0xFF) + (y.point[i] & 0xFF);
			if (val != 0) {
				entropyAcc += doubleLogTerms[val];
			}
		}
		return Math.pow(Math.E, entropyAcc);
	}

	public double getComplexity() {
		if (this.complexity == -1) {
			this.complexity = getComplexity(this.point);
		}
		return this.complexity;
	}

	private static int getTotal(byte[] initialValue) {
		int res = 0;
		for (byte b : initialValue) {
			res += b & 0xFF;
		}
		return res;
	}
}
