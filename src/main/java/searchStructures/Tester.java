package searchStructures;

import histogram.MetricHistogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import searchStructures.experimental.EuclideanCorners;
import searchStructures.experimental.LeanestTree;
import searchStructures.experimental.LeanestTreeFixedDepth;
import searchStructures.experimental.PermutationTree;
import testloads.CartesianThresholds;
import testloads.TestLoad;
import coreConcepts.DataSet;
import coreConcepts.DataSetImpl;
import coreConcepts.Metric;
import coreConcepts.MetricSpace;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannon;

public class Tester {
	/**
	 * @author newrichard We want the result to be something that is close to
	 *         being printed in a paper; so: metric name, dataset name,
	 *         thresholds, mean no of distance calcs, no of results as test
	 */
	public class TestResult {
		private String indexName;
		private String metricName;
		private String datasetName;
		private double[] thresholds;
		public double[] meanDists;
		public double[] stdErrMeanAsPctge;
		public int[] results;

		TestResult(String indexName, String metricName, String datasetName,
				double[] thresholds) {
			this.indexName = indexName;
			this.metricName = metricName;
			this.datasetName = datasetName;
			this.thresholds = thresholds;
			this.meanDists = new double[thresholds.length];
			this.stdErrMeanAsPctge = new double[thresholds.length];
			this.results = new int[thresholds.length];
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.indexName);
			sb.append("\t" + this.metricName);
			sb.append("\t" + this.datasetName);
			for (double t : this.thresholds) {
				sb.append("\t" + t);
			}
			for (int t : this.results) {
				sb.append("\t" + t);
			}
			for (double t : this.stdErrMeanAsPctge) {
				sb.append("\t" + t);
			}
			for (double t : this.meanDists) {
				sb.append("\t" + t);
			}
			return sb.toString();
		}

		public String getIndexName() {
			return this.indexName;
		}

	}

	public class PowerTestResult {
		public double cosExcs;
		public double hypExcs;
		public double pivExcs;
		public double cosExcsStd;
		public double hypExcsStd;
		public double pivExcsStd;
	}

	List<TestLoad> tls;
	private int dataSize;
	private int querySize;
	private int cartesianDim;
	private String dataSetName;

	/**
	 * @param cartesianDim
	 * @param dataSize
	 * @param querySize
	 * @param noOfTestSets
	 * @param gaussian
	 */
	@SuppressWarnings("boxing")
	public Tester(int cartesianDim, int dataSize, int querySize,
			int noOfTestSets, boolean gaussian) {
		this.dataSize = dataSize;
		this.querySize = querySize;
		this.cartesianDim = cartesianDim;
		this.dataSetName = "cart_" + cartesianDim + "_" + dataSize / 1000 + "k";
		if (gaussian) {
			this.dataSetName += "_gauss";
		}
		this.tls = new ArrayList<>();
		for (int i = 0; i < noOfTestSets; i++) {
			final TestLoad tl = new TestLoad(cartesianDim,
					dataSize + querySize, false, gaussian);
			tl.getQueries(querySize);
			this.tls.add(tl);
		}
	}

	public static SearchIndex<CartesianPoint> getSearchIndex(String it,
			List<CartesianPoint> data, int depth, Metric<CartesianPoint> wm) {
		switch (it) {

		case "leanest": {
			return new LeanestTreeFixedDepth<>(data, wm, depth, true);
		}
		default:
			throw new RuntimeException(it + ": no such tree can be constructed");
		}
	}

	public static SearchIndex<CartesianPoint> getSearchIndex(String it,
			List<CartesianPoint> data, Metric<CartesianPoint> wm) {
		switch (it) {
		case "bvpt": {
			return new VPTree<>(data, wm);
		}
		case "perm_h": {
			PermutationTree<?> t;
			return new PermutationTree<>(data, wm);
		}
		case "ght_v": {
			GHTree<CartesianPoint> ght = new GHTree<>(data, wm);
			ght.setCrExclusionEnabled(true);
			ght.setVorExclusionEnabled(true);
			return ght;
		}
		case "gmht_v": {
			GHMTree<CartesianPoint> ght = new GHMTree<>(data, wm);
			ght.setCrExclusionEnabled(true);
			ght.setVorExclusionEnabled(true);
			return ght;
		}
		case "gmht_h": {
			GHMTree<CartesianPoint> ght = new GHMTree<>(data, wm);
			ght.setCrExclusionEnabled(true);
			ght.setCosExclusionEnabled(true);
			return ght;
		}
		case "bghm_v": {
			BGHMTree<CartesianPoint> ght = new BGHMTree<>(data, wm, false);
			return ght;
		}
		case "bghm_h": {
			BGHMTree<CartesianPoint> ght = new BGHMTree<>(data, wm, true);
			return ght;
		}
		case "ght_h": {
			GHTree<CartesianPoint> ght = new GHTree<>(data, wm);
			ght.setCrExclusionEnabled(true);
			ght.setCosExclusionEnabled(true);
			return ght;
		}
		case "serial": {
			SerialSearch<CartesianPoint> ser = new SerialSearch<>(data, wm);
			return ser;
		}
		case "hqt": {
			HilbertQuadTree<CartesianPoint> ser = new HilbertQuadTree<>(data,
					wm);
			return ser;
		}
		case "sat_v": {
			SATGeneric<CartesianPoint> ser = new SATGeneric<>(data, wm,
					SATGeneric.Strategy.original);
			return ser;
		}
		case "sat_h": {
			SATGeneric<CartesianPoint> ser = new SATGeneric<>(data, wm,
					SATGeneric.Strategy.original);
			ser.setCosineTestEnabled(true);
			return ser;
		}
		case "leanest": {
			return new LeanestTree<>(data, wm, true);
		}
		case "temp1": {
			return new ListOfClusters<>(data, wm, 49);
		}
		case "temp2": {
			return new ListOfMonPartitions<>(data, wm, 49);
		}
		default:
			throw new RuntimeException(it + ": no such tree can be constructed");
		}
	}

	/**
	 * 
	 * For this metric and size of data: for each index, calculate a result
	 * 
	 * 
	 * @param metric
	 * @param dataSize
	 * @return
	 */
	public Map<String, TestResult> testAllIndices(Metric<CartesianPoint> metric) {
		Map<String, TestResult> res = new TreeMap<>();
		for (String indexName : IndexTypes) {
			res.put(indexName, testSingleIndex(indexName, metric));
		}
		return res;
	}

	public Map<String, TestResult> testSomeIndices(
			Metric<CartesianPoint> metric, String... indexNames) {
		Map<String, TestResult> res = new TreeMap<>();
		for (String indexName : indexNames) {
			res.put(indexName, testSingleIndex(indexName, metric));
		}
		return res;
	}

	public PowerTestResult testExclusionPower(Metric<CartesianPoint> metric,
			double threshold) {
		PowerTestResult res = new PowerTestResult();

		TestLoad t = this.tls.get(0);
		List<CartesianPoint> qs = t.getQueriesCopy();
		List<CartesianPoint> dat = t.getDataCopy();
		int iterations = qs.size() / 2;
		int[] hyps = new int[iterations];
		int[] pivs = new int[iterations];
		int[] coss = new int[iterations];

		for (int i = 0; i < iterations; i++) {
			CartesianPoint p1 = qs.get(i);
			CartesianPoint p2 = qs.get(i + 1);
			double[] p1Dists = new double[dat.size()];
			double[] p2Dists = new double[dat.size()];
			int ptr = 0;
			for (CartesianPoint p : dat) {
				p1Dists[ptr] = metric.distance(p, p1);
				p2Dists[ptr] = metric.distance(p, p2);
				ptr++;
			}
			double pivDist = metric.distance(p1, p2);
			hyps[i] = getHypPower(p1Dists, p2Dists, threshold);
			coss[i] = getCosPower(p1Dists, p2Dists, pivDist, threshold);
			pivs[i] = getPivPower(p1Dists, threshold);
		}
		res.cosExcs = getMean(coss);
		res.hypExcs = getMean(hyps);
		res.pivExcs = getMean(pivs);
		res.cosExcsStd = getStdDev(coss, res.cosExcs);
		res.hypExcsStd = getStdDev(hyps, res.hypExcs);
		res.pivExcsStd = getStdDev(pivs, res.pivExcs);

		return res;
	}

	private int getPivPower(double[] p1Dists, double threshold) {

		ObjectWithDistance[] diffs = new ObjectWithDistance[p1Dists.length];

		for (int ptr = 0; ptr < p1Dists.length; ptr++) {
			double pD = p1Dists[ptr];
			diffs[ptr] = new ObjectWithDistance<>(null, pD);
		}
		Quicksort.placeMedian(diffs);
		double medDiff = diffs[diffs.length / 2].getDistance();
		int projs = 0;
		for (ObjectWithDistance<?> od : diffs) {
			if (Math.abs(od.getDistance() - medDiff) > threshold) {
				projs++;
			}
		}

		return projs;
	}

	private int getCosPower(double[] p1Dists, double[] p2Dists, double pivDist,
			double threshold) {
		// ?without balancing?, how many points can act as exclusion queries?
		ObjectWithDistance[] projections = new ObjectWithDistance[p1Dists.length];

		for (int ptr = 0; ptr < p1Dists.length; ptr++) {
			double pD = SearchIndex.projectionDistance(pivDist, p1Dists[ptr],
					p2Dists[ptr]);
			projections[ptr] = new ObjectWithDistance<>(null, pD);
		}
		Quicksort.placeMedian(projections);
		double medProjDist = projections[projections.length / 2].getDistance();
		int projs = 0;
		for (ObjectWithDistance<?> od : projections) {
			if (Math.abs(od.getDistance() - medProjDist) > threshold) {
				projs++;
			}
		}

		return projs;
	}

	private int getHypPower(double[] p1Dists, double[] p2Dists, double threshold) {

		ObjectWithDistance[] diffs = new ObjectWithDistance[p1Dists.length];

		for (int ptr = 0; ptr < p1Dists.length; ptr++) {
			double pD = p1Dists[ptr] - p2Dists[ptr];
			diffs[ptr] = new ObjectWithDistance<>(null, pD);
		}
		Quicksort.placeMedian(diffs);
		double medDiff = diffs[diffs.length / 2].getDistance();
		int projs = 0;
		for (ObjectWithDistance<?> od : diffs) {
			if (Math.abs(od.getDistance() - medDiff) > 2 * threshold) {
				projs++;
			}
		}

		return projs;
	}

	/**
	 * 
	 * @param index
	 * @param metric
	 * @param dataSize
	 */
	public TestResult testSingleIndex(String index,
			Metric<CartesianPoint> metric) {
		double[] threshes = getThresholds(metric);
		TestResult res = new TestResult(index, metric.getMetricName(),
				this.dataSetName, threshes);

		int[] box = { 0 };
		Metric<CartesianPoint> m = getCountedMetric(metric, box);

		int[][] meanDists = new int[threshes.length][this.tls.size()];
		int tlPtr = 0;
		for (TestLoad t : this.tls) {
			List<CartesianPoint> qs = t.getQueriesCopy();
			List<CartesianPoint> dat = t.getDataCopy();
			SearchIndex<CartesianPoint> si;
			if (index.equals("leanest_30_h")) {
				si = getSearchIndex("leanest", dat, 28, m);
			} else {
				si = getSearchIndex(index, dat, m);
			}
			res.indexName = si.getShortName();

			int thrPtr = 0;
			for (double threshold : threshes) {
				box[0] = 0;
				int noOfRes = 0;
				for (CartesianPoint q : qs) {
					noOfRes += (si.thresholdSearch(q, threshold)).size();
				}
				res.results[thrPtr] += noOfRes;
				meanDists[thrPtr][tlPtr] = box[0];
				thrPtr++;
			}
			tlPtr++;
		}
		for (int th = 0; th < threshes.length; th++) {
			final double mean = getMean(meanDists[th]) / this.querySize;
			res.meanDists[th] = mean;
			res.stdErrMeanAsPctge[th] = getStdErr(meanDists[th], mean) / mean;
		}

		return res;
	}

	private double getStdErr(int[] is, double mean) {
		double acc = 0;
		for (int i : is) {
			double next = (double) i / querySize;
			double diff = next - mean;
			acc += diff * diff;
		}
		return Math.sqrt(acc / is.length) / Math.sqrt(is.length);
	}

	private static double getStdDev(int[] is, double mean) {
		double acc = 0;
		for (int i : is) {
			double diff = i - mean;
			acc += diff * diff;
		}
		return Math.sqrt(acc / is.length);
	}

	private static double getMean(int[] is) {
		double acc = 0;
		for (int i : is) {
			acc += i;
		}

		return (double) acc / is.length;
	}

	public double getIdim(Metric<CartesianPoint> metric) {
		List<CartesianPoint> x = this.tls.get(0).getDataCopy();
		DataSet<CartesianPoint> ds = new DataSetImpl<>(x, "list", true);
		MetricSpace<CartesianPoint> ms = new MetricSpace<>(ds, metric);

		MetricHistogram<CartesianPoint> h = new MetricHistogram<>(ms, 10000,
				1000, true, false, Math.sqrt(this.cartesianDim));
		MetricHistogram<CartesianPoint>.HistogramInfo inf = h
				.getHistogramInfo();
		return inf.idim;
	}

	@SuppressWarnings("boxing")
	public double[] getThresholds(Metric<CartesianPoint> metric) {
		double[] threshes = new double[CartesianThresholds.perMil.length];
		int ptr = 0;
		for (int ppm : CartesianThresholds.perMil) {
			threshes[ptr++] = CartesianThresholds.getThreshold(
					metric.getMetricName(), this.cartesianDim, ppm);
		}
		return threshes;
	}

	@SuppressWarnings("boxing")
	private static void test(TestLoad colors, double[] thresh) throws Exception {

		List<CartesianPoint> queries = colors
				.getQueries(colors.dataSize() / 20);
		List<CartesianPoint> testData = colors.getDataCopy();
		System.out.println("data size: " + testData.size() + "; query size: "
				+ queries.size());

		Metric<CartesianPoint> jsd = new JensenShannon<>(true, true);
		final Metric<CartesianPoint> euc = new Euclidean<>();
		final Metric<CartesianPoint> metric = euc;

		final int[] box = { 0 };
		Metric<CartesianPoint> wm = getCountedMetric(metric, box);

		EuclideanCorners cc = new EuclideanCorners(testData);

		final String indexType = "gmht_h";
		SearchIndex<CartesianPoint> tree = getSearchIndex(indexType, testData,
				wm);
		// LeanestTree<CartesianPoint> actual = (LeanestTree<CartesianPoint>)
		// tree;
		// System.out.println("max depth is " + actual.maxDepth);

		System.out.println(box[0] + " distances to build tree");

		System.out
				.println("structure\tmetric\tthreshold\tdistances\ttime\tresults");
		for (double t : thresh) {
			long t0 = System.currentTimeMillis();

			box[0] = 0;
			int res = 0;
			for (CartesianPoint q : queries) {
				res += tree.thresholdSearch(q, t).size();
			}

			long t1 = System.currentTimeMillis() - t0;

			System.out.println(indexType + "\t" + metric.getMetricName() + "\t"
					+ t + "\t" + (double) box[0] / queries.size() + "\t" + t1
					+ "\t" + res);
			// System.out.println((double) box[0] / queries.size()
			// + " distances to execute query (" + res + ")");
			// System.out.println(t1 + " msecs to execute query");
		}
	}

	protected static Metric<CartesianPoint> getCountedMetric(
			final Metric<CartesianPoint> metric, final int[] box) {
		Metric<CartesianPoint> wm = new Metric<CartesianPoint>() {

			@Override
			public double distance(CartesianPoint x, CartesianPoint y) {
				box[0]++;
				return metric.distance(x, y);
			}

			@Override
			public String getMetricName() {
				return "temp";
			}
		};
		return wm;
	}

	public static String[] IndexTypes = { "ght_h", "ght_v", "perm_h", "bvpt",
			"gmht_h", "gmht_v", "bghm_v", "bghm_h", "leanest", "hqt", "sat_v",
			"sat_h" };

	public static void main(String[] a) {
		Tester t = new Tester(6, 100000, 10, 1, false);
		final Euclidean euc = new Euclidean();
		TestResult r = t.testSingleIndex("leanest", euc);
		for (int i : r.results) {
			System.out.println(i);
		}
		r = t.testSingleIndex("leanest_30_h", euc);
		for (int i : r.results) {
			System.out.println(i);
		}
	}
}
