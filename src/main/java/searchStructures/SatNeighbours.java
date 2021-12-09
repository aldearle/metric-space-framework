package searchStructures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import searchStructures.SATGeneric.Strategy;
import coreConcepts.Metric;

public class SatNeighbours<T> extends ArrayList<T> {

	private Metric<T> m;
	private T head;

	/**
	 * @param head
	 *            the head node
	 * @param data
	 *            the data, this method must delete the neighbours returned from
	 *            this
	 */
	SatNeighbours(T head, List<T> data, Metric<T> m, Strategy consStrategy) {
		super();
		this.m = m;
		this.head = head;

		switch (consStrategy) {
		case richard:
			myDynamicInit(data);
			break;
		case original:
			originalInit(data);
			break;
		case distal:
			reverseInit(data);
			break;
		case random:
			randomInit(data);
			break;
		}
	}

	protected void originalInit(List<T> data) {
		ObjectWithDistance<T>[] sorted = new ObjectWithDistance[data.size()];
		int ptr = 0;
		for (T datum : data) {
			double d = m.distance(datum, head);
			sorted[ptr++] = new ObjectWithDistance<>(datum, d);
		}
		
		Quicksort.sort(sorted);

		for (ObjectWithDistance<T> datum : sorted) {

			double toHead = datum.getDistance();
			boolean canAdd = true;
			for (T neighb : this) {
				if (canAdd) {
					if (this.m.distance(datum.getValue(), neighb) <= toHead) {
						canAdd = false;
					}
				}
			}
			if (canAdd) {
				this.add(datum.getValue());
			}
			for (T n : this) {
				data.remove(n);
			}
		}
	}

	protected void reverseInit(List<T> data) {
		ObjectWithDistance<T>[] toSort = new ObjectWithDistance[data.size()];
		int ptr = 0;
		for (T datum : data) {
			double d = m.distance(datum, head);
			toSort[ptr++] = new ObjectWithDistance<>(datum, d);
		}
		Quicksort.sort(toSort);

		for (int i = toSort.length - 1; i >= 0; i--) {

			ObjectWithDistance<T> datum = toSort[i];
			double toHead = datum.getDistance();
			boolean canAdd = true;
			for (T neighb : this) {
				if (canAdd) {
					if (this.m.distance(datum.getValue(), neighb) <= toHead) {
						canAdd = false;
					}
				}
			}
			if (canAdd) {
				this.add(datum.getValue());
			}
			for (T n : this) {
				data.remove(n);
			}
		}
	}

	protected void myDynamicInit(List<T> data) {
		Set<T> toRemove = new HashSet<>();
		for (T datum : data) {
			double toHead = this.m.distance(this.head, datum);
			boolean canAdd = true;
			for (T neighb : this) {
				if (canAdd && !toRemove.contains(neighb)) {
					if (this.m.distance(datum, neighb) <= toHead) {
						canAdd = false;
					}
				}
			}
			if (canAdd) {/*
						 * may need to purge another... but which?
						 */
				for (T existing : this) {
					if (this.m.distance(datum, existing) < this.m.distance(
							existing, this.head)) {
						toRemove.add(existing);
					}
				}
				this.add(datum);
			}
		}
		/*
		 * remove any nodes that were selected as neighbours and no longer are
		 */
		for (T redundant : toRemove) {
			this.remove(redundant);
		}
		/*
		 * remove the returned neighbours from the original data
		 */
		for (T n : this) {
			data.remove(n);
		}
	}

	protected void randomInit(List<T> data) {
		Set<T> toRemove = new HashSet<>();
		for (T datum : data) {
			double toHead = this.m.distance(this.head, datum);
			boolean canAdd = true;
			for (T neighb : this) {
				if (canAdd && !toRemove.contains(neighb)) {
					if (this.m.distance(datum, neighb) <= toHead) {
						canAdd = false;
					}
				}
			}
			if (canAdd) {/*
						 * may need to purge another... but which?
						 */
				for (T existing : this) {
					if (this.m.distance(datum, existing) < this.m.distance(
							existing, this.head)) {
						toRemove.add(existing);
					}
				}
				this.add(datum);
			}
		}
		/*
		 * remove any nodes that were selected as neighbours and no longer are
		 */
		// for (T redundant : toRemove) {
		// this.remove(redundant);
		// }
		/*
		 * remove the returned neighbours from the original data
		 */
		for (T n : this) {
			data.remove(n);
		}
	}

	private boolean checkNeighbours(T head1, List<T> data1) {
		boolean wrong = false;
		for (T p1 : data1) {
			for (T p2 : data1) {
				if (p1 != p2) {
					if (this.m.distance(p1, p2) < this.m
							.distance(p1, this.head)) {
						wrong = true;
					}
				}
			}
		}
		System.out.println(wrong ? "bad neighbours" : "good neighbours");

		return wrong;
	}

}
