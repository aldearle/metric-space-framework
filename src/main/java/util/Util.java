package util;

import coreConcepts.DataSet;
import coreConcepts.MetricSpace;
import dataPoints.cartesian.SEDByComplexity;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * some utility functions which don't fit nicely anywhere else
 * 
 * @author Richard Connor
 * 
 */
public class Util {

	/**
	 * Utility method to convert a list to a dataset
	 * 
	 * @param l
	 *            the list
	 * @param name
	 *            the name of the dataset
	 * @param shortName
	 *            the short name of the dataset
	 * @return the new DataSet
	 */
	public static <T> DataSet<T> listToDataSet(final List<T> l,
			final String name, final String shortName) {
		return new DataSet<T>() {
			Random rand = new Random(0);

			@Override
			public Iterator<T> iterator() {
				return l.iterator();
			}

			@Override
			public boolean isFinite() {
				return true;
			}

			@Override
			public T randomValue() {
				return l.get(this.rand.nextInt(l.size()));
			}

			@Override
			public String getDataSetName() {
				return name;
			}

			@Override
			public String getDataSetShortName() {
				return shortName;
			}

			@Override
			public int size() {
				return l.size();
			}
		};
	}

	public <T> T id(T x) {
		return x;
	}

	/**
	 * a (very poor!) estimation of a maximum distance within a space...
	 * 
	 * @param ms
	 *            the metric space
	 * @return
	 */
	public static <T> double estimateMaxValue(MetricSpace<T> ms) {
		double est = 0;
		for (int i = 0; i < 1000; i++) {
			double d = ms.distance(ms.randomValue(), ms.randomValue());
			est = Math.max(est, d);
		}
		return est * 2;
	}

	public static double sedToJs(double sed) {
		double t1 = Math.pow(sed, 1 / SEDByComplexity.FINAL_POWER) + 1;
		double t2 = Math.log(t1) / Math.log(2);
		return Math.sqrt(t2);
	}

	public static double jsToSed(double js) {
		double sq = Math.pow(2, js * js) - 1;
		return Math.pow(sq, SEDByComplexity.FINAL_POWER);
	}
}
