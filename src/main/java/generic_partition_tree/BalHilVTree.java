package generic_partition_tree;

import java.util.List;
import java.util.Map;

import searchStructures.Quicksort;
import coreConcepts.Metric;

public class BalHilVTree<T> extends MonotonicTree<T> {

	public BalHilVTree(List<T> data, Metric<T> metric) {
		super(data, metric);
	}

	@SuppressWarnings("boxing")
	@Override
	public int assignSplitValues(int from, int to, Map<T, Double> p1dists,
			Map<T, Double> p2dists, double pDist) {

		for (int i = from; i <= to; i++) {
			final Double d1 = p1dists.get(this.data[i].getValue());
			final Double d2 = p2dists.get(this.data[i].getValue());
			assert d1 != null : "d1 is null";
			assert d2 != null : "d2 is null";
			double diff = (d1 * d1 - d2 * d2) / (2 * pDist);

			this.data[i].setDistance(diff);
		}
		int pSize = (to - from) + 1;
		int middleToRightOffset = from + (pSize * 4) / 20;

		Quicksort.partitionToPivotPoint(this.data, from, to,
				middleToRightOffset);
		return middleToRightOffset;
	}

	@Override
	public String getShortName() {
		return "BalHilV" + super.getShortName();
	}

	@Override
	protected boolean canExcludeLeft(double threshold, CoverRadii cr,
			double d1, final double d2, double pDist, double splitValue) {

		double diff = (d1 * d1 - d2 * d2) / (2 * pDist);
		return canExcludeLeftCr(d1, d2, threshold, cr)
				|| diff >= threshold + splitValue;
	}

	@Override
	protected boolean canExcludeRight(double threshold, CoverRadii cr,
			double d1, final double d2, double pDist, double splitValue) {
		double diff = (d1 * d1 - d2 * d2) / (2 * pDist);
		return canExcludeRightCr(d1, d2, threshold, cr)
				|| diff < -(threshold) + splitValue;
	}

}
