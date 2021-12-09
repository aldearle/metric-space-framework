package referenceSets;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import util.OrderedList;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

/**
 * @author Richard Connor
 * 
 *         creates a list of reference points from a set, according to a given
 *         strategy
 * 
 */
public class ReferencePoints extends ArrayList<CartesianPoint> {

	public static String STRAT_FIRST = "first";
	public static String STRAT_LARGE = "largest_total";
	public static String STRAT_CORNERS = "corners";

	private Metric<CartesianPoint> metric;

	public ReferencePoints(Metric<CartesianPoint> metric,
			List<CartesianPoint> fromSet, int number, String strategy)
			throws Exception {
		super();
		this.metric = metric;

		assert fromSet.size() >= number;

		if (strategy.equals(STRAT_FIRST)) {
			for (int i = 0; i < number; i++) {
				this.add(fromSet.get(i));
			}
		} else if (strategy.equals(STRAT_LARGE)) {
			setWidelySpacedPivots(fromSet, metric, number);

		} else if (strategy.equals(STRAT_CORNERS)) {
			setCornerPoints(fromSet, number);

		} else {
			throw new Exception("unknown strategy: " + strategy);
		}

	}

	private void setCornerPoints(List<CartesianPoint> fromSet, int number) {
		/*
		 * this will be the maximum value in each of the first number dimensions
		 * of the fromSet data
		 */
		int dataDims = fromSet.get(0).getPoint().length;
		double[] maxes = new double[number];
		for (CartesianPoint point : fromSet) {
			int dim = 0;
			final double[] point2 = point.getPoint();
			for (int i = 0; i < number; i++) {
				maxes[dim] = Math.max(maxes[dim++], point2[i]);
			}
		}

		for (int i = 0; i < number; i++) {
			/*
			 * needs to be the same number of dimensions as the data of course
			 */
			double[] newPivot = new double[dataDims];
			for (int j = 1; j <= i; j++) {
				newPivot[j] = maxes[j];
			}
			CartesianPoint np = new CartesianPoint(newPivot);
			this.add(np);
		}
	}

	private void setWidelySpacedPivots(List<CartesianPoint> all,
			Metric<CartesianPoint> metric, int number) throws Exception {

		assert all.size() > number + 1;

		OrderedList<CartesianPoint, Double> ol = new OrderedList<CartesianPoint, Double>(
				number);

		for (int i = 0; i < number + 1; i++) {
			CartesianPoint x = all.get(i);
			double distAcc = 0;
			for (int j = 0; i < number + 1; i++) {
				CartesianPoint y = all.get(j);
				distAcc -= metric.distance(x, y);
			}
			ol.add(x, distAcc);
		}

		/*
		 * ol now contains the best n of the first n+1 points, each compared
		 * against n points
		 */

		for (int i = number + 1; i < all.size(); i++) {
			CartesianPoint p = all.get(i);
			double distAcc = 0;
			for (CartesianPoint q : ol.getList()) {
				distAcc -= metric.distance(p, q);
			}
			ol.add(p, distAcc);
		}
		for (CartesianPoint p : ol.getList()) {
			this.add(p);
		}
	}

	public void buildDistanceIndexFile(List<CartesianPoint> data,
			String outFileName) throws Exception {

		PrintStream out = new PrintStream(outFileName);
		for (CartesianPoint p : data) {
			for (CartesianPoint refPoint : this) {
				double d = this.metric.distance(p, refPoint);
				out.print((float) d + " ");
			}
			out.println();
		}
		out.close();
	}

}
