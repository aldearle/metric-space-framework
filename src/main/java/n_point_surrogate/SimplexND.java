package n_point_surrogate;

import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import supermetrics.Simplex3D;
import supermetrics.SimplexGeneral;

import java.util.Collection;

public class SimplexND<T> extends SimplexExclusion<T> {

	double[][] baseSimplex;
	float[][] baseSimplexF;

	public SimplexND(int dimension, Metric<T> metric, Collection<T> refPoints)
			throws Exception {
		super(dimension, metric, refPoints);
		initialise(dimension, metric);
	}

	@SafeVarargs
	public SimplexND(int dimension, Metric<T> metric, T... refPoints)
			throws Exception {
		super(dimension, metric, refPoints);
		initialise(dimension, metric);
	}

	private void initialise(int dimension, Metric<T> metric) {
		baseSimplex = new double[dimension][dimension - 1];
		// form the inductive base case
		baseSimplex[1][0] = metric.distance(this.referencePoints[0],
				this.referencePoints[1]);
		if (baseSimplex[1][0] == 0) {
			throw new RuntimeException("zero distance points for base simplex");
		}
		// for every further reference point, add the apex
		for (int i = 2; i < this.referencePoints.length; i++) {
			// distances to all previous points
			double[] dists = new double[i];
			for (int j = 0; j < i; j++) {
				dists[j] = metric.distance(this.referencePoints[i],
						this.referencePoints[j]);
			}
			double[] apex = getApex(baseSimplex, dists);
			baseSimplex[i] = apex;
		}
	}

	public double[] formSimplex(double[] dists) {
		return getApex(this.baseSimplex, dists);
	}

	@Override
	public double[] formSimplex(T p) {
		double[] dists = new double[this.referencePoints.length];
		for (int i = 0; i < dists.length; i++) {
			dists[i] = this.metric.distance(p, this.referencePoints[i]);
		}
		return getApex(this.baseSimplex, dists);
	}

	@Override
	public float[] formSimplexF(T p) {
		if (this.baseSimplexF == null) {
			this.baseSimplexF = new float[this.baseSimplex.length][this.baseSimplex[0].length];
		}
		float[] dists = new float[this.referencePoints.length];
		for (int i = 0; i < dists.length; i++) {
			dists[i] = (float) this.metric.distance(p, this.referencePoints[i]);
		}
		return getApexF(this.baseSimplexF, dists);
	}

	public static double[] getApex(double[][] points, double[] distances) {
		// assert points.length == distances.length;
		int dimension = distances.length;
		double res[] = new double[dimension];
		res[0] = distances[0];
		for (int i = 1; i < dimension; i++) {
			double l2 = SimplexExclusion.l2(res, points[i]);
			double d = distances[i];
			double xN = points[i][i - 1];
			double yN = res[i - 1];
			double secondLastVal = yN - (d * d - l2 * l2) / (2 * xN);
			res[i - 1] = secondLastVal;
			double lastVal = Math.sqrt(yN * yN - secondLastVal * secondLastVal);
			if (Double.isNaN(lastVal)) {
//				assert Math.abs(yN - secondLastVal) == 0 : "SimplexND: bad zeroing in getApex";
				lastVal = 0;
			}
			res[i] = lastVal;
		}
		return res;
	}

	public static float[] getApexF(float[][] points, float[] distances) {
		// assert points.length == distances.length;
		int dimension = distances.length;
		float res[] = new float[dimension];
		res[0] = (float) distances[0];
		for (int i = 1; i < dimension; i++) {
			double l2 = SimplexExclusion.l2(res, points[i]);
			double d = distances[i];
			double xN = points[i][i - 1];
			double yN = res[i - 1];
			double secondLastVal = yN - (d * d - l2 * l2) / (2 * xN);
			res[i - 1] = (float) secondLastVal;
			double lastVal = Math.sqrt(yN * yN - secondLastVal * secondLastVal);
			if (Double.isNaN(lastVal)) {
				assert Math.abs(yN - secondLastVal) < 0.0000001 : "SimplexND: bad zeroing in getApex";
				lastVal = 0;
			}
			res[i] = (float) lastVal;
		}
		return res;
	}

	public static double[][] getAllApex(double[][] points, double[] distances) {
		assert points.length == distances.length;
		int dimension = distances.length;
		double res[][] = new double[dimension][dimension];
		res[0][0] = distances[0];
		for (int i = 1; i < dimension; i++) {
			res[i] = new double[dimension];
			for (int j = 0; j < i; j++) {
				res[i][j] = res[i - 1][j];
			}
			double l2 = SimplexExclusion.l2(res[i - 1], points[i]);
			double d = distances[i];
			double xN = points[i][i - 1];
			double yN = res[i][i - 1];
			double secondLastVal = yN - (d * d - l2 * l2) / (2 * xN);
			res[i][i - 1] = secondLastVal;
			double lastVal = Math.sqrt(yN * yN - secondLastVal * secondLastVal);
			res[i][i] = lastVal;
		}
		return res;
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
		Metric<CartesianPoint> m = new Euclidean<>();

		CartesianPoint[] refs = { p1, p2, p3 };
		SimplexND<CartesianPoint> s = new SimplexND<>(3, m, refs);
		double[][] x0 = { s.formSimplex(p4) };
		SimplexGeneral.display("simplex", x0);

		Simplex3D<CartesianPoint> s2 = new Simplex3D<>(3, m, p1, p2, p3);
		double[][] x1 = { s2.formSimplex(p4) };
		SimplexGeneral.display("smipel", x1);

		double[] dists = { s.metric.distance(p1, p6),
				s.metric.distance(p2, p6), s.metric.distance(p3, p6) };
		double[] ap = getApex(s.baseSimplex, dists);
		double[][] allAp = getAllApex(s.baseSimplex, dists);

		SimplexGeneral.display("apex", ap);
		SimplexGeneral.display("allApex", allAp);

	}
}