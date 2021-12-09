package sisap_2017_experiments.laesa;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;
import coreConcepts.Metric;

public abstract class Laesa<T> {

	protected Metric<T> metric;
	protected int dim;
	protected List<T> data;
	protected List<T> refPoints;

	public Laesa(Metric<T> metric, int dimension) {
		this.metric = metric;
		this.dim = dimension;
	}

	public void setDataAndRefPoints(List<T> newData, List<T> refPoints) {
		this.data = newData;
		this.refPoints = refPoints;
	}

	public abstract void setupTable();

	public abstract List<T>[] filter(T query, double threshold);

	public abstract String getName();

	protected double[] getRefDists(T query) {
		double[] dists = new double[this.dim];
		for (int i = 0; i < this.dim; i++) {
			dists[i] = this.metric.distance(query, this.refPoints.get(i));
		}
		return dists;
	}

	public static <T> SearchIndex<T> getSearchIndex(Laesa<T> filter,
			final Metric<T> metric, List<T> data, List<T> refPoints) {

		class LaesaIndex extends SearchIndex<T> {

			Laesa<T> filter;

			public LaesaIndex(List<T> data, List<T> refPoints,
					Metric<T> metric, Laesa<T> filter) {
				super(data, metric);
				this.filter = filter;
				this.filter.setDataAndRefPoints(data, refPoints);
				this.filter.setupTable();
			}

			@Override
			public List<T> thresholdSearch(T query, double t) {
				List<T>[] filtrate = this.filter.filter(query, t);
				List<T> res = new ArrayList<>();
				for (T r : filtrate[0]) {
					double d = this.metric.distance(query, r);
					if (d <= t) {
						res.add(r);
					}
				}
				for (T r : filtrate[1]) {
					res.add(r);
				}
				return res;
			}

			@Override
			public String getShortName() {
				return "laesaIndex" + this.filter.getName();
			}
		}
		return new LaesaIndex(data, refPoints, metric, filter);
	}

}
