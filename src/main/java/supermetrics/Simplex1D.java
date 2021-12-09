package supermetrics;

import java.util.List;

import n_point_surrogate.SimplexExclusion;
import coreConcepts.Metric;

public class Simplex1D<T> extends SimplexExclusion<T> {

	public Simplex1D(int dimension, Metric<T> metric, T... refPoints) throws Exception {
		super(dimension, metric, refPoints);
	}

	@Override
	public double[] formSimplex(T p) {
		T ref = this.referencePoints[0];
		double[] res = new double[1];
		res[0] = this.metric.distance(p, ref);
		return res;
	}

	@Override
	public float[] formSimplexF(T p) {
		// TODO Auto-generated method stub
		return null;
	}

}
