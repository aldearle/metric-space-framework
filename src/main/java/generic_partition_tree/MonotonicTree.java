package generic_partition_tree;

import java.util.List;

import coreConcepts.Metric;

public abstract class MonotonicTree<T> extends GenericPartitionTree<T> {

	public enum Strategy {
		first, furthest, quiteNear
	};

	public enum TreeType {
		normal, monotonic, verylean
	}

	protected static Strategy refPointStrategy;
	protected static TreeType treeType;

	public static void setStrategy(Strategy s) {
		refPointStrategy = s;
	}

	public static void setTreeType(TreeType t) {
		treeType = t;
	}

	protected MonotonicTree(List<T> data, Metric<T> metric) {
		super(data, metric);
	}

	@Override
	protected ReferencePoints getRefPoints(int from, int to, int newP1) {

		if (this.treeType == TreeType.normal) {
			ReferencePoints rp = new ReferencePoints(from);
			setSecondRefPoint(from + 1, to, rp);
			rp.inc = 2;
			return rp;
		} else {
			ReferencePoints rp = new ReferencePoints(newP1);
			setSecondRefPoint(from, to, rp);
			rp.inc = 1;
			return rp;
		}

	}

	private void setSecondRefPoint(int from, int to, ReferencePoints rp) {

		T p1val = this.data[rp.p1].getValue();
		/*
		 * calculate distances from p1 for whole range; meantime maintain the
		 * biggest distance from p1 and note that as the value of p2
		 */
		double farDist = 0;
		int farRef = 0;
		double nearDist = Double.MAX_VALUE;
		int nearRef = 0;
		double firstDist = 0;

		for (int i = from; i <= to; i++) {
			final T value = this.data[i].getValue();
			final double d1 = this.metric.distance(value, p1val);
			if (i == from) {
				firstDist = d1;
				nearDist = d1;
				nearRef = i;
			}

			rp.p1memos.put(value, d1);
			if (d1 >= farDist) {
				farDist = d1;
				farRef = i;
			}
			if (nearDist == 0 || (d1 != 0 && d1 <= nearDist)) {
				nearDist = d1;
				nearRef = i;
			}
		}
		assert rp.p2 != -1 : "p2ref not set properly: " + from + ":" + to;

		if (this.refPointStrategy == Strategy.furthest) {
			swap(from, farRef);
			rp.p2 = from;
			rp.p1p2dist = farDist;
		} else if (this.refPointStrategy == Strategy.quiteNear) {
			swap(from, nearRef);
			rp.p2 = from;
			rp.p1p2dist = nearDist;
		} else {
			rp.p2 = from;
			rp.p1p2dist = firstDist;
		}
	}

	@Override
	public String getShortName() {
		return this.treeType == TreeType.monotonic ? "Mon"
				+ super.getShortName() : super.getShortName();
	}
}
