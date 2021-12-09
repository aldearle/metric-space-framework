package generic_partition_tree;

import java.util.List;
import java.util.Map;

import searchStructures.Quicksort;
import coreConcepts.Metric;

public class BalHilHTree<T> extends MonotonicTree<T> {

	public BalHilHTree(List<T> data, Metric<T> metric) {
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
			double height = getHeight(pDist, d1, d2);

			this.data[i].setDistance(height);
		}
		int pSize = (to - from) + 1;
		int middleToRightOffset = from + (pSize * 4) / 20;

		Quicksort.partitionToPivotPoint(this.data, from, to,
				middleToRightOffset);
		return middleToRightOffset;
	}

	protected double getHeight(double pDist, final Double d1, final Double d2) {
		if (pDist == 0) {
			return d1;
		} else {
			// distance from Y axis, may be negative or positive
			double diff = (d1 * d1 - d2 * d2) / (2 * pDist);
			double s1 = (pDist / 2) + diff;
			double height = Math.sqrt(d1 * d1 - s1 * s1);
			return height;
		}
	}

	@Override
	public String getShortName() {
		return "BalHilH" + super.getShortName();
	}

	@Override
	protected boolean canExcludeLeft(double threshold, CoverRadii cr,
			double d1, final double d2, double pDist, double splitValue) {

		double height = getHeight(pDist, d1, d2);
		return canExcludeLeftCr(d1, d2, threshold, cr)
				|| height >= threshold + splitValue;
	}

	@Override
	protected boolean canExcludeRight(double threshold, CoverRadii cr,
			double d1, final double d2, double pDist, double splitValue) {
		double height = getHeight(pDist, d1, d2);
		return canExcludeRightCr(d1, d2, threshold, cr)
				|| height < -(threshold) + splitValue;
	}

}
