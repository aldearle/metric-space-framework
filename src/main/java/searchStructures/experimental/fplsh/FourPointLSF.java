package searchStructures.experimental.fplsh;

import java.util.Iterator;
import java.util.List;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import util.Range;
import coreConcepts.Metric;

public class FourPointLSF<T> extends LSFunction<T> {

	double[] distToNextPivot;
	double[] medians;

	public FourPointLSF(List<T> refPoints, Metric<T> metric) {
		super(refPoints, metric);
		this.distToNextPivot = new double[refPoints.size()];
		for (int i : Range.range(0, this.refPoints.size())) {
			this.distToNextPivot[i] = this.metric.distance(
					this.refPoints.get(i),
					this.refPoints.get((i + 1) % this.refPoints.size()));
		}
	}

	@Override
	public Iterator<Boolean> bitProducer(final T datum) {
		return new Iterator<Boolean>() {
			final int[] ptr = { 0 };

			@Override
			public boolean hasNext() {
				return this.ptr[0] < FourPointLSF.this.refPoints.size();
			}

			@SuppressWarnings("boxing")
			@Override
			public Boolean next() {
				double d1 = FourPointLSF.this.metric.distance(datum,
						FourPointLSF.this.refPoints.get(this.ptr[0]));
				double d2 = FourPointLSF.this.metric.distance(
						datum,
						FourPointLSF.this.refPoints.get((this.ptr[0] + 1)
								% FourPointLSF.this.refPoints.size()));
				boolean res = ((d1 * d1 - d2 * d2) / FourPointLSF.this.distToNextPivot[this.ptr[0]]) < FourPointLSF.this.medians[this.ptr[0]];
				this.ptr[0]++;
				return res;
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
				T pivot1 = this.refPoints.get(pivotPtr);
				T pivot2 = this.refPoints.get((pivotPtr + 1)
						% this.refPoints.size());
				double d1 = this.metric.distance(sample, pivot1);
				double d2 = this.metric.distance(sample, pivot2);

				owds[pivotPtr][samplePtr] = new ObjectWithDistance<>(null, (d1
						* d1 - d2 * d2)
						/ this.distToNextPivot[pivotPtr]);
			}
		}

		for (int ptr : Range.range(0, this.refPoints.size())) {
			Quicksort.placeMedian(owds[ptr]);
			this.medians[ptr] = owds[ptr][sampleData.size() / 2].getDistance();
		}
	}

	@Override
	public String getName() {
		return "fpp";
	}

}
