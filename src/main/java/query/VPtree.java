package query;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;
import coreConcepts.MetricSpace;

/**
 * A recently imported class, not yet rationalised for this framework or
 * commented, probably safest to not use!
 * 
 * @author Richard Connor
 * 
 * @param <T>
 */
public class VPtree<T> implements MetricIndex<T> {

	private class VPTreeRep {
		private VPTreeRep left;
		private VPTreeRep right;
		private T value;
		private double leftCoveringRadius;
		private double rightCoveringRadius;

		VPTreeRep(T val) {
			this.left = null;
			this.right = null;
			this.value = val;
			this.leftCoveringRadius = 0;
			VPtree.this.totalNodes++;
		}

		@SuppressWarnings("synthetic-access")
		private void insert(T newValue) {
			final double queryToPivotDistance = VPtree.this.theMetric.distance(
					newValue, this.value);
			if (queryToPivotDistance <= VPtree.this.pivotDistance) {
				if (this.left == null) {
					this.left = new VPTreeRep(newValue);
				} else {
					this.left.insert(newValue);
				}
				this.leftCoveringRadius = Math.max(this.leftCoveringRadius,
						queryToPivotDistance);
			} else {
				if (this.right == null) {
					this.right = new VPTreeRep(newValue);
				} else {
					this.right.insert(newValue);
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		private void query(T query, double threshold, List<T> results) {
			double queryToPivotDistance1 = localDistance(query, this.value);

			if (queryToPivotDistance1 < threshold) {
				results.add(this.value);
			}

			if (queryToPivotDistance1 <= VPtree.this.pivotDistance - threshold) {

				if (this.left != null) {
					this.left.query(query, threshold, results);
				}
			} else if (queryToPivotDistance1 > VPtree.this.pivotDistance
					+ threshold) {
				if (this.right != null) {
					this.right.query(query, threshold, results);
				}
			} else {
				if (this.left != null) {
					/*
					 * this is the main pivot condition in general...
					 */
					if (queryToPivotDistance1 <= this.leftCoveringRadius
							+ threshold) {
						this.left.query(query, threshold, results);
					}
				}
				if (this.right != null) {
					this.right.query(query, threshold, results);
				}
			}

		}

		private void slidingThresholdQuery(T query, List<T> results) {
			double pivotDistance = localDistance(query, this.value);

			if (pivotDistance < VPtree.this.slidingThreshold) {
				results.add(this.value);
				VPtree.this.slidingThreshold = pivotDistance;
			}

			if (pivotDistance <= VPtree.this.pivotDistance
					- VPtree.this.slidingThreshold) {
				if (this.left != null) {
					this.left.slidingThresholdQuery(query, results);
				}
			} else if (pivotDistance >= VPtree.this.pivotDistance
					+ VPtree.this.slidingThreshold) {
				if (this.right != null) {
					this.right.slidingThresholdQuery(query, results);
				}
			} else {
				if (this.left != null) {
					this.left.slidingThresholdQuery(query, results);
				}
				if (this.right != null) {
					this.right.slidingThresholdQuery(query, results);
				}
			}

		}
	}

	private final double pivotDistance;

	private int noOfDistanceCalcs = 0;
	private double slidingThreshold;
	private Metric<T> theMetric;
	private VPTreeRep treeRoot;
	int totalNodes = 0;

	/**
	 * an odd thing to do, but if the tree has been built with either JSD or
	 * SED, then the other would also have created the same tree
	 * 
	 * @param newMetric
	 *            the new metric
	 */
	public void resetMetric(Metric<T> newMetric) {
		this.theMetric = newMetric;
	}

	public VPtree(Metric<T> metric, double pivotDistance) {
		this.theMetric = metric;
		this.pivotDistance = pivotDistance;
	}

	public VPtree(MetricSpace<T> space, double pivotDistance) throws Exception {
		if (!space.isFinite()) {
			throw new Exception(
					"can't create this VPtree for a non-finite space");
		} else {
			this.theMetric = space.getMetric();
			this.pivotDistance = pivotDistance;

			for (T x : space) {
				insertItem(x);
			}
		}
	}

	/**
	 * returns the number of distance calcualtions performed either since the
	 * instance was created, or since this method was last called
	 * 
	 * @return the number of calls to the metric distance
	 */
	public int getNoOfDistanceCalcs() {
		final int noOfDistanceCalcs2 = this.noOfDistanceCalcs;
		this.noOfDistanceCalcs = 0;
		return noOfDistanceCalcs2;
	}

	@SuppressWarnings("synthetic-access")
	public void insertItem(T item) {
		if (this.treeRoot == null) {
			this.treeRoot = new VPTreeRep(item);
		} else {
			this.treeRoot.insert(item);
		}
	}

	/**
	 * A nearest neighbour query over the data set
	 * 
	 * @param query
	 *            the query object
	 * @param n
	 *            the number of neighbours to return
	 * @return list of the n closest neighbours
	 */
	@SuppressWarnings("synthetic-access")
	public List<T> NNQuery(T query, int n) {
		List<T> results = new ArrayList<T>();

		this.slidingThreshold = 1.0;
		this.treeRoot.slidingThresholdQuery(query, results);

		return results;
	}

	@SuppressWarnings("synthetic-access")
	@Override
	public List<T> thresholdQuery(T queryObject, double t) {
		List<T> results = new ArrayList<T>();

		if (this.treeRoot != null) {
			this.treeRoot.query(queryObject, t, results);
		}

		return results;
	}

	public List<T> thresholdQueryAndAddToIndex(T value, double threshold) {
		List<T> res = thresholdQuery(value, threshold);
		insertItem(value);

		return res;
	}

	private double localDistance(T x, T y) {
		this.noOfDistanceCalcs++;
		return this.theMetric.distance(x, y);
	}

	public int getTreeSize() {
		return this.totalNodes;
	}

}
