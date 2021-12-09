package is_paper_experiments.dynamic_binary_partitions;


import is_paper_experiments.dynamic_binary_partitions.SearchTree.Null;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

/**
 * An exhaustive search mechanism which can be used to test and benchmark other
 * more sophisticated search mechanisms
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data being searched
 */
public class ExhaustiveSearchTree<T> extends
		BinaryExclusionFactory<T, Null, Null> {

	private class NoExclusion extends BinaryExclusion<T, Null, Null> {

		List<T> data;

		private NoExclusion(List<T> data) {
			this.data = data;
		}

		@Override
		public List<T> leftData() {
			if (this.data.size() > 1) {
				return this.data.subList(1, ((this.data.size() / 2) + 1));
			} else {
				return new ArrayList<>();
			}
		}

		@Override
		public List<T> rightData() {
			if (this.data.size() > 1) {
				return this.data.subList((this.data.size() / 2) + 1,
						this.data.size());
			} else {
				return new ArrayList<>();
			}
		}

		@Override
		public ExclusionTest getQueryInfo(T query, double threshold,
				Null queryContext) {
			// in this case there is only a single exclusion datum which must be
			// present
			ExclusionTest res = new ExclusionTest(null) {

				@Override
				public Null getLeftQueryContext() {
					return null;
				}

				@Override
				public Null getRightQueryContext() {
					return null;
				}
			};
			if (ExhaustiveSearchTree.this.metric.distance(this.data.get(0),
					query) < threshold) {
				res.addResult(this.data.get(0));
			}
			return res;
		}

		@Override
		public int storedDataSize() {
			return 1;
		}

		@Override
		public Null getLeftCreationContext() {
			return null;
		}

		@Override
		public Null getRightCreationContext() {
			return null;
		}

	}

	/**
	 * @param metric
	 */
	public ExhaustiveSearchTree(Metric<T> metric) {
		super(metric);
		System.out.println("this ex tree");
	}

	@Override
	public String getName() {
		return "exhaustive_tree_search";
	}

	@SuppressWarnings("synthetic-access")
	@Override
	public BinaryExclusion<T, Null, Null> getExclusion(List<T> data,
			Null context) {
		return new NoExclusion(data);
	}

}
