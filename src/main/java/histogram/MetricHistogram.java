package histogram;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import coreConcepts.MetricSpace;

/**
 * 
 * beware, this class is currently inconsistent during a refactoring of the main
 * Histogram class... the upper-bound value (typically 1.0) is not judged to be
 * a part of the histogram and is treated as an "over-value" value instead, but
 * the code here probably assumes it is in the histogram... but it might not,
 * it's actually just unchecked rather than definitely wrong!
 * 
 * @author Richard Connor
 * 
 * @param <T>
 */
public class MetricHistogram<T> extends Histogram {

	/**
	 * Contains the essential information of the histogram for use in other
	 * functions
	 * 
	 * @author Richard Connor
	 * 
	 */
	public class HistogramInfo {
		public double meanDistance;

		public double median;
		public double idim;
		public double variance;
		public double stdev;

		public double t5;
		public double t6;
		public double t7;

		public double if5;
		public double if6;
		public double if7;
	}

	private HistogramInfo histInfo = new HistogramInfo();

	private int noOfPoints;
	private double maxValue;
	private List<T> pointsDone = new ArrayList<T>();

	private MetricSpace<T> theMetricSpace;

	private double variance;

	/**
	 * @param theSpace
	 * @param noOfPoints
	 * @param granularity
	 * @param analysisRequired
	 * @param randomValues
	 * @param maxValue
	 */
	public MetricHistogram(MetricSpace<T> theSpace, int noOfPoints,
			int granularity, boolean analysisRequired, boolean randomValues,
			double maxValue) {
		super(granularity, 0, 1);

		this.theMetricSpace = theSpace;

		this.noOfPoints = noOfPoints;
		this.maxValue = maxValue;

		if (randomValues) {
			createRandomHistogram();
		} else {
			createSeriesHistogram();
		}

		if (analysisRequired) {
			setVariance();
			setMuAndMedian();
			setIFPoints();
		}

	}

	private void createRandomHistogram() {
		double meanAccumulator = 0;
		int noOfDistances = 0;

		for (int i = 0; i < this.noOfPoints; i++) {
			T p1 = this.theMetricSpace.randomValue();

			for (T p2 : this.pointsDone) {

				double distance = this.theMetricSpace.distance(p1, p2);
				if (distance > 1.0) {
					System.out.println("Error: distance too big:" + distance
							+ ":" + p1 + ":::" + p2);
				} else {
					this.addValue(distance);
					meanAccumulator += distance;
				}

			}
			noOfDistances += this.pointsDone.size();
			this.pointsDone.add(p1);
		}

		this.histInfo.meanDistance = meanAccumulator / noOfDistances;
	}

	@Deprecated
	public String displayHistogram() {
		StringBuffer res = new StringBuffer();
		for (int f : getTheHistogram()) {
			res.append(f + " , ");
		}
		return res.toString();
	}

	/**
	 * @return the mean distance
	 */
	public double getMeanDistance() {
		return this.histInfo.meanDistance;
	}

	/**
	 * @return the variance
	 */
	public double getVariance() {
		return this.variance;
	}

	public static void printHeaderRow(PrintStream ps) {
		ps.println("space name,mean,median,stdev,idim,t5,t6,t7,if5,if6,if7,data");
	}

	public void printToStream(PrintStream ps) {

		double mean = getMeanDistance();
		double stdev = Math.sqrt(getVariance());
		double idim = (mean * mean) / (2 * stdev * stdev);

		ps.print(this.theMetricSpace.getName() + "," + mean + ","
				+ this.histInfo.median + "," + stdev + "," + idim + ","
				+ this.histInfo.t5 + "," + this.histInfo.t6 + ","
				+ this.histInfo.t7 + "," + this.histInfo.if5 + ","
				+ this.histInfo.if6 + "," + this.histInfo.if7);

		for (int f : getTheHistogram()) {
			ps.print(f + " , ");
		}

		ps.println();
	}

	public void printVertical(PrintStream ps) {

		double mean = getMeanDistance();
		double stdev = Math.sqrt(getVariance());
		double idim = (mean * mean) / (2 * stdev * stdev);

		ps.println("space name,mean,median,stdev,idim,t5,t6,t7,if5,if6,if7");
		ps.println(this.theMetricSpace.getName() + "," + mean + ","
				+ this.histInfo.median + "," + stdev + "," + idim + ","
				+ this.histInfo.t5 + "," + this.histInfo.t6 + ","
				+ this.histInfo.t7 + "," + this.histInfo.if5 + ","
				+ this.histInfo.if6 + "," + this.histInfo.if7);

		ps.println("<histogram starts>");
		for (int f : getTheHistogram()) {
			ps.println(f);
		}

		ps.println("<end of histrogram");
	}

	private void createSeriesHistogram() {
		double meanAccumulator = 0;
		int noOfDistances = 0;

		Iterator<T> it = this.theMetricSpace.iterator();
		for (int i = 0; i < this.noOfPoints; i++) {
			T p1 = it.next();

			for (T p2 : this.pointsDone) {

				double distance = this.theMetricSpace.distance(p1, p2)
						/ this.maxValue;
				if (distance > 1.0) {
					// System.out.println("Error: distance too big:" + distance
					// + ":" + p1 + ":::" + p2);
				} else {
					this.addValue(distance);
					meanAccumulator += distance;
				}

			}
			noOfDistances += this.pointsDone.size();
			this.pointsDone.add(p1);
		}

		this.histInfo.meanDistance = meanAccumulator / noOfDistances;
	}

	/**
	 * @return the essential characteristics of the histogram
	 */
	public HistogramInfo getHistogramInfo() {
		return this.histInfo;
	}

	private void setIFPoints() {
		this.histInfo.if5 = setIFPoint(this.histInfo.t5);
		this.histInfo.if6 = setIFPoint(this.histInfo.t6);
		this.histInfo.if7 = setIFPoint(this.histInfo.t7);
	}

	private double setIFPoint(double threshold) {
		double lwb = this.histInfo.median - threshold
				+ (1 / this.granularity * 2);
		if (lwb < 0) {
			lwb = 0;
		}
		double upb = this.histInfo.median + threshold
				+ (1 / this.granularity * 2);
		if (upb > 1) {
			upb = 1;
		}

		int acc = 0;
		for (int i = numberToBin(lwb); i < numberToBin(upb); i++) {
			acc += this.getTheHistogram()[i];
		}

		return 1 - (acc / (double) this.getValuesInHistogram());
	}

	/**
	 * NB this sets mu and median points to the lower bound of the cell in which
	 * they appear
	 * 
	 */
	private void setMuAndMedian() {
		double muVal5 = this.getValuesInHistogram() / (double) 100000;
		double muVal6 = this.getValuesInHistogram() / (double) 1000000;
		double muVal7 = this.getValuesInHistogram() / (double) 10000000;
		int acc = 0;
		int half = this.getValuesInHistogram() / 2;
		for (int i = 1; i < getTheHistogram().length; i++) {
			if (acc < half) {
				double currentLwb = binToLwb(i);
				this.histInfo.median = currentLwb;
				if (acc < muVal5) {
					this.histInfo.t5 = currentLwb;
					if (acc < muVal6) {
						this.histInfo.t6 = currentLwb;
						if (acc < muVal7) {
							this.histInfo.t7 = currentLwb;
						}
					}
				}
			}
			acc += getTheHistogram()[i];
		}

		double mean = getMeanDistance();
		this.histInfo.variance = getVariance();
		this.histInfo.stdev = Math.sqrt(this.histInfo.variance);
		this.histInfo.idim = (mean * mean)
				/ (2 * this.histInfo.stdev * this.histInfo.stdev);
	}

	private void setVariance() {

		int point2pos = 1;
		double varianceAccumlator = 0;

		for (T p1 : this.pointsDone) {
			for (int i = point2pos++; i < this.noOfPoints; i++) {
				T p2 = this.pointsDone.get(i);
				double distance = this.theMetricSpace.distance(p1, p2)
						/ this.maxValue;

				double varAcc = (distance - this.histInfo.meanDistance);
				varianceAccumlator += (varAcc * varAcc);
			}
		}

		this.variance = varianceAccumlator / (this.getValuesInHistogram() - 1);

	}
}
