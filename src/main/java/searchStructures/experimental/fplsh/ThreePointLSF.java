package searchStructures.experimental.fplsh;

import java.util.Iterator;
import java.util.List;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import util.Range;
import coreConcepts.Metric;

public class ThreePointLSF<T> extends LSFunction<T> {

	double[] medians;
	boolean balanced;

	public ThreePointLSF(List<T> refPoints, Metric<T> metric, boolean balanced) {
		super(refPoints, metric);
		this.balanced = balanced;
	}

	@Override
	public Iterator<Boolean> bitProducer(final T datum) {
		return new Iterator<Boolean>() {
			final int[] ptr = { 0 };

			@Override
			public boolean hasNext() {
				return this.ptr[0] < ThreePointLSF.this.refPoints.size();
			}

			@SuppressWarnings("boxing")
			@Override
			public Boolean next() {
				double d1 = ThreePointLSF.this.metric.distance(datum,
						ThreePointLSF.this.refPoints.get(this.ptr[0]));
				double d2 = ThreePointLSF.this.metric.distance(
						datum,
						ThreePointLSF.this.refPoints.get((this.ptr[0] + 1)
								% ThreePointLSF.this.refPoints.size()));
				boolean res = d1 < d2;
				if (ThreePointLSF.this.balanced) {
					res = (d1 - d2) < ThreePointLSF.this.medians[this.ptr[0]];
				}
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
		if (balanced) {
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

					owds[pivotPtr][samplePtr] = new ObjectWithDistance<>(null,
							d1 - d2);
				}
			}

			for (int ptr : Range.range(0, this.refPoints.size())) {
				Quicksort.placeMedian(owds[ptr]);
				this.medians[ptr] = owds[ptr][sampleData.size() / 2]
						.getDistance();
			}
		}
	}

	@Override
	public String getName() {
		String b = this.balanced ? "b" : "";
		return "tpp" + b;
	}

}
