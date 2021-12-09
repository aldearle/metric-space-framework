package query;

import java.util.List;

import coreConcepts.Metric;

/**
 * @author newrichard
 * 
 *         These classes have a creator which takes a list of data; this is only
 *         ever accessed, not updated
 * 
 *         internally an array of integers is manipulated according to the list
 *         positions and returned as a result of queries, etc
 * 
 * @param <T>
 */
public abstract class FixedDataIndex<T> {

	protected Metric<T> metric; // the metric for the index
	protected List<T> data; // the data; not ever updated
	protected int[] ids; // updateable array used to model the index values;
							// sorted in tandem with the distances when sorting
							// occurs
	protected double[] dists; // structure used to model distances from a
								// particular point to some or all of the other
								// points; can typically be nulled out once
								// construction is finished (so maybe shouldn't
								// be here at all!)

	protected int noOfDistances; // temp variable used to measure query
									// performance

	FixedDataIndex(Metric<T> metric, List<T> data) {
		this.metric = metric;
		this.data = data;
		this.ids = new int[data.size()];
		for (int i = 0; i < this.ids.length; i++) {
			this.ids[i] = i;
		}
		this.dists = new double[this.ids.length];
		this.noOfDistances = 0;
	}

	/**
	 * @param query
	 * @param threshold
	 * @return a list of integers corresponding to the indices of the query
	 *         results
	 */
	public abstract List<Integer> thresholdQueryByReference(T query,
			double threshold);

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

	public String toString() {
		StringBuffer res = new StringBuffer();
		for (int i : this.ids) {
			res.append(i);
			res.append(";");
		}
		res.append("\n");
		for (double d : this.dists) {
			res.append(d);
			res.append(";");
		}
		return res.toString();
	}

}
