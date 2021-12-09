package searchStructures;

import java.util.ArrayList;
import java.util.List;

import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import util.OrderedListAlt;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

/**
 * @author newrichard
 *
 *         starting to adapt VPTree after convo with Edgar at SISAP2016
 * @param <T>
 */
public class VPTreeLeanest<T> extends SearchIndex<T> {

	private class VPTreeNode {
		// this pivot value will soon become redundant...
		// private int pivot;
		private int datumIndex;
		private double pivotDist;
		VPTreeNode left;
		VPTreeNode right;

		/**
		 * a wickedly lean constructor
		 * 
		 * @param start
		 * @param end
		 */
		@SuppressWarnings("synthetic-access")
		VPTreeNode(int start, int end, int depth) {
			maxDepth = Math.max(maxDepth, depth);
			// this.pivot = VPTreeLeanest.this.ids[start];
			// this.pivot = depth;
			// VPTreeLeanest.this.dists[start] = -1;

			// if (end > start) {
			/*
			 * in the original, this means that there is no recursive build and
			 * the pivot acts as the leaf data node, so we need to do the same
			 * only it's no longer the pivot
			 */
			if (end == start) {
				this.datumIndex = start;
			} else {
				this.datumIndex = -1;
				T piv = VPTreeLeanest.this.pivots.get(depth);

				for (int i = start; i <= end; i++) {
					VPTreeLeanest.this.dists[i] = VPTreeLeanest.this.metric
							.distance(piv, VPTreeLeanest.this.data
									.get(VPTreeLeanest.this.ids[i]));
				}

				final int medianPos = start + ((end - start) + 1) / 2;
				// final int medianPos = start + ((end - start) / 2);
				// quickFindMedian(start + 1, end, medianPos);
				quickFindMedian(start, end, medianPos);

				this.pivotDist = VPTreeLeanest.this.dists[medianPos];
				// if (depth == 1) {
				System.out.println(this.pivotDist);
				for (int i = medianPos; i <= end; i++) {
					if (dists[i] < this.pivotDist) {
						System.out.println(depth + " bad thingy");
					}
				}
				// }

				// if (start + 1 <= medianPos) {
				// if (start < medianPos) {
				/*
				 * this is always true...!
				 */
				// this.left = new VPTreeNode(start + 1, medianPos, depth +
				// 1);
				this.left = new VPTreeNode(start, medianPos - 1, depth + 1);
				// }
				// if (end >= medianPos) {
				// also, always true!
				this.right = new VPTreeNode(medianPos, end, depth + 1);
				// }
			}
		}

		//
		// @SuppressWarnings("synthetic-access")
		// public void nnquery() {
		//
		// final T pivotValue = VPTreeLeanest.this.data.get(this.pivot);
		// double qTOpDistance = VPTreeLeanest.this.metric.distance(
		// VPTreeLeanest.this.nnQuery, pivotValue);
		//
		// if (qTOpDistance < VPTreeLeanest.this.nnThreshold) {
		// nnCurrentIndex = this.pivot;
		// VPTreeLeanest.this.nnThreshold = qTOpDistance;
		// }
		//
		// if (qTOpDistance <= this.pivotDist - VPTreeLeanest.this.nnThreshold)
		// {
		// if (this.left != null) {
		// this.left.nnquery();
		// }
		// } else if (qTOpDistance > this.pivotDist
		// + VPTreeLeanest.this.nnThreshold) {
		// if (this.right != null) {
		// this.right.nnquery();
		// }
		// } else {
		// if (this.left != null) {
		// this.left.nnquery();
		// }
		// if (this.right != null) {
		// // && queryToPivotDistance1 > this.pivotDist
		// // + BalancedVPTree.this.nnThreshold) {
		// this.right.nnquery();
		// }
		// }
		// }

		// @SuppressWarnings("synthetic-access")
		// public void nnquery(OrderedListAlt<Integer, Double> ol) {
		//
		// final T pivotValue = VPTreeLeanest.this.data.get(this.pivot);
		// double qTOpDistance = VPTreeLeanest.this.metric.distance(
		// VPTreeLeanest.this.nnQuery, pivotValue);
		//
		// if (qTOpDistance < VPTreeLeanest.this.nnThreshold) {
		// ol.add(this.pivot, qTOpDistance);
		// Double t = ol.getThreshold();
		// if (t != null) {
		// VPTreeLeanest.this.nnThreshold = t;
		// }
		//
		// // System.out.println("changing current nn to point " +
		// // this.pivot
		// // + " at distance " + qTOpDistance);
		// }
		//
		// if (qTOpDistance <= this.pivotDist - VPTreeLeanest.this.nnThreshold)
		// {
		// if (this.left != null) {
		// this.left.nnquery(ol);
		// }
		// } else if (qTOpDistance > this.pivotDist
		// + VPTreeLeanest.this.nnThreshold) {
		// if (this.right != null) {
		// this.right.nnquery(ol);
		// }
		// } else {
		// if (this.left != null) {
		// this.left.nnquery(ol);
		// }
		// if (this.right != null) {
		// // && queryToPivotDistance1 > this.pivotDist
		// // + BalancedVPTree.this.nnThreshold) {
		// this.right.nnquery(ol);
		// }
		// }
		// }
		//
		// public void queryRef(T query, double threshold, List<Integer>
		// results) {
		//
		// final T pivotValue = VPTreeLeanest.this.data.get(this.pivot);
		//
		// double queryToPivotDistance1 = VPTreeLeanest.this.metric.distance(
		// query, pivotValue);
		//
		// if (queryToPivotDistance1 < threshold) {
		// results.add(this.pivot);
		// }
		//
		// if (queryToPivotDistance1 <= this.pivotDist - threshold) {
		// if (this.left != null) {
		// this.left.queryRef(query, threshold, results);
		// }
		// } else if (queryToPivotDistance1 > this.pivotDist + threshold) {
		// if (this.right != null) {
		// this.right.queryRef(query, threshold, results);
		// }
		// } else {
		// if (this.left != null) {
		// this.left.queryRef(query, threshold, results);
		// }
		// if (this.right != null) {
		// this.right.queryRef(query, threshold, results);
		// }
		// }
		// }

		private void query(T query, double threshold, List<T> results, int depth) {

			if (this.datumIndex != -1) {
				final T dat = VPTreeLeanest.this.data.get(this.datumIndex);
				double queryToDataDistance = VPTreeLeanest.this.metric
						.distance(query, dat);
				if (queryToDataDistance <= threshold) {
					results.add(dat);
				}
			} else {

				final T pivotValue = pivots.get(depth);

				double qToPdist = VPTreeLeanest.this.metric.distance(query,
						pivotValue);

				// if (qToPdist <= this.pivotDist - threshold) {
				// // if (this.left != null) {
				// this.left.query(query, threshold, results, depth + 1);
				// // }
				// } else if (qToPdist > this.pivotDist + threshold) {
				// // if (this.right != null) {
				// this.right.query(query, threshold, results, depth + 1);
				// // }
				// } else {
				this.left.query(query, threshold, results, depth + 1);
				this.right.query(query, threshold, results, depth + 1);
				// }
			}
			// }

		}

		private int cardinality() {
			if (this.datumIndex != -1) {
				return 1;
			} else {
				int res = 0;
				if (this.left != null) {
					res += this.left.cardinality();
				}
				if (this.right != null) {
					res += this.right.cardinality();
				}
				return res;
			}

		}
	}

	int maxDepth = 0;

	private VPTreeNode index;
	// private double nnThreshold;
	// private int nnCurrentIndex;
	//
	// private T nnQuery;

	int noOfPivots;
	private List<T> pivots;
	// double[] queryPivotDists;

	private int[] ids;
	private double[] dists;

	public VPTreeLeanest(List<T> dat, Metric<T> metric) {
		super(dat, metric);

		this.noOfPivots = (int) Math.floor(Math.log(data.size()) / Math.log(2)) + 2;
		System.out.println(data.size() + ";" + this.noOfPivots);
		this.pivots = new ArrayList<>();
		for (int i = 0; i < noOfPivots; i++) {
			this.pivots.add(this.data.remove(0));
		}
		System.out.println(data.size() + ";" + this.noOfPivots);
		this.ids = new int[data.size()];
		for (int i = 0; i < this.ids.length; i++) {
			this.ids[i] = i;
		}
		this.dists = new double[this.ids.length];
		/*
		 * recursively, constructs the entire index
		 */

		this.index = new VPTreeNode(0, data.size() - 1, 0);
		System.out.println("max depth: " + maxDepth);
		System.out.println("cardinality: " + this.index.cardinality());
	}

	//
	// public int nearestNeighbour(T query) {
	// this.nnThreshold = Double.MAX_VALUE;
	// this.nnCurrentIndex = -1;
	// this.nnQuery = query;
	// this.index.nnquery();
	// return this.nnCurrentIndex;
	// }
	//
	// public List<Integer> nearestNeighbour(T query, int numberOfResults) {
	// this.nnThreshold = Double.MAX_VALUE;
	// OrderedListAlt<Integer, Double> ol = new OrderedListAlt<Integer, Double>(
	// numberOfResults);
	// this.nnQuery = query;
	// this.index.nnquery(ol);
	// return ol.getList();
	// }

	//
	// public List<Integer> thresholdQueryByReference(T query, double threshold)
	// {
	// List<Integer> res = new ArrayList<Integer>();
	// this.index.queryRef(query, threshold, res);
	// return res;
	// }

	@SuppressWarnings("synthetic-access")
	@Override
	public List<T> thresholdSearch(T query, double threshold) {
		List<T> res = new ArrayList<>();
		// this.queryPivotDists = new double[this.noOfPivots];
		// int i = 0;
		for (T pivot : this.pivots) {
			final double qDist = this.metric.distance(pivot, query);
			// this.queryPivotDists[i++] = qDist;
			if (qDist <= threshold) {
				res.add(pivot);
			}
		}
		this.index.query(query, threshold, res, 0);
		return res;
	}

	private void swap(int x, int y) {
		if (x != y) {
			double tempD = this.dists[x];
			int tempI = this.ids[x];
			this.dists[x] = this.dists[y];
			this.ids[x] = this.ids[y];
			this.dists[y] = tempD;
			this.ids[y] = tempI;
		}
	}

	/**
	 * sorts just enough of the ids and dists vectors so that the entry at
	 * medianPos is in the correct sorted place; partitions not relevant to that
	 * are not sorted
	 * 
	 * this is of course more general than the median but that's the only use so
	 * far
	 * 
	 * @param from
	 * @param to
	 * @param medianPos
	 */
	protected void quickFindMedian(int from, int to, int medianPos) {

		double pivot = this.dists[to];

		int upTo = from;
		int pivotPos = to;
		/*
		 * now, run into the middle of the vector from both ends, until upTo and
		 * pivotPos meet
		 */
		while (pivotPos != upTo) {
			if (this.dists[upTo] > pivot) {
				swap(upTo, pivotPos - 1);
				swap(pivotPos - 1, pivotPos--);
			} else {
				upTo++;
			}
		}

		/*
		 * this code only places the median value correctly
		 */
		if (pivotPos > medianPos && pivotPos > from) {
			quickFindMedian(from, pivotPos - 1, medianPos);
		} else if (pivotPos < medianPos && pivotPos < to) {
			quickFindMedian(pivotPos + 1, to, medianPos);
		}
	}

	/**
	 * sorts the ids and dists vectors into the order of dists
	 * 
	 * @param from
	 * @param to
	 * @param medianPos
	 */
	protected void quickSort(int from, int to) {

		double pivot = this.dists[to];

		int upTo = from;
		int pivotPos = to;
		/*
		 * now, run into the middle of the vector from both ends, until upTo and
		 * pivotPos meet
		 */
		while (pivotPos != upTo) {
			if (this.dists[upTo] > pivot) {
				swap(upTo, pivotPos - 1);
				swap(pivotPos - 1, pivotPos--);

			} else {
				upTo++;
			}
		}

		/*
		 * here is the quicksort code if we want more than the median value
		 */
		if (pivotPos > from) {
			quickSort(from, pivotPos - 1);
		}
		if (pivotPos < to) {
			quickSort(pivotPos + 1, to);
		}
	}

	@Override
	public String getShortName() {
		return "vptL";
	}

	public static void main(String[] a) throws Exception {
		final SisapFile file = TestLoad.SisapFile.nasa;
		TestLoad tl = new TestLoad(file);
		double threshold = TestLoad.getSisapThresholds(file)[0];
		System.out.println(threshold);
		List<CartesianPoint> qs = tl.getQueries(tl.dataSize() / 10);
		Metric<CartesianPoint> euc = new Euclidean<>();
		CountedMetric cm = new CountedMetric(euc);
		VPTreeLeanest<CartesianPoint> vpt = new VPTreeLeanest<>(
				tl.getDataCopy(), cm);
		cm.reset();
		int sols = 0;
		for (CartesianPoint p : qs) {
			sols += vpt.thresholdSearch(p, threshold).size();
		}
		System.out.println(sols + "; " + cm.reset() / qs.size());
	}

}
