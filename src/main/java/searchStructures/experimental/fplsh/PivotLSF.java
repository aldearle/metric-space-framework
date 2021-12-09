package searchStructures.experimental.fplsh;

import java.util.Iterator;
import java.util.List;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import util.Range;
import coreConcepts.Metric;

public class PivotLSF<T> extends LSFunction<T> {

	double[] medians;

	public PivotLSF(List<T> refPoints, Metric<T> metric) {
		super(refPoints, metric);
	}

	@Override
	public Iterator<Boolean> bitProducer(final T datum) {
		return new Iterator<Boolean>() {
			final int[] ptr = { 0 };

			@Override
			public boolean hasNext() {
				return this.ptr[0] < PivotLSF.this.refPoints.size();
			}

			@SuppressWarnings("boxing")
			@Override
			public Boolean next() {
				double d = PivotLSF.this.metric.distance(datum,
						PivotLSF.this.refPoints.get(this.ptr[0]));
				return d < PivotLSF.this.medians[this.ptr[0]++];
			}
		};
	}

	@Override
	protected int maxBits() {
		return this.refPoints.size();
	}

	@Override
	public void setSample(List<T> sampleData) {
		this.medians = new double[this.refPoints.size()];

		@SuppressWarnings("unchecked")
		ObjectWithDistance<Object>[][] owds = new ObjectWithDistance[this.refPoints
				.size()][sampleData.size()];
		for (int samplePtr : Range.range(0, sampleData.size())) {
			T sample = sampleData.get(samplePtr);
			for (int pivotPtr : Range.range(0, this.refPoints.size())) {
				T pivot = this.refPoints.get(pivotPtr);
				double d = this.metric.distance(sample, pivot);
				owds[pivotPtr][samplePtr] = new ObjectWithDistance<>(null, d);
			}
		}
		for (int ptr : Range.range(0, this.refPoints.size())) {
			Quicksort.placeMedian(owds[ptr]);
			this.medians[ptr] = owds[ptr][sampleData.size() / 2].getDistance();
		}
	}

	@Override
	public String getName() {
		return "piv";
	}

}
