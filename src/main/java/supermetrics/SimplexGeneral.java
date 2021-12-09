package supermetrics;

import java.util.ArrayList;
import java.util.List;

import n_point_surrogate.SimplexExclusion;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.TriDiscrim;

public class SimplexGeneral<T> {
	/*
	 * only need the upper triangular matrix but we'll just calculate the whole
	 * thing to make it clearer...
	 */
	double[][] distanceMatrix;
	double[][] coordinateMatrix;
	Metric<T> metric;

	public SimplexGeneral(int dimension, Metric<T> metric, List<T> refPoints)
			throws Exception {
		if (refPoints.size() != dimension + 1) {
			throw new Exception("wrong no of ref points");
		}
		this.metric = metric;
		this.distanceMatrix = new double[refPoints.size()][refPoints.size()];
		this.coordinateMatrix = new double[dimension + 1][dimension];

		for (int i = 0; i < refPoints.size() - 1; i++) {
			T p1 = refPoints.get(i);
			for (int j = i + 1; j < refPoints.size(); j++) {
				T p2 = refPoints.get(j);
				distanceMatrix[i][j] = metric.distance(p1, p2);
				// just to print neatly, really!
				distanceMatrix[j][i] = distanceMatrix[i][j];
			}
		}

		formCoordinateMatrix(dimension, refPoints);

	}

	static double[] getApex(double[][] points, double[] distances) {
		assert points.length == distances.length;
		int dimension = distances.length;
		double res[] = new double[dimension];
		res[0] = distances[0];
		for (int i = 1; i < dimension; i++) {
			double l2 = SimplexExclusion.l2(points[i], res);
			double d = distances[i];
			double xN = points[i][i - 1];
			double yN = res[i - 1];
			double secondLastVal = yN - (d * d - l2 * l2) / (2 * xN);
			res[i - 1] = secondLastVal;
			double lastVal = Math.sqrt(yN * yN - secondLastVal * secondLastVal);
			res[i] = lastVal;
		}
		return res;
	}

	private static void print(double[] ds) {
		System.out.print("[");
		for (double d : ds) {
			System.out.print(d + ", ");
		}
		System.out.println("]");
	}

	private void formCoordinateMatrix(int dimension, List<T> refPoints)
			throws Exception {
		if (dimension == 1) {
			this.coordinateMatrix[1][0] = distanceMatrix[0][1];
		} else {
			List<T> refs1 = new ArrayList<>();
			for (int i = 0; i < refPoints.size() - 1; i++) {
				refs1.add(refPoints.get(i));
			}

			SimplexGeneral sg1 = new SimplexGeneral(dimension - 1, this.metric,
					refs1);
			double[][] sg1a = sg1.getCoordinateMatrix();
			int rowPtr = 0;
			for (double[] row : sg1a) {
				int colPtr = 0;
				for (double entry : row) {
					this.coordinateMatrix[rowPtr][colPtr++] = entry;
				}
				rowPtr++;
			}

			refs1.remove(refs1.size() - 1);
			refs1.add(refPoints.get(refPoints.size() - 1));
			SimplexGeneral sg2 = new SimplexGeneral(dimension - 1, this.metric,
					refs1);
			double[][] sg2a = sg2.getCoordinateMatrix();
			int colPtr = 0;
			for (double entry : sg2a[dimension - 1]) {
				this.coordinateMatrix[dimension][colPtr++] = entry;
			}

			double l2 = SimplexExclusion.l2(coordinateMatrix[dimension - 1],
					coordinateMatrix[dimension]);
			double d = distanceMatrix[dimension - 1][dimension];
			double xN = coordinateMatrix[dimension - 1][dimension - 2];
			double yN = coordinateMatrix[dimension][dimension - 2];
			double secondLastVal = (l2 * l2 - d * d) / (2 * xN) + yN;
			coordinateMatrix[dimension][dimension - 2] = secondLastVal;
			double lastVal = Math.sqrt(yN * yN - secondLastVal * secondLastVal);
			coordinateMatrix[dimension][dimension - 1] = lastVal;
		}
	}

	public static void display(String s, double[] ds) {
		System.out.println(s);
		for (double d : ds) {
			System.out.print(d + "\t");
		}
		System.out.println();
	}

	public static void display(String s, double[][] ds) {
		System.out.println(s);
		for (double[] row : ds) {
			for (double d : row) {
				System.out.print(d + "\t");
			}
			System.out.println();
		}
	}

	private void display() {
		display("distance matrix", distanceMatrix);
		display("coordinate matrix", coordinateMatrix);
	}

	private void recreateDistances() {
		System.out.println("recreated distance matrix");
		for (double[] d1 : coordinateMatrix) {
			for (double[] d2 : coordinateMatrix) {
				System.out.print(SimplexExclusion.l2(d1, d2) + "\t");
			}
			System.out.println();
		}
	}

	double[][] getCoordinateMatrix() {
		return this.coordinateMatrix;
	}

	public static void main(String[] a) throws Exception {
		double[] v1 = { 0, 1, 0, 1, 3 };
		double[] v2 = { 5, 0, 0, 0, 6 };
		double[] v3 = { 5, 12, 0, 0, 9 };
		double[] v4 = { 10, 10, 10, 0, 1 };
		double[] v5 = { 5, 5, 5, 5, 2 };
		double[] v6 = { 1, 2, 3, 4, 5 };
		CartesianPoint p1 = new CartesianPoint(v1);
		CartesianPoint p2 = new CartesianPoint(v2);
		CartesianPoint p3 = new CartesianPoint(v3);
		CartesianPoint p4 = new CartesianPoint(v4);
		CartesianPoint p5 = new CartesianPoint(v5);
		CartesianPoint p6 = new CartesianPoint(v6);
		Metric<CartesianPoint> metric = new TriDiscrim<>();

		/*
		 * create a one-dimensional simplex from the first two points
		 */
		double[][] oneDim = new double[2][1];
		final double d1 = metric.distance(p1, p2);
		System.out.println(d1);
		oneDim[1][0] = d1;
		double[] dists = new double[2];
		dists[0] = metric.distance(p1, p3);
		dists[1] = metric.distance(p2, p3);

		double[] tri = getApex(oneDim, dists);
		double[][] twoDim = merge(oneDim, tri);
		display("new triangle", twoDim);

		double[] newDists = new double[3];

		newDists[0] = metric.distance(p1, p4);
		newDists[1] = metric.distance(p2, p4);
		newDists[2] = metric.distance(p3, p4);

		double[] tet = getApex(twoDim, newDists);
		double[][] threeDim = merge(twoDim, tet);
		display("new tetra", threeDim);

		double[] nextDists = new double[4];

		nextDists[0] = metric.distance(p1, p5);
		nextDists[1] = metric.distance(p2, p5);
		nextDists[2] = metric.distance(p3, p5);
		nextDists[3] = metric.distance(p4, p5);

		double[] simpl = getApex(threeDim, nextDists);
		double[][] fourDim = merge(threeDim, simpl);
		display("new simpl", fourDim);

		oldTest();

	}

	private static double[][] merge(double[][] oneDim, double[] tri) {
		double[][] res = new double[oneDim.length + 1][tri.length];
		for (int i = 0; i < oneDim.length; i++) {
			res[i] = oneDim[i];
		}
		res[oneDim.length] = tri;
		return res;
	}

	protected static void oldTest() throws Exception {
		double[] v1 = { 0, 1, 0, 1, 3 };
		double[] v2 = { 5, 0, 0, 0, 6 };
		double[] v3 = { 5, 12, 0, 0, 9 };
		double[] v4 = { 10, 10, 10, 0, 1 };
		double[] v5 = { 5, 5, 5, 5, 2 };
		double[] v6 = { 1, 2, 3, 4, 5 };
		CartesianPoint p1 = new CartesianPoint(v1);
		CartesianPoint p2 = new CartesianPoint(v2);
		CartesianPoint p3 = new CartesianPoint(v3);
		CartesianPoint p4 = new CartesianPoint(v4);
		CartesianPoint p5 = new CartesianPoint(v5);
		CartesianPoint p6 = new CartesianPoint(v6);
		Metric<CartesianPoint> metric = new TriDiscrim<>();

		List<CartesianPoint> refs = new ArrayList<>();
		refs.add(p1);
		refs.add(p2);
		refs.add(p3);
		refs.add(p4);
		refs.add(p5);
		// refs.add(p6);

		SimplexGeneral<CartesianPoint> sg = new SimplexGeneral<>(4, metric,
				refs);

		sg.display();
		sg.recreateDistances();
	}
}
