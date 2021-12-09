package dataPoints.sparseCartesian;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import util.OrderedList;

/**
 * The intention of this class is to create an inverted index from a
 * DataSet<CompactEnsemble> so that it can be queried using the thresholded
 * version of JS
 * 
 * @author Richard Connor
 * @param <T>
 * 
 */
public class InvertedIndexDef3<T extends SparseCartesian> extends
		InvertedIndex<T> {

	public InvertedIndexDef3(Collection<T> data) {
		super(data);
	}

	public InvertedIndexDef3(File f) throws IOException {
		super(f);
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

		double[] accs = getScores(query, differenceFromPerfect);

		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < this.noOfDataPoints; i++) {
			if (accs[i] >= thresholdCalc) {
				res.add(i);
			}
		}
		return res;

	}

	@SuppressWarnings("boxing")
	private double[] getScores(T query, double differenceFromPerfect) {
		double[] accs = new double[this.noOfDataPoints];

		int[] queryDims = query.getDims();
		double[] queryVals = query.getValues();

		double maxPossSim = 0;

		int queryDimPntr = 0;
		for (int dim : queryDims) {

			assert this.invertedIndexIds != null : "no inverted index present";
			
			List<Integer> idPostingList = this.invertedIndexIds.get(dim);
			List<Double> valsPostingList = this.invertedIndexVals.get(dim);
			double v_i = queryVals[queryDimPntr];

			maxPossSim += v_i * 2 * LOG2;

			/*
			 * now, go through all the values in the posting list
			 */
			// there may be an event not in the original set
			if (idPostingList != null) {
				int postingListPntr = 0;
				for (int dataId : idPostingList) {
					assert dataId < accs.length : "dataId exceeds length of accs vector";
					if (accs[dataId] != -1) {
						double w_i = valsPostingList.get(postingListPntr);
						double termValue = getTermValue(v_i, w_i);

						double newAcc = accs[dataId] + termValue;

						if (newAcc < (maxPossSim - differenceFromPerfect)) {
							accs[dataId] = -1;
						} else {
							accs[dataId] = newAcc;
						}
					}
					postingListPntr++;
				}
			}
			queryDimPntr++;
		}
		return accs;
	}

	@SuppressWarnings("boxing")
	@Override
	public List<Integer> nearestNeighbour(T query, int numberOfNeighbours) {
		OrderedList<Integer, Double> ol = new OrderedList<Integer, Double>(
				numberOfNeighbours);

		double[] accs = getScores(query, 10);

		for (int i = 0; i < this.noOfDataPoints; i++) {
			ol.add(i, -accs[i]);
		}
		return ol.getList();
	}
}
