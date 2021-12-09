package is_paper_experiments.n_ary_trees_fourpoint;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

/**
 * 
 * Spatial Approximation Tree exclusion mechanism
 * 
 * @author Richard Connor
 *
 * @param <T>
 */
public class PartitionExclusionFactory<T> extends
		NaryExclusionFactory<T, T, Double> {

	private boolean fourPoint;

	private class ExclusionNode extends NaryExclusion<T, T, Double> {

		private List<T> initialData;
		boolean isDataNode;
		private int arity;
		private List<T> pivots;
		private List<List<T>> partitions;
		private double[] coverRadii;
		private double[] innerRadii;
		double[][] pivotDists;

		private ExclusionNode(List<T> data, T context) {
			super(data, context);
			this.initialData = data;
			initialise(data);
		}

		@Override
		public List<List<T>> getDataPartitions() {
			return this.partitions;
		}

		@Override
		public NaryExclusion<T, T, Double>.QueryResult getQueryInfo(T query,
				double threshold, Double queryContext) {
			QueryResult res = new QueryResult(this.arity);
			double[] qDists = new double[this.pivots.size()];
			int ptr = 0;
			for (T datum : this.pivots) {
				final double qDist = PartitionExclusionFactory.this.metric
						.distance(datum, query);
				qDists[ptr] = qDist;
				if (qDist <= threshold) {
					res.addResult(datum);
				}

				if (!this.isDataNode) {
					final boolean beyondCR = qDist > this.coverRadii[ptr]
							+ threshold;
					final boolean insideIR = qDist < this.innerRadii[ptr]
							- threshold;
					if (beyondCR || insideIR) {
						res.setExclusion(ptr);
					}
				}

				ptr++;
			}

			if (!this.isDataNode) {
				// now check for partition invariant
				for (int i = 0; i < qDists.length - 1; i++) {
					for (int j = i + 1; j < qDists.length; j++) {
						if (PartitionExclusionFactory.this.fourPoint) {
							if ((qDists[i] * qDists[i] - qDists[j] * qDists[j])
									/ this.pivotDists[i][j] > 2 * threshold) {
								res.setExclusion(i);
							}
							if ((qDists[j] * qDists[j] - qDists[i] * qDists[i])
									/ this.pivotDists[i][j] > 2 * threshold) {
								res.setExclusion(j);
							}
						} else {
							if (qDists[i] - qDists[j] > 2 * threshold) {
								// so query is much closer to j than to i, so it
								// can't
								// be in i
								res.setExclusion(i);
							}
							if (qDists[j] - qDists[i] > 2 * threshold) {
								// so query is much closer to j than to i, so it
								// can't
								// be in i
								res.setExclusion(j);
							}
						}
					}
				}
			}
			return res;
		}

		@Override
		public int storedDataSize() {
			return this.pivots.size();
		}

		@SuppressWarnings("boxing")
		private void initialise(List<T> data) {
			this.arity = getArity();
			assert arity > 1 : "arity not correctly set: " + arity;
			this.isDataNode = this.arity >= data.size();
			this.pivots = new ArrayList<>();

			if (!this.isDataNode) {
				this.partitions = new ArrayList<>();
				this.coverRadii = new double[this.arity];
				this.innerRadii = new double[this.arity];
				for (int i = 0; i < this.arity; i++) {
					T pivot = data.remove(rand.nextInt(data.size()));
					this.pivots.add(pivot);
					this.partitions.add(new ArrayList<T>());
					this.coverRadii[i] = 0;
					this.innerRadii[i] = Double.MAX_VALUE;
				}

				for (T datum : data) {
					double minDist = Double.MAX_VALUE;
					int closest = -1;
					int ptr = 0;
					for (T pivot : this.pivots) {
						double dist = PartitionExclusionFactory.this.metric
								.distance(pivot, datum);
						if (dist < minDist) {
							minDist = dist;
							closest = ptr;
						}
						ptr++;
					}
					assert closest != -1 : "closest pivot has not been set";
					this.partitions.get(closest).add(datum);
					this.coverRadii[closest] = Math.max(
							this.coverRadii[closest], minDist);
					this.innerRadii[closest] = Math.min(
							this.innerRadii[closest], minDist);
				}
				// now set up pivot distances for 4-point property
				this.pivotDists = new double[pivots.size()][pivots.size()];
				for (int i = 0; i < pivots.size() - 1; i++) {
					for (int j = i + 1; j < pivots.size(); j++) {
						double dist = metric.distance(pivots.get(i),
								pivots.get(j));
						pivotDists[i][j] = dist;
						pivotDists[j][i] = dist;
					}
				}
			} else {
				this.pivots = data;
			}

		}

		@Override
		protected List<T> getCreationContexts() {
			return this.pivots;
		}

		@Override
		protected boolean isDataNode() {
			return this.isDataNode;
		}

		@Override
		protected int getArity() {
			int logSize = Math.max(2,
					(int) Math.round(Math.log(this.initialData.size()) * 2));
			return logSize;
		}

	}

	@Deprecated
	PartitionExclusionFactory(Metric<T> metric, boolean fourPoint) {
		super(metric);
		this.fourPoint = fourPoint;
	}

	@Override
	public NaryExclusion<T, T, Double> getExclusion(List<T> data, T context) {
		return new ExclusionNode(data, context);
	}

	@Override
	public String getName() {
		return "generic partition";
	}
}
