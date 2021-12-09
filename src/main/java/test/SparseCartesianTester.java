package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import semanticDataTypes.StringShingle;
import semanticDataTypes.StringShingle.ShingleType;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannonViaSed;
import dataPoints.compactEnsemble.EventToIntegerMap;
import dataPoints.sparseCartesian.InvertedIndex;
import dataPoints.sparseCartesian.InvertedIndexDef2;
import dataPoints.sparseCartesian.InvertedIndexDef3;
import dataPoints.sparseCartesian.InvertedIndexDef3Analytic;
import dataPoints.sparseCartesian.JensenShannonDef1;
import dataPoints.sparseCartesian.JensenShannonDef2;
import dataPoints.sparseCartesian.JensenShannonDef3;
import dataPoints.sparseCartesian.SparseCartesianPoint;

public class SparseCartesianTester {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		try {
			assert false;
			System.out.println("assertions are not enabled");
		} catch (AssertionError e) {
			System.out.println("assertions enabled");
		}

		try {
			double[] p1 = { 0, 1, 0, 0 };
			double[] p2 = { 0, 1, 0, 1 };
			basicVectorTests(p1, p2);
			invertedIndexTests(p1, p2);
			testStringShingleToSparseCartesian();
		} catch (AssertionError e) {
			System.out.println("assertion failed: " + e.getMessage());
		}

	}

	private static void invertedIndexTests(double[] p1, double[] p2) {

		final CartesianPoint cp1 = new CartesianPoint(p1);
		SparseCartesianPoint sp1 = new SparseCartesianPoint(cp1);
		final CartesianPoint cp2 = new CartesianPoint(p2);
		SparseCartesianPoint sp2 = new SparseCartesianPoint(cp2);

		List<SparseCartesianPoint> l = new ArrayList<SparseCartesianPoint>();
		l.add(sp1);
		l.add(sp2);

		InvertedIndex<SparseCartesianPoint> ii = new InvertedIndexDef2<SparseCartesianPoint>(
				l);

		double dist = (new JensenShannonDef2<SparseCartesianPoint>().distance(
				sp1, sp2));
		assert ii.thresholdQuery(sp1, dist + 0.00001).size() == 2 : "bad result from ii threshold query";
		assert ii.thresholdQuery(sp1, dist - 0.00001).size() == 1 : "bad result from ii threshold query";

		try {
			ii.writeVerboseIndexFile("testdata/invInd/testIIfile.csv");
		} catch (FileNotFoundException e) {
			assert false : "failed to write index file";
		}

		try {
			InvertedIndex<SparseCartesianPoint> ii2 = new InvertedIndexDef3<SparseCartesianPoint>(
					new File("testdata/invInd/testIIfile.csv"));

			assert ii.toString().equals(ii2.toString()) : "different string reps of same ii form";

			assert ii2.thresholdQuery(sp1, dist + 0.00001).size() == 2 : "bad result from ii2 threshold query";
			assert ii2.thresholdQuery(sp1, dist - 0.00001).size() == 1 : "bad result from ii2 threshold query";
		} catch (FileNotFoundException e) {
			assert false : "failed to read inverted index from file";
		} catch (IOException e) {
			assert false : "I/O failed reading inverted index from file";
		}

		try {
			InvertedIndex<SparseCartesianPoint> ii2 = new InvertedIndexDef3Analytic<SparseCartesianPoint>(
					new File("testdata/invInd/testIIfile.csv"));

			assert ii.toString().equals(ii2.toString()) : "different string reps of same ii form";

			assert ii2.thresholdQuery(sp1, dist + 0.00001).size() == 2 : "bad result from ii2 threshold query";
			assert ii2.thresholdQuery(sp1, dist - 0.00001).size() == 1 : "bad result from ii2 threshold query";
		} catch (FileNotFoundException e) {
			assert false : "failed to read inverted index from file";
		} catch (IOException e) {
			assert false : "I/O failed reading inverted index from file";
		}

	}

	private static void basicVectorTests(double[] p1, double[] p2) {

		final CartesianPoint cp1 = new CartesianPoint(p1);
		SparseCartesianPoint sp1 = new SparseCartesianPoint(cp1);
		final CartesianPoint cp2 = new CartesianPoint(p2);
		SparseCartesianPoint sp2 = new SparseCartesianPoint(cp2);

		int[] p1dims = sp1.getDims();
		assert p1dims.length == 1 : "p1 wrong dim count";
		int[] p2dims = sp2.getDims();
		assert p2dims.length == 2 : "p2 wrong dim count";

		double pSum1 = 0;
		for (double d : cp2.getNormalisedPoint()) {
			pSum1 += d;
		}
		assert pSum1 == 1.0 : "bad sum of terms in vector cp2";
		double pSum12 = 0;
		for (double d : sp2.getValues()) {
			pSum12 += d;
		}
		assert pSum12 == 1.0 : "bad sum of terms in vector sp2";

		Metric<CartesianPoint> m0 = new JensenShannonViaSed();
		Metric<SparseCartesianPoint> m1 = new JensenShannonDef1<SparseCartesianPoint>();
		Metric<SparseCartesianPoint> m2 = new JensenShannonDef2<SparseCartesianPoint>();

		double threshold = m2.distance(sp1, sp2);

		Metric<SparseCartesianPoint> m3 = new JensenShannonDef3<SparseCartesianPoint>(
				threshold + 0.00001);
		Metric<SparseCartesianPoint> m4 = new JensenShannonDef3<SparseCartesianPoint>(
				threshold - 0.00001);

		assert m0.distance(cp1, cp2) == m1.distance(sp1, sp2) : "basic vector tests: bad distance comparison 1";
		assert m0.distance(cp1, cp2) == m2.distance(sp1, sp2) : "basic vector tests: bad distance comparison 2";
		assert m0.distance(cp1, cp2) == m3.distance(sp1, sp2) : "basic vector tests: bad distance comparison 3";
		assert m4.distance(sp1, sp2) == -1 : "bad threshold calculation";
	}

	private static void testStringShingleToSparseCartesian() throws Exception {
		String s1 = "brick moulder wife";
		String s2 = "brick moulder wise";

		EventToIntegerMap<String> eToi = new EventToIntegerMap<String>();

		StringShingle ss1 = new StringShingle(s1, ShingleType.singlesAndPairs,
				eToi);
		StringShingle ss2 = new StringShingle(s2, ShingleType.singlesAndPairs,
				eToi);

		SparseCartesianPoint sp1 = new SparseCartesianPoint(ss1);
		SparseCartesianPoint sp2 = new SparseCartesianPoint(ss2);

		Metric<StringShingle> js0 = new dataPoints.compactEnsemble.JensenShannonDef2a<StringShingle>();
		Metric<SparseCartesianPoint> js1 = new JensenShannonDef2<SparseCartesianPoint>();

		assert js0.distance(ss1, ss2) == js1.distance(sp1, sp2) : "CompactEnsemble didn't translate to SparseCartesian";
	}
}
