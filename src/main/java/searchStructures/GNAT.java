package searchStructures;

import java.util.ArrayList;
import java.util.List;

import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

/**
 * @author newrichard
 *
 * @param <T>
 * 
 *            implements the GNAT metric index, doesn't seem that exciting...
 */
public class GNAT<T> extends SearchIndex<T> {

	private GNATnode head;
	private boolean minExclusionEnabled;
	private boolean maxExclusionEnabled;

	protected class GNATnode {
		boolean isLeaf;
		List<T> leafData;
		List<T> pivots;
		List<GNATnode> subTrees;
		double[][] minDists;
		double[][] maxDists;

		protected GNATnode(List<T> data) {
			int arity = (int) Math.round(Math.log(data.size()));
			if (arity < 3) {
				arity = 3;
			}
			if (data.size() <= arity) {
				this.isLeaf = true;
				this.leafData = data;
			} else {
				this.minDists = new double[arity][arity];
				this.maxDists = new double[arity][arity];
				for (int x = 0; x < arity; x++) {
					for (int y = 0; y < arity; y++) {
						this.minDists[x][y] = Double.MAX_VALUE;
					}
				}
				this.pivots = new ArrayList<>();
				for (int i = 0; i < arity; i++) {
					this.pivots.add(data.get(0));
					data.remove(0);
				}
				List<List<T>> sublists = new ArrayList<>();
				for (T piv : this.pivots) {
					sublists.add(new ArrayList<T>());
				}
				for (T d : data) {
					double smallestDistance = Double.MAX_VALUE;
					int closestPivot = -1;
					double[] dists = new double[this.pivots.size()];
					for (int ptr = 0; ptr < this.pivots.size(); ptr++) {
						T piv = this.pivots.get(ptr);
						double thisDist = GNAT.this.metric.distance(d, piv);
						dists[ptr] = thisDist;
						if (thisDist < smallestDistance) {
							smallestDistance = thisDist;
							closestPivot = ptr;
						}
					}
					sublists.get(closestPivot).add(d);
					for (int ptr = 0; ptr < dists.length; ptr++) {
						this.minDists[ptr][closestPivot] = Math.min(
								this.minDists[ptr][closestPivot], dists[ptr]);
						this.maxDists[ptr][closestPivot] = Math.max(
								this.maxDists[ptr][closestPivot], dists[ptr]);
					}
				}
				this.subTrees = new ArrayList<>();
				for (List<T> sublist : sublists) {
					this.subTrees.add(new GNATnode(sublist));
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		protected void thresholdSearch(List<T> res, T query, double threshold) {
			if (this.isLeaf) {
				for (T dat : this.leafData) {
					if (GNAT.this.metric.distance(query, dat) <= threshold) {
						res.add(dat);
					}
				}
			} else {
				boolean[] mustSearch = new boolean[this.pivots.size()];
				for (int i = 0; i < mustSearch.length; i++) {
					mustSearch[i] = true;
				}
				for (int i = 0; i < this.pivots.size(); i++) {
					T piv = this.pivots.get(i);
					final double qDist = GNAT.this.metric.distance(query, piv);
					if (qDist <= threshold) {
						res.add(piv);
					}
					for (int j = 0; j < this.pivots.size(); j++) {
						if (GNAT.this.minExclusionEnabled
								&& qDist + threshold < this.minDists[i][j]) {
							mustSearch[j] = false;
						}
						if (GNAT.this.maxExclusionEnabled
								&& qDist - threshold > this.maxDists[i][j]) {
							mustSearch[j] = false;
						}
					}
				}
				for (int i = 0; i < this.subTrees.size(); i++) {
					if (mustSearch[i]) {
						GNATnode gn = this.subTrees.get(i);
						gn.thresholdSearch(res, query, threshold);

					}
				}
			}
		}

		protected int size() {
			if (this.isLeaf) {
				return this.leafData.size();
			} else {
				int res = this.pivots.size();
				for (GNATnode gn : this.subTrees) {
					res += gn.size();
				}
				return res;
			}
		}
	}

	public GNAT(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.minExclusionEnabled = false;
		this.maxExclusionEnabled = false;
		this.head = new GNATnode(data);
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		this.head.thresholdSearch(res, query, t);
		return res;
	}

	public static void main(String[] a) throws Exception {
		final SisapFile sisapFile = TestLoad.SisapFile.colors;
		double[] ts = TestLoad.getSisapThresholds(sisapFile);
		final double threshold = ts[2];

		final TestLoad testLoad = new TestLoad(sisapFile);
		List<CartesianPoint> queries = testLoad
				.getQueries(testLoad.dataSize() / 5);

		CartesianPoint queryRand = queries.get(38);

		List<CartesianPoint> tl = testLoad.getDataCopy();
		System.out.println("data size: " + tl.size());
		Metric<CartesianPoint> euc = new Euclidean<>();
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(euc);
		GNAT<CartesianPoint> gn = new GNAT<>(tl, cm);

		System.out.println("done GNAT: " + gn.head.size() + "; " + cm.reset()
				+ "; " + cm.reset());

		List<CartesianPoint> res = gn.thresholdSearch(queryRand, threshold);
		System.out.println(res.size() + ":" + cm.reset());

		gn.setMinExclusionEnabled(true);

		List<CartesianPoint> res2 = gn.thresholdSearch(queryRand, threshold);
		System.out.println(res2.size() + ":" + cm.reset());

		gn.setMinExclusionEnabled(false);
		gn.setMaxExclusionEnabled(true);

		List<CartesianPoint> res3 = gn.thresholdSearch(queryRand, threshold);
		System.out.println(res3.size() + ":" + cm.reset());

		gn.setMinExclusionEnabled(true);
		gn.setMaxExclusionEnabled(true);

		List<CartesianPoint> res4 = gn.thresholdSearch(queryRand, threshold);
		System.out.println(res4.size() + ":" + cm.reset());

		VPTree<CartesianPoint> vpt = new VPTree<>(testLoad.getDataCopy(), cm);
		cm.reset();
		List<CartesianPoint> resV = vpt.thresholdSearch(queryRand, threshold);
		System.out.println(resV.size() + ":" + cm.reset());

	}

	public void setMinExclusionEnabled(boolean minExclusionEnabled) {
		this.minExclusionEnabled = minExclusionEnabled;
	}

	public void setMaxExclusionEnabled(boolean maxExclusionEnabled) {
		this.maxExclusionEnabled = maxExclusionEnabled;
	}

	@Override
	public String getShortName() {
		return "gnat";
	}
}
