package is_paper_experiments.dynamic_binary_partitions;

import java.util.List;

import coreConcepts.Metric;

/**
 * 
 * basic type of classes which generate Exclusion mechanisms for use in building
 * search trees
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of data begin searched
 */
public abstract class BinaryExclusionFactory<T, C, Q> {

	protected Metric<T> metric;

	/**
	 * @param metric
	 *            the metric to be used for all uses of the generated Exclusions
	 */
	public BinaryExclusionFactory(Metric<T> metric) {
		this.metric = metric;
	}

	/**
	 * @param data
	 *            the data used to build the Exclusion
	 * @param context
	 *            the context info required
	 * @return an exclusion mechanisms for use in building/querying a tree
	 */
	public abstract BinaryExclusion<T, C, Q> getExclusion(List<T> data,
			C context);

	/**
	 * @return some meaningful name for experimental results to be annotated
	 */
	public abstract String getName();

	/**
	 * @return the metric used for this ExclusionFactory
	 */
	public Metric<T> getMetric() {
		return this.metric;
	}

}
