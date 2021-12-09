package is_paper_experiments.dynamic_binary_partitions;

import is_paper_experiments.binary_partitions.BinaryPartition;
import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.RandomPartition;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

/**
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data to be searched
 */
public class HyperplaneTree<T> extends BinaryExclusionFactory<T, T, Double> {

	private BinaryPartitionFactory<T> partitionStrategy;
	private boolean fourPointExclusion;

	private class GHExclusion extends BinaryExclusion<T, T, Double> {

		// private List<T> initialData;
		private List<T> leftList;
		private List<T> rightList;
		T leftPivot;
		T rightPivot;
		double lrPivotDist;
		double crLeft, crRight, irLeft, irRight;

		@SuppressWarnings("synthetic-access")
		GHExclusion(List<T> initialData) {
			assert initialData != null : "data is null";
			assert initialData.size() > 0 : "data size is zero";

			this.leftList = new ArrayList<>();
			this.rightList = new ArrayList<>();

			if (initialData.size() <= 2) {
				this.leftPivot = initialData.get(0);
				if (initialData.size() > 1) {
					this.rightPivot = initialData.get(1);
				}
			} else {

				@SuppressWarnings("unchecked")
				BinaryPartition<T> pivots = HyperplaneTree.this.partitionStrategy
						.getPartition(initialData);

				this.leftPivot = pivots.getReferencePoints().get(0);
				this.rightPivot = pivots.getReferencePoints().get(1);
				List<T> remainingData = pivots.getData();
				this.lrPivotDist = HyperplaneTree.this.metric.distance(
						this.leftPivot, this.rightPivot);
				this.irLeft = Double.MAX_VALUE;
				this.irRight = Double.MAX_VALUE;

				for (T item : remainingData) {
					double d1 = HyperplaneTree.this.metric.distance(item,
							this.leftPivot);

					double d2 = HyperplaneTree.this.metric.distance(item,
							this.rightPivot);
					if (d1 < d2) {
						this.leftList.add(item);
						this.crLeft = Math.max(this.crLeft, d1);
						this.irLeft = Math.min(this.irLeft, d1);
					} else {
						this.rightList.add(item);
						this.crRight = Math.max(this.crRight, d2);
						this.irRight = Math.min(this.irRight, d2);
					}
				}
			}

		}

		@SuppressWarnings("synthetic-access")
		@Override
		public ExclusionTest getQueryInfo(T query, double threshold,
				Double queryContext) {

			ExclusionTest res = new ExclusionTest(query) {

				@Override
				public Double getLeftQueryContext() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Double getRightQueryContext() {
					// TODO Auto-generated method stub
					return null;
				}
			};

			double d1 = HyperplaneTree.this.metric.distance(query,
					this.leftPivot);
			if (d1 < threshold) {
				res.addResult(this.leftPivot);
			}
			if (this.rightPivot != null) {
				double d2 = HyperplaneTree.this.metric.distance(query,
						this.rightPivot);
				if (d2 < threshold) {
					res.addResult(this.rightPivot);
				}

				if (HyperplaneTree.this.fourPointExclusion) {
					if ((d1 * d1 - d2 * d2) / this.lrPivotDist >= 2 * threshold) {
						res.setExcludeLeft();
					}
					if ((d2 * d2 - d1 * d1) / this.lrPivotDist > 2 * threshold) {
						res.setExcludeRight();
					}
				} else {
					if ((d1 - d2) >= 2 * threshold) {
						res.setExcludeLeft();
					}
					if ((d2 - d1) > 2 * threshold) {
						res.setExcludeRight();
					}
				}
				if (d1 + threshold < this.irLeft) {
					res.setExcludeLeft();
				}
				if (d1 - threshold > this.crLeft) {
					res.setExcludeLeft();
				}
				if (d2 + threshold < this.irRight) {
					res.setExcludeRight();
				}
				if (d2 - threshold > this.crRight) {
					res.setExcludeRight();
				}

			}
			return res;
		}

		@Override
		public List<T> leftData() {
			return this.leftList;
		}

		@Override
		public List<T> rightData() {
			return this.rightList;
		}

		@Override
		public int storedDataSize() {
			return this.rightPivot == null ? 1 : 2;
		}

		@Override
		public T getLeftCreationContext() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public T getRightCreationContext() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	/**
	 * Creates a new Hyperplane tree binary exclusion strategy
	 * 
	 * @param metric
	 *            the metric to be used
	 */
	public HyperplaneTree(Metric<T> metric) {
		super(metric);
		this.setPartitionStrategy(new RandomPartition<>(metric));
		System.out.println("this h tree");
	}

	@Override
	public BinaryExclusion<T, T, Double> getExclusion(List<T> data,
			T context) {
		return new GHExclusion(data);
	}

	/**
	 * @param partitionStrategy
	 *            the strategy to be used when creating partitions
	 */
	public void setPartitionStrategy(BinaryPartitionFactory<T> partitionStrategy) {
		this.partitionStrategy = partitionStrategy;
	}

	/**
	 * @param fourPoint
	 *            whether Hilbert Exclusion is to be used or not
	 */
	public void setFourPoint(boolean fourPoint) {
		this.fourPointExclusion = fourPoint;
	}

	@Override
	public String getName() {
		return "ght test";
	}

}
