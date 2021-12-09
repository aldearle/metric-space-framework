package histogram;

import java.io.PrintWriter;

/**
 * 
 * A core histogram class. Results are counted in a number of bins evenly
 * distributed across a range. A value equal to the lower bound of a bin is
 * placed in that bin; thus for example if the range given is zero to one,
 * values of one will not be placed in the histogram..
 * 
 * @author Richard Connor
 * 
 */
public class Histogram {

	private int[] theHistogram;

	/**
	 * @return the theHistogram
	 */
	public int[] getTheHistogram() {
		return this.theHistogram;
	}

	private int valuesInHistogram;
	protected int granularity;

	private double lwb;
	private double upb;
	private double range;

	private int valuesOverUpb;
	private int valuesBelowLwb;

	/**
	 * 
	 * @param noOfBins
	 *            The number of bins in the histogram
	 * @param rangeLwb
	 *            the lowest value to be tracked
	 * @param rangeUpb
	 *            a non-inclusive upper bound greater than the highest value to
	 *            be tracked; values of rangeUpb will not be added to the
	 *            histogram
	 */
	public Histogram(int noOfBins, double rangeLwb, double rangeUpb) {
		assert rangeLwb < rangeUpb;

		this.theHistogram = new int[noOfBins];
		this.granularity = noOfBins;
		this.lwb = rangeLwb;
		this.upb = rangeUpb;
		this.range = rangeUpb - rangeLwb;
	}

	protected int numberToBin(double d) {
		int res = (int) Math.floor((d - this.lwb)
				* (this.granularity / this.range));
		return res;
	}

	protected double binToLwb(int bin) {
		double res = 0;
		res = this.lwb + (this.range / this.granularity) * bin;
		return res;
	}

	/**
	 * add a value to the histogram
	 * 
	 * @param d
	 *            the value to be added
	 */
	public void addValue(double d) {
		try {
			this.theHistogram[numberToBin(d)]++;
			this.valuesInHistogram++;
		} catch (Throwable t) {
			if (d < this.lwb) {
				this.valuesBelowLwb++;
			} else if (d >= this.upb) {
				this.valuesOverUpb++;
			}
		}
	}

	/**
	 * @return the valuesInHistogram
	 */
	public int getValuesInHistogram() {
		return this.valuesInHistogram;
	}

	/**
	 * @return the valuesOverUpb
	 */
	public int getValuesOverUpb() {
		return this.valuesOverUpb;
	}

	/**
	 * @return the valuesBelowLwb
	 */
	public int getValuesBelowLwb() {
		return this.valuesBelowLwb;
	}

	/**
	 * prints the histogram as a series of comma=separated values terminated by
	 * a new line
	 * 
	 * @param pw
	 *            PrintWriter for the output
	 */
	public void printToCSVFormat(PrintWriter pw) {
		for (int i : this.theHistogram) {
			pw.print(i);
			pw.print(",");
		}
		pw.print("\n");
	}

	/**
	 * prints the histogram as a series of space=separated values terminated by
	 * a new line
	 * 
	 * @param pw
	 *            PrintWriter for the output
	 */
	public void printToSpacedFormat(PrintWriter pw) {
		for (int i : this.theHistogram) {
			pw.print(i);
			pw.print(" ");
		}
		pw.print("\n");
	}

}
