package query;

import java.util.ArrayList;
import java.util.List;

import util.OrderedList;
import coreConcepts.DataSet;
import coreConcepts.Metric;

/**
 * @author Richard Connor
 * 
 */
public class Exhaustive<T> implements MetricIndex<T> {

	private DataSet<T> theData;
	private Metric<T> theMetric;

	/**
	 * @param data
	 *            the DataSet to be searched
	 * @param metric
	 *            the metric to be used, does not require to be a proper
	 *            distance!
	 * @param item
	 *            the item for which the most similar items are to be found
	 * @param nn
	 *            the number of matches to return
	 * 
	 * @return an OrderedList with the closest results from the DataSet, given
	 *         in terms of their positions within the DataSet iterator. If the
	 *         item is itself in the dataset, it will not be included in the
	 *         result list
	 */
	@SuppressWarnings("boxing")
	public static <T> OrderedList<Integer, Double> exhaustiveSearch(
			DataSet<T> data, Metric<T> metric, T item, int nn) {
		OrderedList<Integer, Double> res = new OrderedList<Integer, Double>(nn);

		double threshold = Double.MAX_VALUE;

		int pointer = 0;
		for (T point : data) {
			if (point != item) {
				double dist = metric.distance(item, point);
				if (dist < threshold) {
					res.add(pointer, dist);
					final Double thresh = res.getThreshold();
					if (thresh != null) {
						threshold = thresh;
					}
				}
			}
			pointer++;
		}

		return res;
	}

	Exhaustive(DataSet<T> data, Metric<T> metric) {
		this.theData = data;
		this.theMetric = metric;
	}

	@Override
	public List<T> thresholdQuery(T query, double threshold) {
		List<T> res = new ArrayList<T>();

		for (T point : this.theData) {
			if (point != query) {
				double dist = this.theMetric.distance(query, point);
				if (dist < threshold) {
					res.add(point);
				}
			}
		}

		return res;
	}
}
