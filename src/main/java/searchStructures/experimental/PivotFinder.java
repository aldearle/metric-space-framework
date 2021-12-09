package searchStructures.experimental;

import java.util.ArrayList;
import java.util.List;

import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

public class PivotFinder {

	public static <T> List<Integer> getPivotIndices(List<T> data,
			Metric<T> metric, int noReqd) {
		List<Integer> res = new ArrayList<>();
		res.add(0);
		int lastAdded = 0;
		int batch = 0;
		int batchSize = 500;
		double firstDistance = -1;
		while (res.size() < noReqd) {
			T nextDatum = data.get(lastAdded);
			double biggest = 0;
			lastAdded = batch * batchSize;
			for (int i = batch * batchSize; i < (batch * batchSize) + batchSize; i++) {
				double d = metric.distance(nextDatum, data.get(i));
				if (d > biggest) {
					boolean closeToAnother = false;
					for (int pivot : res) {
						if (metric.distance(data.get(i), data.get(pivot)) <= firstDistance / 2) {
							closeToAnother = true;
						}
					}
					if (!closeToAnother) {
						lastAdded = i;
						biggest = d;
					}
				}
			}
			if (firstDistance == -1) {
				firstDistance = biggest;
			}
			res.add(lastAdded);
			batch++;
		}
		return res;

	}

	public static void main(String[] a) throws Exception {
		final SisapFile sisapFile = TestLoad.SisapFile.colors;
		TestLoad tl = new TestLoad(sisapFile);
		double[] thresholds = TestLoad.getSisapThresholds(sisapFile);
		final double threshold = thresholds[0];

		System.out.println("data size is " + tl.dataSize());

		List<CartesianPoint> qs = tl.getQueries(tl.dataSize() / 10);
		List<CartesianPoint> dat = tl.getDataCopy();

		final Euclidean<CartesianPoint> euc = new Euclidean<>();
		List<Integer> pivs = getPivotIndices(dat, euc, 10);
		for (int i : pivs) {
			System.out.println(i);
		}

		for (int i : pivs) {
			for (int j : pivs) {
				System.out.print(euc.distance(dat.get(i), dat.get(j)) + "\t");
			}
			System.out.println();
		}
	}
}
