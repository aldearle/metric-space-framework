package is_paper_experiments.dynamic_binary_partitions;

import is_paper_experiments.binary_partitions.BinaryPartition;
import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import coreConcepts.Metric;

/**
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data to be searched
 */
public class MonotoneHyperplaneTree<T> extends
		BinaryExclusionFactory<T, T, Double> {

	private BinaryPartitionFactory<T> partitionStrategy;
	private boolean fourPointExclusion;
	static Random rand = new Random();

	private class MGHExclusion extends BinaryExclusion<T, T, Double> {

		private boolean isHeadNode;
		private boolean isLeafNode;
		private List<T> leftList;
		private List<T> rightList;
		T leftPivot;
		T rightPivot;
		double lrPivotDist;
		double crLeft, crRight, irLeft, irRight;

		@SuppressWarnings("synthetic-access")
		MGHExclusion(List<T> initialData, T pivotFromAbove) {
			assert initialData != null : "data is null";
			assert initialData.size() > 0 : "data size is zero";

			this.isHeadNode = (pivotFromAbove == null) ? true : false;

			this.leftList = new ArrayList<>();
			this.rightList = new ArrayList<>();

			if (initialData.size() == 1) {
				// just enter the data and finish
				this.isLeafNode = true;
				this.rightPivot = initialData.get(0);
			} else {

				this.isLeafNode = false;
				if (this.isHeadNode) {
					BinaryPartition<T> bp = MonotoneHyperplaneTree.this.partitionStrategy
							.getPartition(initialData);
					assert bp.getReferencePoints().size() == 2 : "wrong number of ref points";
					this.leftPivot = bp.getReferencePoints().get(0);
					this.rightPivot = bp.getReferencePoints().get(1);
				} else {
					this.leftPivot = pivotFromAbove;
					BinaryPartition<T> bp = MonotoneHyperplaneTree.this.partitionStrategy
							.getPartition(initialData, this.leftPivot);
					assert bp.getReferencePoints().size() == 1 : "wrong number of ref points";
					this.rightPivot = bp.getReferencePoints().get(0);
				}

				this.lrPivotDist = MonotoneHyperplaneTree.this.metric.distance(
						this.leftPivot, this.rightPivot);
				this.irLeft = Double.MAX_VALUE;
				this.irRight = Double.MAX_VALUE;

				for (T item : initialData) {
					double d1 = MonotoneHyperplaneTree.this.metric.distance(
							item, this.leftPivot);

					double d2 = MonotoneHyperplaneTree.this.metric.distance(
							item, this.rightPivot);
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

		@SuppressWarnings({ "synthetic-access", "boxing" })
		@Override
		public ExclusionTest getQueryInfo(T query, double threshold,
				Double pivotDistance) {

			final double[] pivDists = new double[2];
			ExclusionTest res = new ExclusionTest(query) {
				@SuppressWarnings("boxing")
				@Override
				public Double getLeftQueryContext() {
					// return distance to left pivot
					return pivDists[0];
				}

				@SuppressWarnings("boxing")
				@Override
				public Double getRightQueryContext() {
					// return distance to right pivot
					return pivDists[1];
				}
			};

			double d1 = 0;
			if (this.isHeadNode) {
				d1 = MonotoneHyperplaneTree.this.metric.distance(query,
						this.leftPivot);
				if (d1 < threshold) {
					res.addResult(this.leftPivot);
				}
			} else {
				d1 = pivotDistance;
			}

			pivDists[0] = d1;
			
			if (this.rightPivot != null) {
				double d2 = MonotoneHyperplaneTree.this.metric.distance(query,
						this.rightPivot);
				pivDists[1] = d2;
				if (d2 < threshold) {
					res.addResult(this.rightPivot);
				}

				assert !Double.isNaN(d1) && !Double.isNaN(d2) : "NaN here";

				if (MonotoneHyperplaneTree.this.fourPointExclusion) {
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
			return this.isHeadNode ? 2 : 1;
		}

		@Override
		public T getLeftCreationContext() {
			return this.leftPivot;
		}

		@Override
		public T getRightCreationContext() {
			return this.rightPivot;
		}

	}

	/**
	 * Creates a new Hyperplane tree binary exclusion strategy
	 * 
	 * @param metric
	 *            the metric to be used
	 */
	public MonotoneHyperplaneTree(Metric<T> metric) {
		super(metric);
		this.setPartitionStrategy(new SimpleWidePartition<>(metric));
	}

	@Override
	public BinaryExclusion<T, T, Double> getExclusion(List<T> data, T context) {
		return new MGHExclusion(data, context);
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
		return "mht";
	}

}
