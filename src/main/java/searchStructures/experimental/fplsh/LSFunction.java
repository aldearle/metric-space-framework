package searchStructures.experimental.fplsh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import util.Range;
import coreConcepts.Metric;

/**
 * 
 * an abstract class for any mechanisms which implements a family of locality
 * sensitive family of functions to produce bits, based only on a collection of
 * reference points and a metric
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data
 */
public abstract class LSFunction<T> {

	protected Metric<T> metric;
	protected List<T> refPoints;

	public LSFunction(List<T> refPoints, Metric<T> metric) {

		this.refPoints = refPoints;
		this.metric = metric;
	}

	public abstract Iterator<Boolean> bitProducer(T datum);

	protected abstract int maxBits();

	@SuppressWarnings("boxing")
	public List<Integer> getBitClusters(T datum, int width, int clusters) {

		final int bitsReqd = width * clusters;
		final Iterator<Boolean> bitProducer = this.bitProducer(datum);
		final List<Integer> res = new ArrayList<>();

		int currentWord = 0;

		for (int i : Range.range(1, bitsReqd + 1)) {
			int pushable = bitProducer.next() ? 1 : 0;
			currentWord = (currentWord << 1) + pushable;

			if (i % width == 0) {
				res.add(currentWord);
				currentWord = 0;
			}
		}

		return res;
	}

	/**
	 * @param sampleData
	 *            the data to set values for the whole set
	 */
	public abstract void setSample(List<T> sampleData);

	public abstract String getName();

}
