package is_paper_experiments.dynamic_binary_partitions;


import is_paper_experiments.dynamic_binary_partitions.SearchTree.Null;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import coreConcepts.Metric;

/**
 * A simple pivot exclusion mechanism, will produce a balanced Vantage Point
 * Tree; mostly to illustrate use of generic SearchTree class with
 * BinaryExclusionFactory
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of data being searched
 */
public class VantagePointTree<T> extends BinaryExclusionFactory<T, Null, Null> {

	private static Random rand;

	private class PivotExclusion extends BinaryExclusion<T, Null, Null> {

		private T pivot;
		private double medianDist;
		private List<T> leftList;
		private List<T> rightList;

		@SuppressWarnings("synthetic-access")
		private PivotExclusion(List<T> data) {
			this.pivot = data.remove(rand.nextInt(data.size()));
			this.leftList = new ArrayList<>();
			this.rightList = new ArrayList<>();

			if (data.size() > 0) {

				@SuppressWarnings("unchecked")
				ObjectWithDistance<T>[] od = new ObjectWithDistance[data.size()];
				int ptr = 0;
				for (T d : data) {
					od[ptr++] = new ObjectWithDistance<>(d,
							VantagePointTree.this.metric
									.distance(this.pivot, d));
				}
				Quicksort.placeMedian(od);
				this.medianDist = od[od.length / 2].getDistance();

				for (ObjectWithDistance<T> d : od) {
					if (d.getDistance() < this.medianDist) {
						this.leftList.add(d.getValue());
					} else {
						this.rightList.add(d.getValue());
					}
				}
			}
		}

		@Override
		public ExclusionTest getQueryInfo(T query, double threshold,
				Null queryContext) {

			ExclusionTest res = new ExclusionTest(null) {
				@Override
				public Null getLeftQueryContext() {
					return null;
				}

				@Override
				public Null getRightQueryContext() {
					return null;
				}
			};

			double d1 = VantagePointTree.this.metric
					.distance(query, this.pivot);
			if (d1 < threshold) {
				res.addResult(this.pivot);
			}

			if (d1 >= this.medianDist + threshold) {
				res.setExcludeLeft();
			}
			if (d1 < this.medianDist - threshold) {
				res.setExcludeRight();
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
			return 1;
		}

		@Override
		public Null getLeftCreationContext() {
			return null;
		}

		@Override
		public Null getRightCreationContext() {
			return null;
		}

	}

	/**
	 * @param metric
	 */
	public VantagePointTree(Metric<T> metric) {
		super(metric);
		this.rand = new Random();
	}

	@SuppressWarnings("synthetic-access")
	@Override
	public BinaryExclusion<T, Null, Null> getExclusion(List<T> data,
			Null context) {
		return new PivotExclusion(data);
	}

	@Override
	public String getName() {
		return "single_pivot_search";
	}

}
