package is_paper_experiments.dynamic_binary_partitions;

import is_paper_experiments.binary_partitions.BinaryPartition;
import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
public class MonotonePcaHyperplaneTree<T> extends
		BinaryExclusionFactory<T, T, Double> {

	private BinaryPartitionFactory<T> partitionStrategy;
	private boolean fourPointExclusion;
	static Random rand = new Random();
	private Set<T> referenceSet;

	private void createReferenceSet(List<T> data) {
		Random ra = new Random();
		assert data.size() > 1000;
		this.referenceSet = new HashSet<>();
		while (this.referenceSet.size() < 500) {
			T r = data.get(ra.nextInt(data.size()));
			if (!this.referenceSet.contains(r)) {
				this.referenceSet.add(r);
			}
		}
	}

	private class MGHExclusion extends BinaryExclusion<T, T, Double> {

		private boolean isUselessNode;
		private boolean isHeadNode;
		private boolean isLeafNode;
		private List<T> leftList;
		private List<T> rightList;
		private T leftPivot;
		private T rightPivot;
		double crLeft, crRight, irLeft, irRight;
		private SimplexND<T> simp;
		double rotationAngle;
		private double xIntercept;
		double cosTheta;
		double sinTheta;
		double medianX;

		@SuppressWarnings("synthetic-access")
		MGHExclusion(List<T> initialData, T pivotFromAbove) {
			assert initialData != null : "data is null";
			assert initialData.size() > 0 : "data size is zero";

			this.isHeadNode = (pivotFromAbove == null) ? true : false;

			if (isHeadNode) {
				createReferenceSet(initialData);
			}

			this.leftList = new ArrayList<>();
			this.rightList = new ArrayList<>();

			if (initialData.size() == 1) {
				// just enter the data and finish
				this.isLeafNode = true;
				this.rightPivot = initialData.get(0);
			} else {

				setPivots(initialData, pivotFromAbove);

				final double pivDist = metric.distance(this.leftPivot,
						this.rightPivot);
				if (pivDist == 0) {
					this.isUselessNode = true;
					this.leftList = initialData;
				} else {

					Object[] refs = { this.leftPivot, this.rightPivot };
					try {
						this.simp = new SimplexND<>(2, metric, (T[]) refs);
					} catch (Exception e) {
						throw new RuntimeException("couldn't create simplex");
					}

					try {
						final double[] rotationAngleAndIntercept = getRotationAngleAndIntercept(
								this.simp, initialData);
						this.rotationAngle = -rotationAngleAndIntercept[0];
						this.cosTheta = Math.cos(this.rotationAngle);
						this.sinTheta = Math.sin(this.rotationAngle);
						this.xIntercept = rotationAngleAndIntercept[1];
						// this.xIntercept = rand.nextDouble();
						assert Double.isFinite(this.xIntercept);
					} catch (Throwable t) {
						// failed to from simplex
						// rotation angle and intercept as zero, does no harm
					}

					// now, we are just going to store the x offset of the
					// rotated
					// data points!
					@SuppressWarnings("unchecked")
					ObjectWithDistance<T>[] owds = new ObjectWithDistance[initialData
							.size()];

					for (int i : Range.range(0, initialData.size())) {
						T item = initialData.get(i);
						double d1 = MonotonePcaHyperplaneTree.this.metric
								.distance(item, this.leftPivot);
						double d2 = MonotonePcaHyperplaneTree.this.metric
								.distance(item, this.rightPivot);
						if (d1 == 0) {
							owds[i] = new ObjectWithDistance<>(item,
									this.xIntercept);
						} else if (d2 == 0) {
							owds[i] = new ObjectWithDistance<>(item, pivDist
									+ xIntercept);
						} else {
							double[] dists = { d1, d2 };
							double[] apex = this.simp.formSimplex(dists);

							double x = apex[0] + this.xIntercept;
							double y = apex[1];

							double rotatedX = x * cosTheta - y * sinTheta;
							// begin lucia
							// rotatedX = Math.abs(rotatedX);
							// end lucia
							owds[i] = new ObjectWithDistance<>(item, rotatedX);
						}
					}
					Quicksort.placeMedian(owds);
					this.medianX = owds[owds.length / 2].getDistance();

					this.irLeft = Double.MAX_VALUE;
					for (int i : Range.range(0, owds.length / 2)) {
						T item = owds[i].getValue();
						this.leftList.add(item);
						// TODO should store this and look it up
						double d = metric.distance(item, this.leftPivot);
						this.crLeft = Math.max(this.crLeft, d);
						this.irLeft = Math.min(this.irLeft, d);
					}

					this.irRight = Double.MAX_VALUE;
					for (int i : Range.range(owds.length / 2, owds.length)) {
						T item = owds[i].getValue();
						this.rightList.add(item);
						// TODO should store this and look it up
						double d = metric.distance(item, this.rightPivot);
						this.crRight = Math.max(this.crRight, d);
						this.irRight = Math.min(this.irRight, d);
					}
				}
			}

		}

		private void setPivots(List<T> initialData, T pivotFromAbove) {
			this.isLeafNode = false;
			if (this.isHeadNode) {
				BinaryPartition<T> bp = MonotonePcaHyperplaneTree.this.partitionStrategy
						.getPartition(initialData);
				assert bp.getReferencePoints().size() == 2 : "wrong number of ref points";
				this.leftPivot = bp.getReferencePoints().get(0);
				this.rightPivot = bp.getReferencePoints().get(1);
			} else {
				this.leftPivot = pivotFromAbove;
				BinaryPartition<T> bp = MonotonePcaHyperplaneTree.this.partitionStrategy
						.getPartition(initialData, this.leftPivot);
				assert bp.getReferencePoints().size() == 1 : "wrong number of ref points";
				this.rightPivot = bp.getReferencePoints().get(0);
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
				d1 = MonotonePcaHyperplaneTree.this.metric.distance(query,
						this.leftPivot);
				if (d1 < threshold) {
					res.addResult(this.leftPivot);
				}
			} else {
				d1 = pivotDistance;
			}

			pivDists[0] = d1;

			if (this.rightPivot != null) {
				double d2 = MonotonePcaHyperplaneTree.this.metric.distance(
						query, this.rightPivot);
				pivDists[1] = d2;
				if (d2 < threshold) {
					res.addResult(this.rightPivot);
				}

				if (!this.isLeafNode && !this.isUselessNode) {
					assert !Double.isNaN(d1) && !Double.isNaN(d2) : "NaN here";

					if (MonotonePcaHyperplaneTree.this.fourPointExclusion) {

						assert this.simp != null : "no simplex has been created"
								+ this.leftList.size();

						double[] apex = this.simp.formSimplex(pivDists);
						double x = apex[0] + this.xIntercept;
						double y = apex[1];
						double rotatedX = x * this.cosTheta - y * this.sinTheta;

						// Lucia change
						// rotatedX = Math.abs(rotatedX);
						assert !Double.isNaN(rotatedX);
						assert !Double.isNaN(threshold);
						assert Double.isFinite(rotatedX);
						assert Double.isFinite(threshold);
						if (rotatedX < this.medianX - threshold) {
							res.setExcludeRight();
						} else if (rotatedX > this.medianX + threshold) {
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
	public MonotonePcaHyperplaneTree(Metric<T> metric) {
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
		return "pcaHt";
	}

	/**
	 * @param simp
	 * @param refData
	 * @return the angle by which the data points need to be rotated in order to
	 *         maximise the X spread
	 * 
	 *         however this is not all we need, as we need to rotate around the
	 *         X-intercept
	 * 
	 *         so, instead return the angle and the X-intercept; if we subtract
	 *         the X-intercept from all apex X values then we can rotate around
	 *         the origin
	 * 
	 * 
	 */
	private static <T> double[] getRotationAngleAndIntercept(SimplexND<T> simp,
			List<T> refData) {
		List<double[]> apexes = new ArrayList<>();

		for (T point : refData.subList(0, Math.min(500, refData.size()))) {
			final double[] apex = simp.formSimplex(point);
			apexes.add(apex);
		}

		double[] mean = getMeans(apexes);
		assert !Double.isNaN(mean[0]);
		assert !Double.isNaN(mean[1]);
		double topAcc = 0;
		double bottomAcc = 0;
		for (double[] apex : apexes) {
			double xDiff = apex[0] - mean[0];
			assert !Double.isNaN(xDiff);
			double yDiff = apex[1] - mean[1];
			assert !Double.isNaN(yDiff);
			topAcc += xDiff * yDiff;
			bottomAcc += xDiff * xDiff;
		}

		double[] res = new double[2];
		double gradient = topAcc / bottomAcc;
		assert Double.isFinite(gradient) : "bottomAcc = " + bottomAcc;
		res[0] = Math.atan(gradient);
		if (gradient != 0) {
			double xIntercept = (mean[1] / gradient) - mean[0];
			// double xIntercept = mean[0] - (mean[1] / gradient);
			// System.out.println(xIntercept + "; " + gradient);
			res[1] = xIntercept;
		}
		return res;
	}

	private static double[] getMeans(List<double[]> apexes) {
		assert apexes.size() > 0;
		final int dim = apexes.get(0).length;
		double[] res = new double[dim];
		for (double[] ds : apexes) {
			for (int i : Range.range(0, dim)) {
				res[i] += ds[i];
			}
		}
		for (int i : Range.range(0, dim)) {
			res[i] = res[i] / apexes.size();
		}
		// System.out.println("mean x is " + res[0] + "; mean y is " + res[1]);
		return res;
	}
}
