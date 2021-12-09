package generic_partition_tree;

import java.util.List;
import java.util.Map;

import searchStructures.Quicksort;
import coreConcepts.Metric;

public class BalHypTree<T> extends MonotonicTree<T> {

	public BalHypTree(List<T> data, Metric<T> metric) {
		super(data, metric);
	}

	@SuppressWarnings("boxing")
	@Override
	public int assignSplitValues(int from, int to, Map<T, Double> p1dists,
			Map<T, Double> p2dists, double pDist) {

		for (int i = from; i <= to; i++) {
			final Double d1 = p1dists.get(this.data[i].getValue());
			final Double d2 = p2dists.get(this.data[i].getValue());
			double diff = d1 - d2;
			this.data[i].setDistance(diff);
		}
		int pSize = (to - from) + 1;
		int middleToRightOffset = from + pSize / 2;

		Quicksort.partitionToPivotPoint(this.data, from, to,
				middleToRightOffset);
		return middleToRightOffset;
	}

	@Override
	public String getShortName() {
		return "BalHyp" + super.getShortName();
	}

	@Override
	protected boolean canExcludeLeft(double threshold, CoverRadii cr,
			double p1dist, final double p2dist, double pDist, double splitValue) {
		return canExcludeLeftCr(p1dist, p2dist, threshold, cr)
				|| p1dist - p2dist >= 2 * threshold + splitValue;
	}

	@Override
	protected boolean canExcludeRight(double threshold, CoverRadii cr,
			double p1dist, final double p2dist, double pDist, double splitValue) {
		return canExcludeRightCr(p1dist, p2dist, threshold, cr)
				|| p1dist - p2dist < -(2 * threshold) + splitValue;
	}

}
