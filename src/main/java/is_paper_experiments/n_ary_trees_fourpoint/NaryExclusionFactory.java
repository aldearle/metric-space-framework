package is_paper_experiments.n_ary_trees_fourpoint;

import java.util.List;
import java.util.Random;

import coreConcepts.Metric;

/**
 * @author Richard Connor
 * 
 *         a class to create an instance of an ExclusionMechanism for a given
 *         recursive node, used when building a search tree
 *
 * @param <T>
 *            the type of the metric space data
 * @param <C>
 *            the type of info required during the build context; for simple
 *            mechanisms this may not be required
 * @param <Q>
 *            the type of info required during query; for simple mechanisms this
 *            may not be required
 */
public abstract class NaryExclusionFactory<T, C, Q> {

	protected static class Null {
		// deliberately blank
	}

	protected Random rand;

	protected Metric<T> metric;

	/**
	 * @param metric
	 *            the metric to be used for all uses of the generated Exclusions
	 */
	public NaryExclusionFactory(Metric<T> metric) {
		this.metric = metric;
		this.rand = new Random();
	}

	/**
	 * @param data
	 *            the data used to build the Exclusion
	 * @param context
	 *            the context info required
	 * @return an exclusion mechanisms for use in building/querying a tree
	 */
	public abstract NaryExclusion<T, C, Q> getExclusion(List<T> data, C context);

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
