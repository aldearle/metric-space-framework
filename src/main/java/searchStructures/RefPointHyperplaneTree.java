package searchStructures;

import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannon;
import dataPoints.cartesian.SEDByComplexity;
import searchStructures.experimental.PivotFinder;
import searchStructures.experimental.PreCalcDists;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;

import java.util.ArrayList;
import java.util.List;

public class RefPointHyperplaneTree<T> extends SearchIndex<T> {

	private class DataNode {
		T value;
		double[] leafDists;
		double[][] refPointPlots;

		DataNode(T val) {
			this.value = val;
		}
	}

	private class TreeNode {
		TreeNode left, right;
		DataNode leftData, rightData;
		double medianOffset;
		double lMinFromR;
		double rMinFromL;
		double lCr;
		double rCr;
		double lMinRad;
		double rMinRad;

		/**
		 * 
		 * to and from are inclusive bounds for the area of the dataPoint vector
		 * this node covers in its subnodes
		 * 
		 * @param depth
		 * @param from
		 * @param to
		 */
		TreeNode(int depth, int from, int to) {
			T p1 = referencePoints.get(depth);
			T p2 = referencePoints.get(depth + 1);
			double refPointDist = refPointDists[depth][depth + 1];

			setOffset(from, to, p1, p2, refPointDist);

			// middle of odd-sized, first on rhs for even sized
			int medianPos = from + ((to - from) + 1) / 2;
			Quicksort.partitionToPivotPoint(dataPoints, from, to, medianPos);
			this.medianOffset = dataPoints[medianPos].getDistance();
			this.lMinFromR = Double.MAX_VALUE;
			this.lCr = 0;
			this.lMinRad = Double.MAX_VALUE;
			for (int p = from; p < medianPos; p++) {
				double dFromL = metric.distance(dataPoints[p].getValue(), p1);
				double dFromR = metric.distance(dataPoints[p].getValue(), p2);
				this.lMinFromR = Math.min(dFromR, lMinFromR);
				this.lCr = Math.max(lCr, dFromL);
				this.lMinRad = Math.min(lMinRad, dFromL);
			}
			this.rMinFromL = Double.MAX_VALUE;
			this.rCr = 0;
			this.rMinRad = Double.MAX_VALUE;
			for (int p = medianPos; p <= to; p++) {
				double dFromL = metric.distance(dataPoints[p].getValue(), p1);
				double dFromR = metric.distance(dataPoints[p].getValue(), p2);
				this.rMinFromL = Math.min(dFromL, rMinFromL);
				rCr = Math.max(rCr, dFromR);
				this.rMinRad = Math.min(this.rMinRad, dFromR);
			}

			if (medianPos - from == 1) {
				final T value = dataPoints[from].getValue();
				this.leftData = new DataNode(value);
				this.leftData.leafDists = new double[referencePoints.size()];
				for (int i = 0; i < refPointDists.length; i++) {
					this.leftData.leafDists[i] = metric.distance(value,
							referencePoints.get(i));
				}
				// if (hilbertMetric) {
				this.leftData.refPointPlots = getRefPointPlots(this.leftData.leafDists);
				// }
			} else {
				this.left = new TreeNode(depth + 1, from, medianPos - 1);
			}

			if (medianPos == to) {
				final T value = dataPoints[to].getValue();
				this.rightData = new DataNode(value);
				this.rightData.leafDists = new double[referencePoints.size()];
				for (int i = 0; i < refPointDists.length; i++) {
					this.rightData.leafDists[i] = metric.distance(value,
							referencePoints.get(i));
				}
				// if (hilbertMetric) {
				this.rightData.refPointPlots = getRefPointPlots(this.rightData.leafDists);
				// }
			} else {
				this.right = new TreeNode(depth + 1, medianPos, to);
			}
		}

		@SuppressWarnings("synthetic-access")
		protected void setOffset(int from, int to, T p1, T p2,
				double refPointDist) {
			if (RefPointHyperplaneTree.this.hilbertMetric) {
				setHilbertOffset(from, to, p1, p2, refPointDist);
			} else {
				setHyperbolicOffset(from, to, p1, p2, refPointDist);
			}
		}

		private void setHyperbolicOffset(int from, int to, T p1, T p2,
				double refPointDist) {
			for (int i = from; i <= to; i++) {
				ObjectWithDistance<T> owd = dataPoints[i];
				final T s = owd.getValue();
				double d1 = metric.distance(s, p1);
				double d2 = metric.distance(s, p2);
				double diff = d1 - d2;
				owd.setDistance(diff);
			}
		}

		protected void setHilbertOffset(int from, int to, T p1, T p2,
				double refPointDist) {
			for (int i = from; i <= to; i++) {
				ObjectWithDistance<T> owd = dataPoints[i];
				final T s = owd.getValue();
				double d1 = metric.distance(s, p1);
				double d2 = metric.distance(s, p2);
				/*
				 * set either by projection distance or by altitude
				 */
				double proj = SearchIndex.projectionDistance(refPointDist, d1,
						d2);
				double alt = SearchIndex.altitude(proj, d1);
				owd.setDistance(proj);
			}
		}

		public void search(int depth, T query, double t, List<T> res) {
			double d1 = refPointDistsPerQuery[depth];
			double d2 = refPointDistsPerQuery[depth + 1];
			double refPointDist = refPointDists[depth][depth + 1];
			double proj = SearchIndex.projectionDistance(refPointDist, d1, d2);
			double alt = SearchIndex.altitude(proj, d1);

			/*
			 * to be on the left, the projection distance is small so if the
			 * query projection distance is large and the difference is greater
			 * than the threshold, we don't need to check
			 * 
			 * also, if the query point is much closer to the right pivot than
			 * the minimum of the left hand set we can exclude
			 * 
			 * also, if the query point is much further than the left cover
			 * radius...
			 * 
			 * finally, also have an anti-cover radius so if query is much
			 * closer than that...
			 */
			if (!(hilbertMetric && alt - this.medianOffset > t)
					|| !(!hilbertMetric && (d1 - d2 > medianOffset + 2 * t))) {
				if (!(checkCoverRadii && d2 < this.lMinFromR - t)
						&& !(d1 > this.lCr + t) && !(d1 < this.lMinRad - t)) {
					if (this.leftData != null) {
						boolean canBeSolution = true;
						if (recheckThreePoint) {
							for (int i = 0; i < this.leftData.leafDists.length; i++) {
								if (Math.abs(this.leftData.leafDists[i]
										- refPointDistsPerQuery[i]) > t) {
									canBeSolution = false;
								}
							}
						}
						if (canBeSolution && recheckFourPoint) {
							for (int i = 0; i < refPointPlotsPerQuery.length; i++) {
								if (canBeSolution) {
									double[] thisPlot = this.leftData.refPointPlots[i];
									double[] refPlot = refPointPlotsPerQuery[i];
									double xDif = thisPlot[0] - refPlot[0];
									double yDif = thisPlot[1] - refPlot[1];
									double d = Math.sqrt(xDif * xDif + yDif
											* yDif);
									if (d > t) {
										canBeSolution = false;
									}
								}
							}
						}
						if (canBeSolution
								&& metric.distance(this.leftData.value, query) <= t) {
							res.add(this.leftData.value);
						}
					} else {
						this.left.search(depth + 1, query, t, res);
					}
				}
			}
			/*
			 * to be on the right, the projection distance is big so if the
			 * query projection distance is small and the difference is greater
			 * than the threshold, we don't need to check
			 */
			if (!(hilbertMetric && medianOffset - alt > t)
					|| !(!hilbertMetric && (d1 - d2 < medianOffset - 2 * t))) {
				if (!(checkCoverRadii && d1 < this.rMinFromL - t)
						&& !(d2 > this.rCr + t) && !(d2 < this.rMinRad - t)) {
					if (this.rightData != null) {

						boolean canBeSolution = true;
						if (recheckThreePoint) {
							for (int i = 0; i < this.rightData.leafDists.length; i++) {
								if (Math.abs(this.rightData.leafDists[i]
										- refPointDistsPerQuery[i]) > t) {
									canBeSolution = false;
								}
							}
						}
						if (canBeSolution && recheckFourPoint) {
							for (int i = 0; i < refPointPlotsPerQuery.length; i++) {
								if (canBeSolution) {
									double[] thisPlot = this.rightData.refPointPlots[i];
									double[] refPlot = refPointPlotsPerQuery[i];
									double xDif = thisPlot[0] - refPlot[0];
									double yDif = thisPlot[1] - refPlot[1];
									double d = Math.sqrt(xDif * xDif + yDif
											* yDif);
									if (d > t) {
										canBeSolution = false;
									}
								}
							}
						}
						if (canBeSolution
								&& metric.distance(this.rightData.value, query) <= t) {
							res.add(this.rightData.value);
						}
					} else {
						this.right.search(depth + 1, query, t, res);
					}
				}
			}
		}
	}

	private boolean hilbertMetric;
	private boolean recheckThreePoint;
	private boolean recheckFourPoint;
	private boolean checkCoverRadii;
	private TreeNode root;
	private List<T> referencePoints;
	private double[][] refPointDists;
	private ObjectWithDistance<T>[] dataPoints;
	private double[] refPointDistsPerQuery;
	private double[][] refPointPlotsPerQuery;
	private int fourPointRecheckLimit;

	public RefPointHyperplaneTree(List<T> data, Metric<T> metric,
			boolean hilbertMetric) {
		super(data, metric);
		this.hilbertMetric = hilbertMetric;
		this.setRecheckThreePoint(false);
		this.setRecheckFourPoint(false);
		this.setCheckCoverRadii(false);

		int leafDepth = getLeafDepth(data.size());
		this.referencePoints = new ArrayList<>();

		List<Integer> refs = PivotFinder.getPivotIndices(data, metric,
				leafDepth + 2);

		for (int i = refs.size() - 1; i >= 0; i--) {
			int r = refs.get(i);
			this.referencePoints.add(data.remove(r));
		}

		initRefPointDists();

		this.dataPoints = new ObjectWithDistance[data.size()];
		int ptr = 0;
		for (T dat : data) {
			this.dataPoints[ptr++] = new ObjectWithDistance<>(dat, 0);
		}

		populate();
	}

	protected void initRefPointDists() {
		this.refPointDists = new double[referencePoints.size()][referencePoints
				.size()];
		for (int i = 0; i < referencePoints.size() - 1; i++) {
			for (int j = i + 1; j < referencePoints.size(); j++) {
				final double dist = this.metric.distance(
						referencePoints.get(i), referencePoints.get(j));
				// only ever accessed from small to large...
				refPointDists[i][j] = dist;
			}
		}
	}

	protected double[][] getRefPointPlots(double[] distsToRefPoints) {

		double[][] res = new double[(distsToRefPoints.length * (distsToRefPoints.length - 1)) / 2][2];
		int ptr = 0;
		for (int i = 0; i < distsToRefPoints.length - 1; i++) {
			for (int j = i + 1; j < distsToRefPoints.length; j++) {
				double ab = this.refPointDists[i][j];
				double aq = distsToRefPoints[i];
				double bq = distsToRefPoints[j];
				double projD = projectionDistance(ab, aq, bq);
				double alt = altitude(projD, aq);
				double[] p = { projD, alt };
				res[ptr++] = p;
			}
		}
		return res;
	}

	private void populate() {
		this.root = new TreeNode(0, 0, this.dataPoints.length - 1);
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		this.refPointDistsPerQuery = new double[this.referencePoints.size()];
		List<T> res = new ArrayList<>();

		int ptr = 0;
		for (T ref : this.referencePoints) {
			final double distance = this.metric.distance(query, ref);
			this.refPointDistsPerQuery[ptr++] = distance;
			if (distance <= t) {
				res.add(ref);
			}
		}

		// if (hilbertMetric) {
		this.refPointPlotsPerQuery = getRefPointPlots(this.refPointDistsPerQuery);
		this.fourPointRecheckLimit = this.refPointPlotsPerQuery.length;
		// }

		this.root.search(0, query, t, res);
		return res;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	private static int getLeafDepth(int size) {
		int attempt = (int) Math.ceil(Math.log(size) / Math.log(2));
		int residualSize = size - (attempt + 2);
		int attempt2 = (int) Math.ceil(Math.log(residualSize) / Math.log(2));
		if (attempt != attempt2) {
			throw new RuntimeException(
					"oh bugger not sure how deep this should be");
		}
		return attempt;
	}

	protected static void test() throws Exception {
		final SisapFile sisapFile = TestLoad.SisapFile.colors;
		TestLoad tl = new TestLoad(sisapFile);
		double[] thresholds = TestLoad.getSisapThresholds(sisapFile);

		List<CartesianPoint> qs = tl.getQueries(tl.dataSize() / 10);
		System.out.println("data size is " + tl.dataSize());
		System.out.println("there are " + qs.size() + " queries");
		Metric<CartesianPoint> euc = new Euclidean<>();
		Metric<CartesianPoint> jsd = new JensenShannon<CartesianPoint>(false,
				false);
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(jsd);

		VPTree<CartesianPoint> testTree = new VPTree<>(tl.getDataCopy(), cm);
		// testTree.setVorExclusionEnabled(true);
		// testTree.setCrExclusionEnabled(true);

		// monotonicTree.setCosExclusionEnabled(true);
		// monotonicTree.setCrExclusionEnabled(true);
		RefPointHyperplaneTree<CartesianPoint> veryLeanTree = new RefPointHyperplaneTree<>(
				tl.getDataCopy(), cm, false);
		veryLeanTree.setCheckCoverRadii(true);
		veryLeanTree.setRecheckThreePoint(false);
		veryLeanTree.setRecheckFourPoint(false);
		cm.reset();

		for (double threshold : thresholds) {
			System.out.println("threshold: " + threshold);
			int tot1 = 0;
			int tot2 = 0;
			int dists1 = 0;
			int dists2 = 0;
			long time0 = System.currentTimeMillis();
			for (CartesianPoint query : qs) {
				tot2 += veryLeanTree.thresholdSearch(query, threshold).size();
				dists2 += cm.reset();
			}
			long time1 = System.currentTimeMillis();
			for (CartesianPoint query : qs) {
				tot1 += testTree.thresholdSearch(query, threshold).size();
				dists1 += cm.reset();
			}
			long time2 = System.currentTimeMillis();
			System.out.println(tot1 + "; " + tot2 + " (" + (dists1 / qs.size())
					+ "; " + (dists2 / qs.size()) + ")" + "; "
					+ (time2 - time1) + ":" + (time1 - time0));
		}
	}

	public static void main(String[] a) throws Exception {
		testPreCalcs();
		// test();
	}

	private static void testPreCalcs() throws Exception {
		final SisapFile sisapFile = TestLoad.SisapFile.colors;
		TestLoad tl = new TestLoad(sisapFile);
		double[] thresholds = TestLoad.getSisapThresholds(sisapFile);
		final double threshold = thresholds[2];

		System.out.println("data size is " + tl.dataSize());

		List<CartesianPoint> qs = tl.getQueries(tl.dataSize() / 10);
		List<CartesianPoint> dat = tl.getDataCopy();

		System.out.println(qs.get(0).getPoint().length);

		System.out.println("there are " + qs.size() + " queries");
		System.out.println("and " + dat.size() + " residual data");
		Metric<CartesianPoint> euc = new Euclidean<>();
		Metric<CartesianPoint> jsd = new JensenShannon<CartesianPoint>(false,
				false);
		final Metric<CartesianPoint> sed = new SEDByComplexity<CartesianPoint>();

		int depth = RefPointHyperplaneTree.getLeafDepth(dat.size());

		CountedMetric<CartesianPoint> cm = new CountedMetric<>(euc);

		System.out.println("method\tresults\tdistances\ttime");
		long t0 = System.currentTimeMillis();

		PreCalcDists<CartesianPoint> pcd = new PreCalcDists<CartesianPoint>(dat, cm,
		depth + 2);
		int qOffset = pcd.addQueries(qs);
		final Metric<Integer> pseudoMetric = pcd.pseudoMetric();

		System.out.println("precalc" + "\t" + 0 + "\t" + cm.reset() + "\t"
				+ (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();

		int totalRes = 0;
		printLaesaResults(threshold, qs, cm, t0, pcd, qOffset, pseudoMetric,
				totalRes);

		printSurrogateResults(threshold, qs, cm, t0, pcd, qOffset,
				pseudoMetric, false);

		printSurrogateResults(threshold, qs, cm, t0, pcd, qOffset,
				pseudoMetric, true);

		doVVLTree(threshold, dat.size(), qs, cm, t0, qOffset, pseudoMetric,
				false);

		doVVLTree(threshold, dat.size(), qs, cm, t0, qOffset, pseudoMetric,
				true);
	}

	protected static void printSurrogateResults(final double threshold,
			List<CartesianPoint> qs, CountedMetric<CartesianPoint> cm, long t0,
			PreCalcDists pcd, int qOffset, final Metric<Integer> pseudoMetric,
			boolean fourPoint) {
		int totalRes = 0;
		pcd.setQueryDists();
		SearchIndex<Integer> sur;
		if (fourPoint) {
			sur = pcd.surrogateSearchIndex();
		} else {
			sur = pcd.surrogateSearchIndex4P();
		}
		totalRes = 0;
		for (int i = qOffset; i < qOffset + qs.size(); i++) {
			List<Integer> res = sur.thresholdSearch(i, threshold);
			for (int r : res) {
				if (pseudoMetric.distance(i, r) <= threshold) {
					totalRes++;
				}
			}
		}

		System.out.println("surrogate" + "\t" + totalRes + "\t" + cm.reset()
				/ qs.size() + "\t" + (System.currentTimeMillis() - t0));

	}

	protected static void printLaesaResults(final double threshold,
			List<CartesianPoint> qs, CountedMetric<CartesianPoint> cm, long t0,
			PreCalcDists pcd, int qOffset, final Metric<Integer> pseudoMetric,
			int totalRes) {
		for (int i = qOffset; i < qOffset + qs.size(); i++) {
			List<Integer> res = pcd.laesaSearch(i, threshold);
			for (int r : res) {
				if (pseudoMetric.distance(i, r) <= threshold) {
					totalRes++;
				}
			}
		}

		System.out.println("laesa" + "\t" + totalRes + "\t" + cm.reset()
				/ qs.size() + "\t" + (System.currentTimeMillis() - t0));

	}

	protected static void doVVLTree(final double threshold, int dataSize,
			List<CartesianPoint> qs, CountedMetric<CartesianPoint> cm, long t0,
			int qOffset, final Metric<Integer> pseudoMetric,
			boolean fourPointTree) {

		List<Integer> ints = new ArrayList<Integer>();
		for (int i = 0; i < dataSize; i++) {
			ints.add(i);
		}

		RefPointHyperplaneTree<Integer> t = new RefPointHyperplaneTree(ints,
				pseudoMetric, fourPointTree);
		t.setCheckCoverRadii(true);
		t.setRecheckThreePoint(false);
		t.setRecheckFourPoint(false);
		// VPTree<Integer> t = new VPTree(ints, pcd.pseudoMetric());

		System.out.println("VVL build" + "\t" + 0 + "\t" + cm.reset() + "\t"
				+ (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();

		int results = 0;
		for (int i = qOffset; i < qOffset + qs.size(); i++) {
			List<Integer> res = t.thresholdSearch(i, threshold);
			results += res.size();
		}

		System.out.println("VVL" + "\t" + results + "\t" + cm.reset()
				/ qs.size() + "\t" + (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();

		t.setRecheckThreePoint(true);
		t.setRecheckFourPoint(false);
		results = 0;
		for (int i = qOffset; i < qOffset + qs.size(); i++) {
			List<Integer> res = t.thresholdSearch(i, threshold);
			results += res.size();
		}

		System.out.println("VVL-3p" + "\t" + results + "\t" + cm.reset()
				/ qs.size() + "\t" + (System.currentTimeMillis() - t0));
	}

	public void setCheckCoverRadii(boolean checkCoverRadii) {
		this.checkCoverRadii = checkCoverRadii;
	}

	public void setRecheckThreePoint(boolean recheckThreePoint) {
		this.recheckThreePoint = recheckThreePoint;
	}

	public void setRecheckFourPoint(boolean recheckFourPoint) {
		this.recheckFourPoint = recheckFourPoint;
		// if (recheckFourPoint && !this.hilbertMetric) {
		// this.recheckFourPoint = false;
		// }
	}
}
