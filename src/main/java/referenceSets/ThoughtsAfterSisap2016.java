package referenceSets;

import java.util.List;

import testloads.CartesianThresholds;
import testloads.TestLoad;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

public class ThoughtsAfterSisap2016 {

	public static void main(String[] a) {
		final int dims = 6;
		TestLoad tl = new TestLoad(dims, 100000 + 40, false);
		Metric<CartesianPoint> euc = new Euclidean<>();
		List<CartesianPoint> P = tl.getQueries(20);
		List<CartesianPoint> queries = tl.getQueries(20);
		List<CartesianPoint> data = tl.getDataCopy();

		double threshold = CartesianThresholds.getThreshold("euc", dims, 1);
		double[][] laesaTable = new double[P.size()][data.size()];
		int pPtr = 0;
		for (CartesianPoint p : P) {
			int sPtr = 0;
			for (CartesianPoint s : data) {
				laesaTable[pPtr][sPtr++] = euc.distance(p, s);
			}
			pPtr++;
		}

		for (CartesianPoint p : P) {
			for (CartesianPoint q : queries) {
				final double[] psDists = laesaTable[0];

				double[] thetaDiffs = new double[psDists.length];
				double pqDist = euc.distance(p, q);
				int ptr = 0;
				int exclusions = 0;
				for (double psDist : psDists) {
					final double diff = psDist - pqDist;
					thetaDiffs[ptr++] = diff;
					if (Math.abs(diff) > threshold) {
						exclusions++;
					}
				}

				System.out.print((double) exclusions / data.size() + "\t");
			}
			System.out.println();
		}
	}
}
