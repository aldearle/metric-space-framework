package query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import util.OrderedList;
import util.OrderedListAlt;
import util.Util;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.LebesqueInf;
import dataPoints.cartesian.SEDByComplexity;
import dataSets.fileReaders.CartesianPointFileReader;

public class BalancedVPTree<T> extends FixedDataIndex<T> {

	private VPTreeNode index;

	private double nnThreshold;
	private int nnCurrentIndex;
	private T nnQuery;

	private class VPTreeNode {
		private int pivot;
		private double pivotDist;
		VPTreeNode left;
		VPTreeNode right;

		VPTreeNode(int start, int end) {
			this.pivot = BalancedVPTree.this.ids[start];
			BalancedVPTree.this.dists[start] = -1;

			if (end > start) {
				T piv = BalancedVPTree.this.data
						.get(BalancedVPTree.this.ids[start]);

				for (int i = start + 1; i <= end; i++) {
					BalancedVPTree.this.dists[i] = BalancedVPTree.this.metric
							.distance(piv, BalancedVPTree.this.data
									.get(BalancedVPTree.this.ids[i]));
				}

				final int medianPos = start + ((end - start) + 1) / 2;
				quickFindMedian(start + 1, end, medianPos);

				this.pivotDist = BalancedVPTree.this.dists[medianPos];

				if (start + 1 <= medianPos) {
					this.left = new VPTreeNode(start + 1, medianPos);
				}
				if (end >= medianPos + 1) {
					this.right = new VPTreeNode(medianPos + 1, end);
				}
			}
		}

		private void query(T query, double threshold, List<T> results) {

			final T pivotValue = BalancedVPTree.this.data.get(this.pivot);

			double queryToPivotDistance1 = BalancedVPTree.this.metric.distance(
					query, pivotValue);
			BalancedVPTree.this.noOfDistances++;

			if (queryToPivotDistance1 < threshold) {
				results.add(pivotValue);
			}

			if (queryToPivotDistance1 <= this.pivotDist - threshold) {
				if (this.left != null) {
					this.left.query(query, threshold, results);
				}
			} else if (queryToPivotDistance1 > this.pivotDist + threshold) {
				if (this.right != null) {
					this.right.query(query, threshold, results);
				}
			} else {
				if (this.left != null) {
					this.left.query(query, threshold, results);
				}
				if (this.right != null) {
					this.right.query(query, threshold, results);
				}
			}

		}

		public void queryRef(T query, double threshold, List<Integer> results) {

			final T pivotValue = BalancedVPTree.this.data.get(this.pivot);

			double queryToPivotDistance1 = BalancedVPTree.this.metric.distance(
					query, pivotValue);
			BalancedVPTree.this.noOfDistances++;

			if (queryToPivotDistance1 < threshold) {
				results.add(this.pivot);
			}

			if (queryToPivotDistance1 <= this.pivotDist - threshold) {
				if (this.left != null) {
					this.left.queryRef(query, threshold, results);
				}
			} else if (queryToPivotDistance1 > this.pivotDist + threshold) {
				if (this.right != null) {
					this.right.queryRef(query, threshold, results);
				}
			} else {
				if (this.left != null) {
					this.left.queryRef(query, threshold, results);
				}
				if (this.right != null) {
					this.right.queryRef(query, threshold, results);
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		public void nnquery() {

			final T pivotValue = BalancedVPTree.this.data.get(this.pivot);
			double qTOpDistance = BalancedVPTree.this.metric.distance(
					BalancedVPTree.this.nnQuery, pivotValue);

			BalancedVPTree.this.noOfDistances++;
			if (qTOpDistance < BalancedVPTree.this.nnThreshold) {
				nnCurrentIndex = this.pivot;
				BalancedVPTree.this.nnThreshold = qTOpDistance;
			}

			if (qTOpDistance <= this.pivotDist
					- BalancedVPTree.this.nnThreshold) {
				if (this.left != null) {
					this.left.nnquery();
				}
			} else if (qTOpDistance > this.pivotDist
					+ BalancedVPTree.this.nnThreshold) {
				if (this.right != null) {
					this.right.nnquery();
				}
			} else {
				if (this.left != null) {
					this.left.nnquery();
				}
				if (this.right != null) {
					// && queryToPivotDistance1 > this.pivotDist
					// + BalancedVPTree.this.nnThreshold) {
					this.right.nnquery();
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		public void nnquery(OrderedListAlt<Integer, Double> ol) {

			final T pivotValue = BalancedVPTree.this.data.get(this.pivot);
			double qTOpDistance = BalancedVPTree.this.metric.distance(
					BalancedVPTree.this.nnQuery, pivotValue);

			BalancedVPTree.this.noOfDistances++;
			if (qTOpDistance < BalancedVPTree.this.nnThreshold) {
				ol.add(this.pivot, qTOpDistance);
				Double t = ol.getThreshold();
				if (t != null) {
					BalancedVPTree.this.nnThreshold = t;
				}

				// System.out.println("changing current nn to point " +
				// this.pivot
				// + " at distance " + qTOpDistance);
			}

			if (qTOpDistance <= this.pivotDist
					- BalancedVPTree.this.nnThreshold) {
				if (this.left != null) {
					this.left.nnquery(ol);
				}
			} else if (qTOpDistance > this.pivotDist
					+ BalancedVPTree.this.nnThreshold) {
				if (this.right != null) {
					this.right.nnquery(ol);
				}
			} else {
				if (this.left != null) {
					this.left.nnquery(ol);
				}
				if (this.right != null) {
					// && queryToPivotDistance1 > this.pivotDist
					// + BalancedVPTree.this.nnThreshold) {
					this.right.nnquery(ol);
				}
			}
		}

	}

	public List<T> thresholdQuery(T query, double threshold) {
		List<T> res = new ArrayList<T>();
		this.index.query(query, threshold, res);
		return res;
	}

	public int nearestNeighbour(T query) {
		this.nnThreshold = Double.MAX_VALUE;
		this.nnCurrentIndex = -1;
		this.nnQuery = query;
		this.index.nnquery();
		return this.nnCurrentIndex;
	}

	public List<Integer> nearestNeighbour(T query, int numberOfResults) {
		this.nnThreshold = Double.MAX_VALUE;
		OrderedListAlt<Integer, Double> ol = new OrderedListAlt<Integer, Double>(
				numberOfResults);
		this.nnQuery = query;
		this.index.nnquery(ol);
		return ol.getList();
	}

	@Override
	public List<Integer> thresholdQueryByReference(T query, double threshold) {
		List<Integer> res = new ArrayList<Integer>();
		this.index.queryRef(query, threshold, res);
		return res;
	}

	public BalancedVPTree(Metric<T> metric, List<T> vals) {
		super(metric, vals);
		/*
		 * recursively, constructs the entire index
		 */
		this.index = new VPTreeNode(0, vals.size() - 1);
	}

	public int getLastQueryDists() {
		int res = this.noOfDistances;
		this.noOfDistances = 0;
		return res;
	}

}
