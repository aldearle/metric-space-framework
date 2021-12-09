package sisap_2017_experiments.laesa;

import java.util.ArrayList;
import java.util.List;

import sisap_2017_experiments.NdimSimplex;
import util.Range;
import coreConcepts.Metric;

public class NpointLaesa<T> extends Laesa<T> {

	protected double[] memory;
	private NdimSimplex<T> simplex;

	public NpointLaesa(Metric<T> metric, int dimension) {
		super(metric, dimension);
	}

	@Override
	public void setDataAndRefPoints(List<T> newData, List<T> refPoints) {
		super.setDataAndRefPoints(newData, refPoints);
		this.memory = new double[newData.size() * this.dim];
	}

	@Override
	public void setupTable() {

		this.simplex = new NdimSimplex<>(this.metric, this.refPoints);

		int dataPtr = 0;
		for (T p : this.data) {
			double[] dists = new double[this.dim];
			for (int i : Range.range(0, this.dim)) {
				dists[i] = this.metric.distance(p, this.refPoints.get(i));
			}
			double[] ap = this.simplex.getApex(dists);
			for (int i : Range.range(0, this.dim)) {
				this.memory[(dataPtr * this.dim) + i] = ap[i];
			}
			dataPtr++;
		}

	}

	@Override
	public List<T>[] filter(T query, double threshold) {
		double[] dists = getRefDists(query);
		List<T>[] res = new ArrayList[2];
		res[0] = new ArrayList<>();
		res[1] = new ArrayList<>();
		for (int i : Range.range(0, dists.length)) {
			if (dists[i] <= threshold) {
				res[1].add(this.refPoints.get(i));
			}
		}
		double[] apex = this.simplex.getApex(dists);

		double thresh = threshold * threshold;

		for (int i : Range.range(0, this.data.size())) {
			boolean excluded = false;
			double acc = 0;
			int ptr = 0;
			while (!excluded && ptr < this.dim) {
				double diff = apex[ptr] - this.memory[(i * this.dim) + ptr];
				if (diff == 0) {
					ptr = this.dim;
				} else {
					acc += diff * diff;
					if (acc > thresh) {
						excluded = true;
					} else {
						ptr++;
					}
				}
			}
			if (!excluded) {
				if (upperBound(apex, i) < thresh) {
					res[1].add(this.data.get(i));
				} else {
					res[0].add(this.data.get(i));
				}
			}
		}
		return res;
	}

	private double upperBound(double[] apex, int i) {
		double acc = 0;
		for (int ptr = 0; ptr < apex.length - 1; ptr++) {
			double diff = apex[ptr] - this.memory[(i * this.dim) + ptr];
			acc += diff * diff;
		}
		double sum = apex[apex.length - 1]
				+ this.memory[(i * this.dim) + (apex.length - 1)];
		acc += sum * sum;
		return acc;
	}

	@Override
	public String getName() {
		return "npoint";
	}

}
