package searchStructures.experimental.fplsh;

import java.util.Iterator;
import java.util.List;

import util.Range;
import coreConcepts.Metric;

public class VoronoiLSF<T> extends LSFunction<T> {

	public VoronoiLSF(List<T> refPoints, Metric<T> metric) {
		super(refPoints, metric);
	}

	@Override
	public Iterator<Boolean> bitProducer(T datum) {
		final int[] ptr = { 0 };
		final int[] nearest = { -1 };
		double nearestD = Double.MAX_VALUE;
		for (int i : Range.range(0, this.refPoints.size())) {
			double nextD = this.metric.distance(datum, this.refPoints.get(i));
			if (nextD < nearestD) {
				nearest[0] = i;
				nearestD = nextD;
			}
		}

		return new Iterator<Boolean>() {

			@Override
			public boolean hasNext() {
				return ptr[0] < VoronoiLSF.this.refPoints.size();
			}

			@SuppressWarnings("boxing")
			@Override
			public Boolean next() {
				return ptr[0]++ == nearest[0];
			}
		};
	}

	@Override
	protected int maxBits() {
		return this.refPoints.size();
	}

	@Override
	public void setSample(List<T> sampleData) {
		// not required
	}

	@Override
	public String getName() {
		return "vor";
	}

}
