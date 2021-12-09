package query;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import util.OrderedList;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.SEDByComplexity;
import dataPoints.doubleArray.JSDoubleArray;

public class InvertedIndexArray implements MetricIndex<CartesianPoint> {

	private static double MAX_ACC = Math.log(2) * 2;

	private double[][] dataArray;
	private List<CartesianPoint> dataSet;
	Metric<CartesianPoint> metric;

	public InvertedIndexArray(List<CartesianPoint> data) {
		this.dataSet = data;
		this.metric = new SEDByComplexity();
		double[] first = data.get(0).getPoint();

		this.dataArray = new double[first.length][data.size()];
		int p1 = 0;
		for (CartesianPoint p : data) {
			int p2 = 0;
			for (double d : p.getNormalisedPoint()) {
				this.dataArray[p2++][p1] = d;
			}
			p1++;
		}
	}

	private static double xLogx(double d) {
		return d * Math.log(d);
	}

	public static double JSComponent(double d1, double d2) {
		if (d1 == 0 || d2 == 0) {
			return 0;
		} else {
			return xLogx(d1 + d2) - xLogx(d1) - xLogx(d2);
		}
	}

	public List<Double> dimComplexities() {
		List<Double> res = new ArrayList<Double>();
		return res;
	}

	public InvertedIndexArray(File f) throws FileNotFoundException, IOException,
			ClassNotFoundException {
		ObjectInputStream oos = new ObjectInputStream(new FileInputStream(f));
		this.dataArray = (double[][]) oos.readObject();
		oos.close();
	}

	public void save(File f) throws FileNotFoundException, IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
		oos.writeObject(this.dataArray);
		oos.close();
	}

	@Override
	public List<CartesianPoint> thresholdQuery(CartesianPoint query,
			double threshold) {

		int phase1dims = 130;
		List<CartesianPoint> res = new ArrayList<CartesianPoint>();

		double[] thisPoint = query.getNormalisedPoint();
		double[] contendorsAcc = new double[this.dataSet.size()];
		double[] contendorsConsumed = new double[this.dataSet.size()];

		List<Integer> order = getOrdering(phase1dims, thisPoint);

		assert order.size() == phase1dims : order.size() + ":" + phase1dims;

		/*
		 * phase 1
		 */
		int dimensionsDone = 0;
		int noRemoved = 0;

		double queryAcc = 0;
		double queryConsumed = 0;
		/*
		 * iterate through the dimensions
		 */
		// int dimNo = 0;
		// while (nonZeros < phase1dims && dimNo < thisPoint.length) {
		for (int dimNo : order) {
			double queryVal = thisPoint[dimNo];

			if (queryVal != 0) {

				queryAcc += JSComponent(queryVal, queryVal);
				queryConsumed += queryVal;
				double[] thisDimension = this.dataArray[dimNo];

				/*
				 * iterate through all the points at that dimension
				 */
				for (int pointNo = 0; pointNo < this.dataSet.size(); pointNo++) {

					final double contenderAcc = contendorsAcc[pointNo];
					if (contenderAcc != -1) {

						final double contenderVal = thisDimension[pointNo];

						final double newContenderConsumed = contendorsConsumed[pointNo]
								+ contenderVal;
						double newContenderAcc = contenderAcc
								+ JSComponent(contenderVal, queryVal);

						if (stillInContention(threshold, newContenderAcc,
								queryConsumed, newContenderConsumed)) {

							contendorsAcc[pointNo] = newContenderAcc;
							contendorsConsumed[pointNo] = newContenderConsumed;
						} else {
							contendorsAcc[pointNo] = -1;
							noRemoved++;
						}
					}
				}
			}
			// dimNo++;
			dimensionsDone++;
		}

		assert dimensionsDone == order.size();

		/*
		 * phase 2
		 */

		int queryPointNo = this.dataSet.indexOf(query);
		if (queryPointNo == 1606 || queryPointNo == 1471) {
			System.out.println(noRemoved + " points removed from fish "
					+ queryPointNo);
			System.out.println(contendorsAcc[1471] + ";" + contendorsAcc[1606]);
		}
		int pointNo = 0;
		/*
		 * now iterate through all points, checking contendors not switched to
		 * -1
		 */
		for (double flag : contendorsAcc) {
			if (flag != -1 && pointNo != queryPointNo) {

				// might be within the threshold
				double distance = this.metric.distance(query,
						this.dataSet.get(pointNo));

				double jsSim = JSDoubleArray.naturalJS(query
						.getNormalisedPoint(), this.dataSet.get(pointNo)
						.getNormalisedPoint());

				boolean mightBeOne = (jsSim >= MAX_ACC - threshold);
				// int p4 = 0;
				// while (mightBeOne && p4 < thisPoint.length) {
				//
				// if (thisPoint[p4] != this.dataArray[p3][p4]) {
				// mightBeOne = false;
				// }
				//
				// p4++;
				// }

				if (mightBeOne) {
					res.add(this.dataSet.get(pointNo));
				}
			}
			pointNo++;
		}

		return res;
	}

	private List<Integer> getOrdering(int dims, double[] thisPoint) {

		OrderedList<Integer, Double> ol = new OrderedList(dims);

		for (int i = 0; i < thisPoint.length; i++) {
			ol.add(i, 1 - thisPoint[i]);
		}
		return ol.getList();
	}

	static boolean stillInContention(double threshold, double contentderAcc,
			double queryConsumed, double contenderConsumed) {

		double maxResidue = JSComponent(1 - queryConsumed,
				1 - contenderConsumed);

		double maxPossMatch = maxResidue + contentderAcc;

		return maxPossMatch > MAX_ACC - threshold;

	}

}
