package searchStructures.experimental.fplsh;

import java.util.Iterator;
import java.util.List;

import util.OrderedList;
import util.Range;
import coreConcepts.Metric;

public class AbsPermLSF<T> extends LSFunction<T> {

	public AbsPermLSF(List<T> refPoints, Metric<T> metric) {
		super(refPoints, metric);
	}

	@SuppressWarnings("boxing")
	@Override
	public Iterator<Boolean> bitProducer(T datum) {
		OrderedList<Integer, Double> ol = new OrderedList<>(
				this.refPoints.size());
		for (int i : Range.range(0, this.refPoints.size())) {
			ol.add(i, this.metric.distance(datum, this.refPoints.get(i)));
		}
		final boolean[] bits = new boolean[this.refPoints.size()];
		final int threshold = this.refPoints.size() / 2;
		for (int i : Range.range(threshold, this.refPoints.size())) {
			bits[ol.getList().get(i)] = true;
		}
		final int[] ptr = { 0 };
		return new Iterator<Boolean>() {
			@Override
			public boolean hasNext() {
				return ptr[0] < AbsPermLSF.this.refPoints.size();
			}

			@Override
			public Boolean next() {
				return bits[ptr[0]++];
			}
		};
	}

	@Override
	protected int maxBits() {
		return this.refPoints.size();
	}

	@Override
	public void setSample(List<T> sampleData) {
		// nothing to do here
	}

	@Override
	public String getName() {
		return "abp";
	}

}
