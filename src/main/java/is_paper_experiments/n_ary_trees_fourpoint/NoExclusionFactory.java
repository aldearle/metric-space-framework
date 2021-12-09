package is_paper_experiments.n_ary_trees_fourpoint;

import is_paper_experiments.n_ary_trees_fourpoint.NaryExclusionFactory.Null;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

/**
 * an example Nary Exclusion mechanism which results in a balanced binary tree
 * with no exclusions being performed
 * 
 * @author Richard Connor
 *
 * @param <T>
 */
public class NoExclusionFactory<T> extends NaryExclusionFactory<T, Null, Null> {

	private class NoExclusion extends NaryExclusion<T, Null, Null> {

		private int arity;
		private List<List<T>> dataSets;
		private List<T> nodeData;
		private List<Null> creationSubContexts;
		private boolean isData;

		public NoExclusion(List<T> data, Null context) {
			super(data, context);
			assert data != null : "data must not be null";

			this.arity = getArity();
			assert this.arity >= 2 : "artity too small";
			this.isData = data.size() <= 1;
			setReferencePoints(data);
			if (!this.isData) {
				this.dataSets = new ArrayList<>();
				this.creationSubContexts = new ArrayList<>();
				for (int i = 0; i < this.arity; i++) {
					this.dataSets.add(new ArrayList<T>());
					this.creationSubContexts.add(null);
				}
				int rotator = 0;
				for (T d : data) {
					this.dataSets.get(rotator).add(d);
					rotator = (rotator + 1) % this.arity;
				}
			}
		}

		private void setReferencePoints(List<T> data) {
			this.nodeData = new ArrayList<>();
			if (data.size() != 0) {
				this.nodeData.add(data.remove(NoExclusionFactory.this.rand
						.nextInt(data.size())));
			}
		}

		@Override
		public QueryResult getQueryInfo(T query, double threshold,
				Null queryContext) {
			QueryResult res = new QueryResult(this.arity);

			for (T d : this.nodeData) {
				double dist = NoExclusionFactory.this.metric.distance(query, d);
				if (dist <= threshold) {
					res.addResult(d);
				}
			}

			return res;
		}

		@Override
		public List<List<T>> getDataPartitions() {
			return this.dataSets;
		}

		@Override
		protected boolean isDataNode() {
			return this.isData;
		}

		@Override
		protected List<Null> getCreationContexts() {
			return this.creationSubContexts;
		}

		@Override
		public int storedDataSize() {
			return this.nodeData.size();
		}

		@Override
		protected int getArity() {
			return 2;
		}

	}

	/**
	 * an ExclusionFactory which excludes nothing, can measure tree structure
	 * overhead
	 * 
	 * @param metric
	 *            the metric used
	 */
	public NoExclusionFactory(Metric<T> metric) {
		super(metric);
	}

	@Override
	public NaryExclusion<T, Null, Null> getExclusion(List<T> data, Null context) {
		return new NoExclusion(data, null);
	}

	@Override
	public String getName() {
		return "exhaustive";
	}

}
