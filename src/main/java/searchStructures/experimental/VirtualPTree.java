package searchStructures.experimental;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;
import coreConcepts.Metric;

public class VirtualPTree<T> extends SearchIndex<T> {

	int depth;
	int[][] permutationNumbering;

	/**
	 * In the case of this tree, the initial data supplied is only the list of
	 * permutands and does not include the bulk of the data
	 * 
	 * @param data
	 * @param metric
	 * @param depth
	 */
	protected VirtualPTree(List<T> data, Metric<T> metric, int depth) {
		super(data, metric);
		this.permutationNumbering = getPermutationNumbering();
		/*
		 * in principle, no more setup than this is required!
		 * 
		 * ... until the bulk data is added somewhere else...
		 */
	}

	private static int[][] getPermutationNumbering() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		double[] permDists = new double[this.data.size()];
		int ptr = 0;
		for (T perm : this.data) {
			double d = this.metric.distance(perm, query);
			permDists[ptr++] = d;
			if (d < t) {
				res.add(perm);
			}
		}

		// TODO the actual query!

		return res;
	}

	@Override
	public String getShortName() {
		return "VPermHTree";
	}
}
