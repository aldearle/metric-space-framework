package dataPoints.compactEnsemble;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import util.ConstantsAndArith;
import coreConcepts.DataSet;

/**
 * The intention of this class is to create an inverted index from a
 * DataSet<CompactEnsemble> so that it can be queried using the thresholded
 * version of JS
 * 
 * @author Richard Connor
 * @param <T>
 * 
 */
public abstract class InvertedIndex<T extends CompactEnsemble> {

	/*
	 * the core data structure of the index is a map from event id -> data id ->
	 * frequency
	 * 
	 * frequency, to start with, we are going to store as a single integer
	 * containing count and cardinality
	 * 
	 * or... a single integer can contain id, cardinality and count 16/8/8 bits
	 */

	static int eightBitMask = 0xFF;
	static int sixteenBitMask = 0xFFFF;
	static int fourteenBitMask = 0x3FFF;
	static int sixBitMask = 0x3F;

	// static int leftSixteenBitMask = 0xFFFF0000;
	// static int leftTwentyFourBitMask = 0xFFFFFF00;

	protected static final double LOG2 = Math.log(2);
	protected Map<Integer, List<Integer>> invertedIndex;
	protected int noOfDataPoints;

	@SuppressWarnings("boxing")
	protected void addToIndex(int dimension, int composite) {
		List<Integer> l = this.invertedIndex.get(dimension);
		if (l == null) {
			l = new ArrayList<Integer>();
			this.invertedIndex.put(dimension, l);
		}
		l.add(composite);
	}

	public InvertedIndex(DataSet<T> data) {
		this.invertedIndex = new TreeMap<Integer, List<Integer>>();
		this.noOfDataPoints = data.size();

		int dataId = 0;
		for (T datum : data) {
			int[] ens = datum.getEnsemble();
			int card = datum.getCardinality();
			for (int field : ens) {
				int eventId = EventToIntegerMap.getEventCode(field);
				int count = EventToIntegerMap.getCard(field);

				int composite = (dataId << 14) + (card << 6) + count;

				addToIndex(eventId, composite);
			}

			dataId++;
		}
	}

	/**
	 * 
	 * @param query
	 * @param threshold
	 * @return
	 */
	@SuppressWarnings("boxing")
	public abstract List<Integer> thresholdQuery(T query, double threshold);

	/**
	 * 
	 * @param query
	 * @param threshold
	 * @return
	 */
	@SuppressWarnings("boxing")
	public abstract List<Integer> nearestNeighbour(T query,
			int numberOfNeighbours);

	protected static double getTermValue(double v_i, double w_i) {
		if (v_i == w_i) {
			return v_i * 2 * LOG2;
		} else {
			return (v_i + w_i) * Math.log(v_i + w_i) - v_i * Math.log(v_i)
					- w_i * Math.log(w_i);
		}
	}

	/**
	 * @return the number of objects contained in this inverted index
	 */
	public int size() {
		return noOfDataPoints;
	}

}
