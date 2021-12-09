package dataPoints.sparseCartesian;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import util.OrderedList;

/**
 * This is a fixed version of Def 3 with a bug removed... copied from the
 * Analytic version with the analysis removed, I think
 * 
 * @author Richard Connor
 * @param <T>
 * 
 */
public class InvertedIndexDef3Correct<T extends SparseCartesian> extends
		InvertedIndex<T> {

	public InvertedIndexDef3Correct(Collection<T> data) {
		super(data);
	}

	public InvertedIndexDef3Correct(File f) throws IOException {
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

		// this is what the accumulator must add to before the point is within
		// the threshold
		double minAccumulatorThreshold = (2 - 2 * threshold * threshold) * LOG2;

		// so, if at any point an accumulator drops this much below what is
		// possible, it can never recover
		double differenceFromPerfect = 2 * LOG2 - minAccumulatorThreshold;

		double[] accs = getScores(query, differenceFromPerfect);

		List<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < this.noOfDataPoints; i++) {
			// used to test for threshold calc which was correct...
			if (accs[i] > minAccumulatorThreshold) {
				res.add(i);
			}
		}
		return res;
	}

	@SuppressWarnings("boxing")
	private double[] getScores(T query, double differenceFromPerfect) {

		double[] accs = new double[this.noOfDataPoints];
		double[] w_i_accs = new double[this.noOfDataPoints];
		double v_i_acc = 0;

		int[] queryDims = query.getDims();
		double[] queryVals = query.getValues();

		int queryDimPntr = 0;
		for (int dim : queryDims) {

			assert this.invertedIndexIds != null : "no inverted index present";

			List<Integer> idPostingList = this.invertedIndexIds.get(dim);
			List<Double> valsPostingList = this.invertedIndexVals.get(dim);
			double v_i = queryVals[queryDimPntr];
			v_i_acc += v_i;

			/*
			 * now, go through all the values in the posting list
			 */
			// there may be an positive dimension in the query which does not
			// occur in the data set
			if (idPostingList != null) {
				int postingListPntr = 0;
				for (int dataId : idPostingList) {
					assert dataId < accs.length : "dataId exceeds length of accs vector";
					if (accs[dataId] != -1) {
						double w_i = valsPostingList.get(postingListPntr);
						w_i_accs[dataId] += w_i;

						double residueTerm = getTermValue(1 - v_i_acc,
								1 - w_i_accs[dataId]);

						double termValue = getTermValue(v_i, w_i);

						double newAcc = accs[dataId] + termValue;

						/*
						 * here is the correct version of the dropout test
						 */
						if (newAcc + residueTerm < (LOG2 * 2 - differenceFromPerfect)) {

							/*
							 * this is the initial test which seems to be a very
							 * good approximation and is very cheap to execute
							 * but unfortunately is not quite safe....
							 */
							// if (newAcc < (maxPossSim -
							// differenceFromPerfect)) {

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
