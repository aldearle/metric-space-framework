package n_point_surrogate;

import coreConcepts.Metric;
import util.Range;

import java.util.List;

public class Simplex_float<T> {

	private float[][] baseSimplex;
	private int dimension;
	private Metric<T> metric;
	private List<T> refPoints;

	public Simplex_float(int dimension, Metric<T> metric, List<T> refPoints)
			throws Exception {
		this.dimension = dimension;
		this.metric = metric;
		this.refPoints = refPoints;
		initialise(dimension, metric, refPoints);
	}

	private void initialise(int dimension, Metric<T> metric, List<T> refPoints) {
		this.baseSimplex = new float[dimension][dimension - 1];
		// form the inductive base case
		this.baseSimplex[1][0] = (float) metric.distance(refPoints.get(0),
				refPoints.get(1));
		assert this.baseSimplex[1][0] != 0 : "zero distance for base simplex";

		// for every further reference point, add the apex
		for (int i : Range.range(2, refPoints.size())) {
			// distances to all previous points
			float[] dists = new float[i];
			for (int j : Range.range(0, i)) {
				dists[j] = (float) metric.distance(refPoints.get(i),
						refPoints.get(j));
			}
			float[] apex = getApex(this.baseSimplex, dists);
			this.baseSimplex[i] = apex;
		}
	}

	public float[] formSimplex(float[] dists) {
		return getApex(this.baseSimplex, dists);
	}

	public float[] formSimplex(T p) {
		float[] dists = new float[this.dimension];
		for (int i = 0; i < dists.length; i++) {
			dists[i] = (float) this.metric.distance(p, this.refPoints.get(i));
		}
		return getApex(this.baseSimplex, dists);
	}

	public static float[] getApex(float[][] points, float[] distances) {
		assert points.length >= distances.length : "bad argument to getApex ("
				+ points.length + ";" + distances.length + ")";
		int dimension = distances.length;
		float res[] = new float[dimension];
		res[0] = distances[0];
		for (int i = 1; i < dimension; i++) {
			float l2 = l2(res, points[i]);
			float d = distances[i];
			float xN = points[i][i - 1];
			float yN = res[i - 1];
			float secondLastVal = yN - (d * d - l2 * l2) / (2 * xN);
			res[i - 1] = secondLastVal;
			float lastVal = (float) Math.sqrt(yN * yN - secondLastVal
					* secondLastVal);
			if (Float.isNaN(lastVal)) {
				lastVal = 0;
			}
			res[i] = lastVal;
		}
		return res;
	}

	private static float l2(float[] fs1, float[] fs2) {
		float acc = 0;
		for (int i : Range.range(0, Math.min(fs1.length, fs2.length))) {
			float diff = fs1[i] - fs2[i];
			acc += diff * diff;
		}
		return (float) Math.sqrt(acc);
	}

	// public static void main(String[] a) throws Exception {
	// double[] v1 = { 0, 1, 0, 1, 3 };
	// double[] v2 = { 5, 0, 0, 0, 6 };
	// double[] v3 = { 5, 12, 0, 0, 9 };
	// double[] v4 = { 10, 10, 10, 0, 1 };
	// double[] v5 = { 5, 5, 5, 5, 2 };
	// double[] v6 = { 1, 2, 3, 4, 5 };
	// CartesianPoint p1 = new CartesianPoint(v1);
	// CartesianPoint p2 = new CartesianPoint(v2);
	// CartesianPoint p3 = new CartesianPoint(v3);
	// CartesianPoint p4 = new CartesianPoint(v4);
	// CartesianPoint p5 = new CartesianPoint(v5);
	// CartesianPoint p6 = new CartesianPoint(v6);
	// Metric<CartesianPoint> m = new Euclidean<>();
	//
	// CartesianPoint[] refs = { p1, p2, p3 };
	// List<CartesianPoint> re = new ArrayList<>();
	// for (CartesianPoint r : refs) {
	// re.add(r);
	// }
	// Simplex_float<CartesianPoint> s = new Simplex_float<>(3, m, re);
	// float[][] x0 = { s.formSimplex(p4) };
	//
	// Simplex3D<CartesianPoint> s2 = new Simplex3D<>(3, m, p1, p2, p3);
	// double[][] x1 = { s2.formSimplex(p4) };
	// SimplexGeneral.display("smipel", x1);
	//
	// }
}
