package dataPoints.sparseCartesian;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The intention of this class is to create an inverted index from a
 * DataSet<CompactEnsemble> so that it can be queried using the thresholded
 * version of JS
 * 
 * @author Richard Connor
 * @param <T>
 * 
 */
public class InvertedIndexDef2<T extends SparseCartesian> extends
		InvertedIndex<T> {

	/**
	 * create an instance from a collection of data
	 * 
	 * @param data
	 *            the data
	 */
	public InvertedIndexDef2(Collection<T> data) {
		super(data);
	}

	/**
	 * create an instance from a saved, verbose file
	 * 
	 * @param f
	 * @throws IOException
	 */
	public InvertedIndexDef2(File f) throws IOException {
		super(f);
	}
	
	/**
	 * create an instance from a saved, compact file
	 * 
	 * @param f
	 * @throws IOException
	 */
	public InvertedIndexDef2(InputStream f) throws Exception {
		super(f);
	}

	
	@Override
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

	@SuppressWarnings("boxing")
	private double[] getScores(T query) {
		double[] accs = new double[this.noOfDataPoints];

		int[] queryDims = query.getDims();
		double[] queryVals = query.getValues();

		int queryDimPntr = 0;
		for (int dim : queryDims) {

			List<Integer> idPostingList = this.invertedIndexIds.get(dim);
			List<Double> valsPostingList = this.invertedIndexVals.get(dim);
			double v_i = queryVals[queryDimPntr];
			/*
			 * now, go through all the values in the posting list
			 */
			// there may be an event not in the original set
			if (idPostingList != null) {
				int postingListPntr = 0;
				for (int dataId : idPostingList) {

					double w_i = valsPostingList.get(postingListPntr);
					accs[dataId] += getTermValue(v_i, w_i);
					postingListPntr++;
				}
			}
			queryDimPntr++;
		}
		return accs;
	}

	@Override
	public List<Integer> nearestNeighbour(T query, int numberOfNeighbours) {
		System.out.println("this function is not implemented!!");
		return null;
	}

}
