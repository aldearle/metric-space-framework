package is_paper_experiments.dynamic_binary_partitions;

import is_paper_experiments.binary_partitions.BinaryPartition;
import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import n_point_surrogate.SimplexND;
import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import util.Range;
import coreConcepts.Metric;

/**
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data to be searched
 */
public class MonotoneBalancedHyperplaneTree<T> extends
		BinaryExclusionFactory<T, T, Double> {

	private BinaryPartitionFactory<T> partitionStrategy;
	private boolean fourPointExclusion;
	static Random rand = new Random();

	private class MGHExclusion extends BinaryExclusion<T, T, Double> {

		private boolean isHeadNode;
		private boolean isLeafNode;
		private boolean isUselessNode;
		private List<T> leftList;
		private List<T> rightList;
		private T leftPivot;
		private T rightPivot;
		private double lrPivotDist;
		private double crLeft, crRight, irLeft, irRight;
		private SimplexND<T> simp;
		private double medianOffset;

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
					BinaryPartition<T> bp = MonotoneBalancedHyperplaneTree.this.partitionStrategy
							.getPartition(initialData);
					assert bp.getReferencePoints().size() == 2 : "wrong number of ref points";
					this.leftPivot = bp.getReferencePoints().get(0);
					this.rightPivot = bp.getReferencePoints().get(1);
				} else {
					this.leftPivot = pivotFromAbove;
					BinaryPartition<T> bp = MonotoneBalancedHyperplaneTree.this.partitionStrategy
							.getPartition(initialData, this.leftPivot);
					assert bp.getReferencePoints().size() == 1 : "wrong number of ref points";
					this.rightPivot = bp.getReferencePoints().get(0);
				}

				this.lrPivotDist = MonotoneBalancedHyperplaneTree.this.metric
						.distance(this.leftPivot, this.rightPivot);
				if (this.lrPivotDist == 0) {
					this.isUselessNode = true;
					this.leftList = initialData;
				} else {
					Object[] refs = { leftPivot, rightPivot };
					try {
						this.simp = new SimplexND<>(2, metric, (T[]) refs);
					} catch (Exception e) {
						throw new RuntimeException("can't make simplex");
					}
					/*
					 * store the X coordinate of the apex in an array of owds
					 */
					ObjectWithDistance<T>[] owds = new ObjectWithDistance[initialData
							.size()];
					for (int i : Range.range(0, initialData.size())) {
						T item = initialData.get(i);
						double d1 = MonotoneBalancedHyperplaneTree.this.metric
								.distance(item, this.leftPivot);

						double d2 = MonotoneBalancedHyperplaneTree.this.metric
								.distance(item, this.rightPivot);
						double[] dists = { d1, d2 };

						if (d1 == 0) {
							owds[i] = new ObjectWithDistance<>(item, 0);
						} else if (d2 == 0) {
							owds[i] = new ObjectWithDistance<>(item,
									this.lrPivotDist);
						} else {
							double[] ap = simp.formSimplex(dists);
							owds[i] = new ObjectWithDistance<>(
									initialData.get(i), ap[0]);
						}

					}
					Quicksort.placeMedian(owds);
					this.medianOffset = owds[owds.length / 2].getDistance();

					this.irLeft = Double.MAX_VALUE;
					for (int i : Range.range(0, owds.length / 2)) {
						final T item = owds[i].getValue();
						this.leftData().add(item);
						// should store and retrieve this...
						double dist = metric.distance(item, this.leftPivot);
						this.irLeft = Math.min(this.irLeft, dist);
						this.crLeft = Math.max(this.crLeft, dist);
					}
					this.irRight = Double.MAX_VALUE;
					for (int i : Range.range(owds.length / 2, owds.length)) {
						final T item = owds[i].getValue();
						this.rightData().add(item);
						// should store and retrieve this...
						double dist = metric.distance(item, this.rightPivot);
						this.irRight = Math.min(this.irRight, dist);
						this.crRight = Math.max(this.crRight, dist);
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
				d1 = MonotoneBalancedHyperplaneTree.this.metric.distance(query,
						this.leftPivot);
				if (d1 < threshold) {
					res.addResult(this.leftPivot);
				}
			} else {
				d1 = pivotDistance;
			}

			pivDists[0] = d1;

			if (this.rightPivot != null) {
				double d2 = MonotoneBalancedHyperplaneTree.this.metric
						.distance(query, this.rightPivot);
				pivDists[1] = d2;
				if (d2 < threshold) {
					res.addResult(this.rightPivot);
				}
				if (!this.isLeafNode && !this.isUselessNode) {
					assert !Double.isNaN(d1) && !Double.isNaN(d2) : "NaN here";

					if (fourPointExclusion) {

						assert this.simp != null : "no simplex has been created"
								+ this.leftList.size();

						double[] apex = this.simp.formSimplex(pivDists);
						double x = apex[0];

						if (x < this.medianOffset - threshold) {
							res.setExcludeRight();
						} else if (x > this.medianOffset + threshold) {
							res.setExcludeLeft();
						}

					} else {
						throw new RuntimeException(
								"can't use PCA tree without four point property");
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
	public MonotoneBalancedHyperplaneTree(Metric<T> metric) {
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
		return "mhtBal";
	}

}
