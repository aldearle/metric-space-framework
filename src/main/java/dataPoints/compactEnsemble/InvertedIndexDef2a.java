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
public class InvertedIndexDef2a<T extends CompactEnsemble> extends
		InvertedIndex<T> {

	public InvertedIndexDef2a(DataSet<T> data) {
		super(data);
	}

	/**
	 * 
	 * @param query
	 * @param threshold
	 * @return
	 */
	@SuppressWarnings("boxing")
	public List<Integer> thresholdQuery(T query, double threshold) {

		double thresholdCalc = (2 - 2 * threshold * threshold) * LOG2;

		double[] accs = getScores(query);

		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < this.noOfDataPoints; i++) {
			if (accs[i] >= thresholdCalc) {
				res.add(i);
			}
		}
		return res;

	}

	private double[] getScores(T query) {
		double[] accs = new double[this.noOfDataPoints];

		int[] queryEns = query.getEnsemble();
		int queryCard = query.getCardinality();

		for (int comp : queryEns) {
			int eventId = EventToIntegerMap.getEventCode(comp);
			int eventCount = EventToIntegerMap.getCard(comp);
			double v_i = eventCount / (double) queryCard;

			List<Integer> postingList = this.invertedIndex.get(eventId);
			/*
			 * now, go through all the value in the posting list
			 */
			// there may be an event not in the original set
			if (postingList != null) {
				for (int i : postingList) {
					int dataId = i >>> 14;
					if (accs[dataId] != -1) {
						int residue = i & fourteenBitMask;
						int card = residue >>> 6;
						int count = residue & sixBitMask;

						double w_i = count / (double) card;

						accs[dataId] += getTermValue(v_i, w_i);
					}

				}
			}
		}
		return accs;
	}

	public int nearestNeighbour(T query) {
		return 0;
	}

	@Override
	public List<Integer> nearestNeighbour(T query, int numberOfNeighbours) {
		System.out.println("this function is not implemented!!");
		return null;
	}

}
