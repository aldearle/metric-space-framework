package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Measurements {

	protected class Mment {
		protected List<Integer> mments;
		protected int acc;
		protected double mean;
		protected double stdev;
		protected double stderr;

		Mment() {
			this.mments = new ArrayList<>();
			this.acc = 0;
		}

		protected void add(int val) {
			this.mments.add(val);
			this.acc += val;
			this.mean = (double) acc / mments.size();
			int stdAcc = 0;
			for (int mment : mments) {
				stdAcc += (mean - mment) * (mean - mment);
			}
			this.stdev = Math.sqrt(stdAcc / mments.size());
			this.stderr = (this.stdev / Math.sqrt(mments.size()));
		}
	}

	private Map<String, Mment> measurements;

	public Measurements(String... names) {
		this.measurements = new TreeMap<>();
		for (String s : names) {
			this.measurements.put(s, new Mment());
		}
	}

	public Measurements() {
		this.measurements = new TreeMap<>();
	}

	public void addMeasurement(String name) {
		this.measurements.put(name, new Mment());
	}

	public void addCount(String name, int number) {
		try {
			this.measurements.get(name).add(number);
		} catch (Throwable t) {
			throw new RuntimeException("wrong name used for measurement");
		}
	}

	public double getMean(String name) {
		return this.measurements.get(name).mean;
	}

	public double getSD(String name) {
		return this.measurements.get(name).stdev;
	}

	public double getStdErrorOfMean(String name) {
		return this.measurements.get(name).stderr;
	}

	/**
	 * @param name
	 *            the name of the measurement
	 * @param acceptablePercentage
	 *            where 1 is 100%, 0.05 is 5% etc
	 * @return whether the measurment has a standard error of the mean within
	 *         the acceptable percentage
	 */
	public boolean done(String name, int acceptablePercentage) {
		double m = getMean(name);
		double err = getStdErrorOfMean(name);
		if (err < m * acceptablePercentage) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param acceptablePercentage
	 *            where 1 is 100%, 0.05 is 5% etc
	 * @return whether all measurements here have standard errors of mean within
	 *         the acceptable percentage
	 */
	public boolean allDone(double acceptablePercentage) {

		boolean allDone = true;
		for (String s : this.measurements.keySet()) {
			if (this.measurements.get(s).mments.size() < 3) {
				allDone = false;
			} else {
				double m = getMean(s);
				double err = getStdErrorOfMean(s);
				if (err > m * acceptablePercentage) {
					allDone = false;
				}
			}
		}
		return allDone;
	}

	public void spewResults() {
		for (String s : this.measurements.keySet()) {
			System.out.print(s + "_mean\t" + s + "_sd\t" + s + "_stderr\t");
		}
		System.out.println();
		for (String s : this.measurements.keySet()) {
			System.out.print(getMean(s) + "\t" + getSD(s) + "\t"
					+ getStdErrorOfMean(s) + "\t");
		}
		System.out.println();
	}
}