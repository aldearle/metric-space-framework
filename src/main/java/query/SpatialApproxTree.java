package query;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

public class SpatialApproxTree<T> extends FixedDataIndex<T> {

	private SatNode index;

	private class SatNode {
		private int root;
		private List<Integer> N;
		private double coveringRadius;

		/**
		 * @param start
		 *            reference into the ids vector
		 * @param end
		 *            reference into the ids vector
		 */
		SatNode(int start, int end) {
			this.root = ids[start];
			if (end > start) {
				final T thisVal = data.get(this.root);
				System.out.println("thisVal is " + thisVal);
				/*
				 * populate the relevant part of the distances vector
				 */
				for (int i = start + 1; i < end; i++) {
					dists[i] = metric.distance(thisVal, data.get(ids[i]));
				}

				/*
				 * now sort, as in the original definition
				 */
				quickSort(start + 1, end);

				/*
				 * now find neighbours: all points that are closer to the root
				 * than any existing neighbour
				 */
				List<Integer> neighbs = new ArrayList<Integer>();
				coveringRadius = 0;
				/*
				 * remember i is a reference into the ids vector
				 */
				for (int i = start + 1; i <= end; i++) {
					final T nextContender = data.get(ids[i]);
					System.out.print("data is " + nextContender + "; ");
					final double rootDistance = metric.distance(thisVal,
							nextContender);
					coveringRadius = Math.max(coveringRadius, rootDistance);

					if (neighbs.size() == 0) {
						neighbs.add(ids[i]);
					} else {
						boolean lessThanAll = true;
						int nPtr = 0;
						while (nPtr < neighbs.size() && lessThanAll) {
							if (metric.distance(nextContender,
									data.get(neighbs.get(nPtr++))) < rootDistance) {
								lessThanAll = false;
							}
						}
						if (lessThanAll) {
							neighbs.add(ids[i]);
						}
					}
					System.out.print("R is " + coveringRadius
							+ "; neighbours are: ");
					for (int n : neighbs) {
						System.out.print(data.get(n) + ",");
					}
					System.out.println();
				}

				/*
				 * so, now we have the neighbours, we need to partition the rest
				 * of the set into them
				 * 
				 * always remembering, i here is a reference into the ids vector
				 */
				for (int i = start + 1; i <= end; i++) {
					int nextId = ids[i];
				}

			}
		}
	}

	public static void main(String[] args) {
		Metric<Integer> m = new Metric<Integer>() {

			@Override
			public double distance(Integer x, Integer y) {
				return Math.abs(x - y);
			}

			@Override
			public String getMetricName() {
				return "integerDifference";
			}
		};
		int[] vs = { 7, 5, 17, 4, 3, 2 };
		List<Integer> vals = new ArrayList<Integer>();
		for (int x : vs) {
			vals.add(x);
		}
		SpatialApproxTree<Integer> sat = new SpatialApproxTree<Integer>(m, vals);

		System.out.println("done");
	}

	public SpatialApproxTree(Metric<T> metric, List<T> vals) {
		super(metric, vals);
		this.index = new SatNode(0, vals.size() - 1);
	}

	@Override
	public List<Integer> thresholdQueryByReference(T query, double threshold) {
		// TODO Auto-generated method stub
		return null;
	}
}
