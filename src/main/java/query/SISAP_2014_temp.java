package query;

import java.util.Date;
import java.util.List;

import util.Util;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.LebesqueInf;
import dataPoints.cartesian.SEDByComplexity;
import dataSets.fileReaders.CartesianPointFileReader;

public class SISAP_2014_temp {

	private static CartesianPointFileReader getGistPointFile(boolean full,
			int fileNumber, int howManyPoints) throws Exception {

		String f = "";
		if (full) {
			f = "_full";
		}

		String dataMF = "/Users/newrichard/Documents/Research/collated SISAP research/MIR-flickr/data/mir flickr 1M";
		String dataFile = dataMF + "/gist_descriptors" + f + "/gist"
				+ fileNumber + ".txt";
		CartesianPointFileReader p = new CartesianPointFileReader(dataFile,
				false, howManyPoints);
		return p;
	}

	protected static CartesianPointFileReader getPivotIndexFile(
			int queryFileNo, int noOfPoints) throws Exception {
		CartesianPointFileReader queryData = new CartesianPointFileReader(
				getEpiFilename(queryFileNo), false, noOfPoints);
		return queryData;
	}

	private static String getEpiFilename(int queryFileNo) {
		String dataMF = "/Users/newrichard/Documents/Research/collated SISAP research/MIR-flickr/data/mir flickr 1M";
		return dataMF + "/gist_reference_points/euc_100_largest_total/"
				+ queryFileNo + ".epi";
	}

	public static void main(String[] args) throws Exception {
		final Metric<CartesianPoint> sed = new SEDByComplexity<CartesianPoint>();
		Metric<CartesianPoint> euc = new Euclidean<CartesianPoint>();
		Metric<CartesianPoint> leb = new LebesqueInf<CartesianPoint>();
		Metric<CartesianPoint> jsd = new Metric<CartesianPoint>() {

			@Override
			public double distance(CartesianPoint x, CartesianPoint y) {
				double d = sed.distance(x, y);
				return Util.sedToJs(d);
			}

			@Override
			public String getMetricName() {
				return "jsd";
			}
		};

		List<CartesianPoint> data = getGistPointFile(true, 1, 10000);
		List<CartesianPoint> indexData = getPivotIndexFile(1, 10000);

		// testMetric(jsd, data, Util.sedToJs(0.02));
		// testMetric(euc, data, 0.05);
		// testMetric(leb, indexData, 0.05);
		testMetric(leb, indexData, 0.003);
	}

	private static void testMetric(Metric<CartesianPoint> metric,
			List<CartesianPoint> data, double threshold) throws Exception {

		BalancedVPTree<CartesianPoint> vpt = new BalancedVPTree<CartesianPoint>(
				metric, data);

		long start = (new Date()).getTime();
		int totalCalcs = 0;
		for (CartesianPoint q : data) {
			List<CartesianPoint> f = vpt.thresholdQuery(q, threshold);
			totalCalcs += vpt.getLastQueryDists();
			for (CartesianPoint p : f) {
				if (p != q) {
					System.out.println("found: " + data.indexOf(q) + ":"
							+ data.indexOf(p));
				}
			}
		}
		System.out.print("100,000,000 " + metric.getMetricName()
				+ " queries took " + ((new Date()).getTime() - start) + " ms");
		System.out.println(", and " + totalCalcs + " calculations");
	}
}
