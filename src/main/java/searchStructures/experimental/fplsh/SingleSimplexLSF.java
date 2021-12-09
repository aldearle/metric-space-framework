package searchStructures.experimental.fplsh;

import java.util.Iterator;
import java.util.List;

import n_point_surrogate.SimplexND;
import util.Range;
import coreConcepts.Metric;

/**
 * @author Richard Connor
 * 
 *         this mechanisms builds an n-1 dimensional simplex from n given
 *         reference points, so than an n-dimensional point can be generated as
 *         a new apex for any new point
 * 
 *         sampling gives mean (maybe should be median?) values for each
 *         dimension
 * 
 *         for each dimension in classification, one bit is produced for each
 *         dimension according to whether it is above or below the mean
 *
 * @param <T>
 */
public class SingleSimplexLSF<T> extends LSFunction<T> {

	private int dim;
	private SimplexND<T> simp;
	private double[] dimensionMeans;

	public SingleSimplexLSF(List<T> refPoints, Metric<T> metric) {
		super(refPoints, metric);
		this.dim = refPoints.size();
		try {
			this.simp = new SimplexND<>(this.dim, metric, refPoints);
		} catch (Exception e) {
			throw new RuntimeException("can't create simplex: "
					+ e.getMessage());
		}
	}

	@Override
	public Iterator<Boolean> bitProducer(T datum) {
		double[] dists = getRefDists(datum);
		final double[] newApex = this.simp.formSimplex(dists);
		final int[] thisDim = { 0 };
		return new Iterator<Boolean>() {

			@SuppressWarnings("synthetic-access")
			@Override
			public boolean hasNext() {
				return thisDim[0] < SingleSimplexLSF.this.dim;
			}

			@SuppressWarnings({ "boxing", "synthetic-access" })
			@Override
			public Boolean next() {
				final boolean res = newApex[thisDim[0]] < SingleSimplexLSF.this.dimensionMeans[thisDim[0]++];
				return res;
			}
		};
	}

	private double[] getRefDists(T datum) {
		double[] res = new double[this.refPoints.size()];
		for (int i : Range.range(0, res.length)) {
			res[i] = this.metric.distance(datum, this.refPoints.get(i));
		}
		return res;
	}

	@Override
	protected int maxBits() {
		return this.dim;
	}

	@Override
	public void setSample(List<T> sampleData) {
		this.dimensionMeans = new double[this.dim];

		for (T datum : sampleData) {
			double[] dists = getRefDists(datum);
			double[] ap = this.simp.formSimplex(dists);
			for (int i : Range.range(0, ap.length)) {
				this.dimensionMeans[i] += ap[i];
			}
		}
		for (int i : Range.range(0, this.dimensionMeans.length)) {
			this.dimensionMeans[i] /= sampleData.size();
		}
	}

	@Override
	public String getName() {
		return "sim";
	}

}
