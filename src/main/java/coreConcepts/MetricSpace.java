package coreConcepts;

import java.util.Iterator;

/**
 * @author Richard Connor
 * 
 *         This class simulates multiple inheritance from an instance of a
 *         DataSet and a Metric to provide a metric space implementations. It
 *         adds no functionality and simply delegates method calls to the
 *         appropriate object used at construction.
 * 
 * @param <T>
 *            The class of the objects in the Metric Space
 */
public class MetricSpace<T> implements DataSet<T>, Metric<T> {

	private Metric<T> metric;
	private DataSet<T> data;

	/**
	 * Creates a new MetricSpace object
	 * 
	 * @param theDataSet
	 *            the DataSet
	 * @param theMetric
	 *            the Metric
	 */
	public MetricSpace(DataSet<T> theDataSet, Metric<T> theMetric) {
		this.data = theDataSet;
		this.metric = theMetric;
	}

	@Override
	public double distance(T x, T y) {
		return this.metric.distance(x, y);
	}

	/**
	 * @return the metric used to construct the space
	 */
	public Metric<T> getMetric() {
		return this.metric;
	}

	@Override
	public T randomValue() {
		return this.data.randomValue();
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public boolean isFinite() {
		return this.data.isFinite();
	}

	@Override
	public Iterator<T> iterator() {
		return this.data.iterator();
	}

	/**
	 * @return a name for the metric space, the DataSet and Metric names
	 *         concatenated around a colon
	 */
	public String getName() {
		return this.data.getDataSetName() + " : " + this.metric.getMetricName(); //$NON-NLS-1$
	}

	@Override
	public String getDataSetName() {
		return this.data.getDataSetName();
	}

	@Override
	public String getMetricName() {
		return this.metric.getMetricName();
	}

	@Override
	public String getDataSetShortName() {
		return this.data.getDataSetShortName();
	}

}
