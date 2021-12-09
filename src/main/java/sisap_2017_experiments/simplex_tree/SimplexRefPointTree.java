package sisap_2017_experiments.simplex_tree;

import java.util.ArrayList;
import java.util.List;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import searchStructures.SearchIndex;
import sisap_2017_experiments.NdimSimplex;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

public class SimplexRefPointTree<T> extends SearchIndex<T> {

	protected List<T> refPoints;
	protected TreeNode root;
	private double[][] refPointDists;
	private NdimSimplex<T> simp;

	private class TreeNode {
		private int depth;
		private double offset;
		private TreeNode left, right;
		private int value = -1;
		private double lCr, rCr;

		@SuppressWarnings("boxing")
		TreeNode(List<Integer> dataRefs, int depth) {
			this.depth = depth;
			if (dataRefs.size() > 1) {
				@SuppressWarnings("unchecked")
				ObjectWithDistance<Integer>[] owds = new ObjectWithDistance[dataRefs
						.size()];
				for (int i : Range.range(0, dataRefs.size())) {
					int dat = dataRefs.get(i);
					owds[i] = new ObjectWithDistance<>(
							dat,
							SimplexRefPointTree.this.refPointDists[dat][depth]
									- SimplexRefPointTree.this.refPointDists[dat][depth + 1]);
				}
				Quicksort.placeMedian(owds);
				this.offset = owds[owds.length / 2].getDistance();
				List<Integer> leftList = new ArrayList<>();
				List<Integer> rightList = new ArrayList<>();
				this.lCr = 0;
				for (int i : Range.range(0, owds.length / 2)) {
					leftList.add(owds[i].getValue());
					this.lCr = Math.max(this.lCr,
							refPointDists[owds[i].getValue()][depth]);
				}
				this.rCr = 0;
				for (int i : Range.range(owds.length / 2, owds.length)) {
					rightList.add(owds[i].getValue());
					this.rCr = Math.max(this.rCr,
							refPointDists[owds[i].getValue()][depth + 1]);
				}
				if (leftList.size() > 0) {
					this.left = new TreeNode(leftList, depth + 1);
				}
				if (rightList.size() > 0) {
					this.right = new TreeNode(rightList, depth + 1);
				}
			} else {
				this.value = dataRefs.get(0);
			}
		}

		public int card() {
			if (this.value == -1) {
				int res = 0;
				if (this.left != null) {
					res += this.left.card();
				}
				if (this.right != null) {
					res += this.right.card();
				}
				return res;
			} else {
				return 1;
			}
		}

		public void search(T q, double[] pds, double[] apex, List<T> res,
				double t) {
			if (this.value == -1) {
				if (this.left != null) {
					if (!(pds[depth] - pds[depth + 1] > (this.offset + 2 * t))) {
						if (!(pds[depth] > this.lCr + t)) {
							this.left.search(q, pds, apex, res, t);
						}
					}
				}
				if (this.right != null) {
					if (!(pds[depth] - pds[depth + 1] < (this.offset - 2 * t))) {
						if (!(pds[depth + 1] > this.rCr + t)) {
							this.right.search(q, pds, apex, res, t);
						}
					}
				}
			} else if (NdimSimplex.l2Flex(apex, refPointDists[this.value]) < t) {
				if (metric.distance(q, data.get(this.value)) <= t) {
					res.add(data.get(this.value));
				}
			}

		}
	}

	/*
	 * so we leave the data list completely alone and manipulate only the
	 * references into it
	 */
	public SimplexRefPointTree(List<T> data, List<T> refPoints, Metric<T> metric) {
		super(data, metric);
		this.refPoints = refPoints;

		List<Integer> dataRefs = new ArrayList<>();
		this.refPointDists = new double[data.size()][refPoints.size()];
		for (int datPtr : Range.range(0, data.size())) {
			dataRefs.add(datPtr);
			for (int refPtr : Range.range(0, refPoints.size())) {
				this.refPointDists[datPtr][refPtr] = metric.distance(
						data.get(datPtr), refPoints.get(refPtr));
			}
		}

		this.root = new TreeNode(dataRefs, 0);

		this.simp = new NdimSimplex<>(this.metric, this.refPoints);
		for (int datPtr : Range.range(0, data.size())) {
			double[] ap = simp.getApex(this.refPointDists[datPtr]);
			this.refPointDists[datPtr] = ap;
		}
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		double[] pds = new double[this.refPoints.size()];
		for (int i : Range.range(0, pds.length)) {
			pds[i] = this.metric.distance(query, this.refPoints.get(i));
			if (pds[i] <= t) {
				res.add(this.refPoints.get(i));
			}
		}
		double[] apex = this.simp.getApex(pds);
		this.root.search(query, pds, apex, res, t);
		return res;
	}

	@Override
	public String getShortName() {
		return "srpt";
	}

}
