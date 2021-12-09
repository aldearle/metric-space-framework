package is_paper_experiments.binary_partitions;

import java.util.List;

import coreConcepts.Metric;

/**
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data
 */
public abstract class BinaryPartition<T> {

	protected List<T> data;
	protected Metric<T> metric;

	public BinaryPartition(List<T> data, Metric<T> metric) {
		this.data = data;
		this.metric = metric;
		this.intialise();
	}

	protected abstract void intialise();

	/**
	 * @return a list of precisely two elements to be used as reference points
	 */
	public abstract List<T> getReferencePoints();

	/**
	 * @return the data minus any reference points selected from it
	 */
	public abstract List<T> getData();
}
