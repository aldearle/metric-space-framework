package dataPoints.compactEnsemble;

import java.util.ArrayList;
import java.util.List;

import util.OrderedList;
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
public class InvertedIndexDef3<T extends CompactEnsemble> extends
		InvertedIndex<T> {

	public InvertedIndexDef3(DataSet<T> data) {
		super(data);
	}

	/**
	 * 
	 * @param query
	 * @param threshold
	 * @return
	 */
	@Override
	@SuppressWarnings("boxing")
	public List<Integer> thresholdQuery(T query, double threshold) {

		double thresholdCalc = (2 - 2 * threshold * threshold) * LOG2;
		double differenceFromPerfect = 2 * LOG2 - thresholdCalc;

		double[] accs = accumulate(query, differenceFromPerfect);

		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < this.noOfDataPoints; i++) {
			if (accs[i] >= thresholdCalc) {
				res.add(i);
			}
		}
		return res;

	}

	private double[] accumulate(T query, double differenceFromPerfect) {
		double[] accs = new double[this.noOfDataPoints];
		int[] queryEns = query.getEnsemble();
		int queryCard = query.getCardinality();

		double maxPossSim = 0;

		for (int comp : queryEns) {
			int eventId = EventToIntegerMap.getEventCode(comp);
			int eventCount = EventToIntegerMap.getCard(comp);
			double v_i = eventCount / (double) queryCard;

			maxPossSim += v_i * 2 * LOG2;

			List<Integer> postingList = this.invertedIndex.get(eventId);
			/*
			 * now, go through all the value in the posting list
			 */
			// there may be an event not in the original set
			if (postingList != null) {
				for (int i : postingList) {
					int dataId = i >>> 14;

					if (accs[dataId] != -1) {
						int residue = i & fourteenBitMask;// %
															// Constants.sixteenBitModMask;
						int card = residue >>> 6;
						int count = residue & sixBitMask;// % 256;

						double w_i = count / (double) card;

						final double termValue = getTermValue(v_i, w_i);
						if( termValue == Double.NaN){
							System.out.println(v_i + w_i);
						}
						double newAcc = accs[dataId] + termValue;

						if (newAcc < (maxPossSim - differenceFromPerfect)) {
							accs[dataId] = -1;
						} else {
							accs[dataId] = newAcc;
						}
					}

				}
			}
		}
		return accs;
	}

	@SuppressWarnings("boxing")
	@Override
	public List<Integer> nearestNeighbour(T query, int numberOfNeighbours) {
		OrderedList<Integer, Double> ol = new OrderedList<Integer, Double>(
				numberOfNeighbours);

		double[] accs = accumulate(query, 10);
		double best = 0;
		int bestMatch = -1;

		for (int i = 0; i < this.noOfDataPoints; i++) {
			ol.add(i, -accs[i]);
		}
		return ol.getList();
	}
}
