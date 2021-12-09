package searchStructures;

import java.util.ArrayList;
import java.util.List;

public abstract class SemiSorter<T> {

	private List<T> data;
	private List<T> leftList;
	private List<T> rightList;
	private double pivotValue;

	public SemiSorter(List<T> inputData) {
		this.data = inputData;
		splitInHalf();
	}

	public List<T> getLeft() {
		return this.leftList;
	}

	public double getPivotDistance() {
		return this.pivotValue;
	}

	public List<T> getRight() {
		return this.rightList;
	}

	public abstract double measure(T d);

	private void setPivot(double pivot) {
		this.pivotValue = pivot;
	}

	private void splitInHalf() {

		this.leftList = new ArrayList<>();
		this.rightList = new ArrayList<>();
		@SuppressWarnings("unchecked")
		ObjectWithDistance<T>[] objs = new ObjectWithDistance[this.data.size()];
		int ptr = 0;
		for (T d : this.data) {
			double distance = measure(d);
			ObjectWithDistance<T> owd = new ObjectWithDistance<>(d, distance);
			objs[ptr++] = owd;
		}
		Quicksort.placeMedian(objs);

		int halfWay = (this.data.size()) / 2;
		this.setPivot(objs[halfWay].getDistance());

		for (int i = 0; i < this.data.size(); i++) {
			final T value = objs[i].getValue();
			if (i < halfWay) {
				this.leftList.add(value);
			} else {
				this.rightList.add(value);
			}
		}
	}
}
