package generic_partition_tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import searchStructures.ObjectWithDistance;
import coreConcepts.Metric;

public class UnbalHilTree<T> extends MonotonicTree<T> {

	public UnbalHilTree(List<T> data, Metric<T> metric) {
		super(data, metric);
	}

	@SuppressWarnings("boxing")
	@Override
	public int assignSplitValues(int from, int to, Map<T, Double> p1dists,
			Map<T, Double> p2dists, double pDist) {

		List<ObjectWithDistance<T>> neg = new ArrayList<>();
		List<ObjectWithDistance<T>> pos = new ArrayList<>();

		for (int i = from; i <= to; i++) {
			final Double d1 = p1dists.get(this.data[i].getValue());
			final Double d2 = p2dists.get(this.data[i].getValue());
			// assert d1 != null : "d1 is null";
			// assert d2 != null : "d2 is null";
			// double diff = (d1 * d1 - d2 * d2) / (2 * pDist);
			//
			// this.data[i].setDistance(diff);
			if (d1 - d2 < 0) {
				neg.add(data[i]);
			} else {
				pos.add(data[i]);
			}
		}
		int ptr = from;
		for (ObjectWithDistance<T> o : neg) {
			data[ptr++] = o;
		}
		for (ObjectWithDistance<T> o : pos) {
			data[ptr++] = o;
		}

		return from + neg.size();
	}

	@Override
	public String getShortName() {
		return "Hil" + super.getShortName();
	}

	@Override
	protected boolean canExcludeLeft(double threshold, CoverRadii cr,
			double d1, final double d2, double pDist, double splitValue) {
		double diff = (d1 * d1 - d2 * d2) / (2 * pDist);
		return canExcludeLeftCr(d1, d2, threshold, cr) || diff >= threshold;
	}

	@Override
	protected boolean canExcludeRight(double threshold, CoverRadii cr,
			double d1, final double d2, double pDist, double splitValue) {
		double diff = (d1 * d1 - d2 * d2) / (2 * pDist);
		return canExcludeRightCr(d1, d2, threshold, cr) || diff < -(threshold);
	}

}
