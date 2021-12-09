package sisap_2017_experiments;

import java.util.List;

import searchStructures.SearchIndex;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Util_ISpaper;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

public class MinimalSimplexTree<T> extends SearchIndex<T> {

	private List<T> refs;
	private NdimSimplex<T> simp;

	public MinimalSimplexTree(List<T> data, Metric<T> metric) {
		super(data, metric);
		int dim = (int) Math.floor(Math.log(data.size()) / Math.log(2)) + 1;
		this.refs = Util_ISpaper.getFFT(data, metric, dim);
		this.simp = new NdimSimplex<>(metric, this.refs);
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return "minSimplex";
	}

	public static void main(String[] a) throws Exception {

		TestContext tc = new TestContext(Context.colors);
		tc.setSizes(tc.dataSize() / 10, 0);
		MinimalSimplexTree<CartesianPoint> mst = new MinimalSimplexTree<>(
				tc.getData(), tc.metric());

		System.out.println(mst.refs.size());
	}

}
