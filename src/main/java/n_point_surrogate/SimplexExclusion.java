package n_point_surrogate;

import java.util.Collection;
import java.util.List;

import coreConcepts.Metric;

public abstract class SimplexExclusion<T> {

	/**
	 * just Euclidean distance, allows different lengths assuming one array is
	 * padded with zeros
	 * 
	 * @param xs
	 * @param ys
	 * @return
	 */
	public static double l2(double[] xs, double[] ys) {
		double acc = 0;
		int length = Math.min(xs.length, ys.length);
		for (int i = 0; i < length; i++) {
			double d = xs[i] - ys[i];
			acc += d * d;
		}
		return Math.sqrt(acc);
	}

	private int dimension;
	private double[][] apexCoordinates;
	protected T[] referencePoints;
	protected Metric<T> metric;
	private List<T> data;

	public SimplexExclusion(int dimension, Metric<T> metric,
			Collection<T> refPoints) throws Exception {
		initialise(dimension, metric, toArray(refPoints));
	}

	public SimplexExclusion(int dimension, Metric<T> metric, T... refPoints)
			throws Exception {
		initialise(dimension, metric, refPoints);
	}

	public int countExclusions(T query, double threshold) {
		int res = 0;
		double[] qs = formSimplex(query);
		for (double[] sur : this.apexCoordinates) {
			if (l2(qs, sur) > threshold) {
				res++;
			}
		}
		return res;
	}

	public double[] getApexCentroid() {
		double[] res = new double[dimension];
		for (double[] ap : this.apexCoordinates) {
			for (int i = 0; i < dimension; i++) {
				res[i] += ap[i];
			}
		}
		for (int i = 0; i < dimension; i++) {
			res[i] /= this.apexCoordinates.length;
		}
		return res;
	}

	public double[][] getApexPoints() {
		return this.apexCoordinates;
	}

	public double[] getApexSDs(double[] means) {
		double[] res = new double[dimension];
		for (double[] ap : this.apexCoordinates) {
			for (int i = 0; i < dimension; i++) {
				res[i] += (ap[i] - means[i]) * (ap[i] - means[i]);
			}
		}
		for (int i = 0; i < dimension; i++) {
			res[i] /= this.apexCoordinates.length;
			res[i] = Math.sqrt(res[i]);
		}
		return res;
	}

	public void trackExclusions(boolean[] excluded, T query, double threshold) {
		double[] qs = formSimplex(query);

		for (int i = 0; i < excluded.length; i++) {
			boolean done = excluded[i];
			if (!done) {
				if (l2(qs, this.apexCoordinates[i]) > threshold) {
					excluded[i] = true;
				}
			}
		}
	}

	private void initialise(int dimension, Metric<T> metric, T[] refPoints)
			throws Exception {
		this.referencePoints = refPoints;
		this.metric = metric;
		this.dimension = dimension;
		if (refPoints.length != dimension) {
			throw new Exception("wrong number of ref points provided");
		}
	}

	private T[] toArray(Collection<T> refPoints) {
		Object[] res = new Object[refPoints.size()];
		int ptr = 0;
		for (T p : refPoints) {
			res[ptr++] = p;
		}
		return (T[]) res;
	}

	public void addData(List<T> data) {
		this.data = data;
		this.apexCoordinates = new double[data.size()][this.referencePoints.length];
		int i = 0;
		for (T p : data) {
			this.apexCoordinates[i++] = formSimplex(p);
		}
	}

	/**
	 * @param p
	 * @return the coordinates of this point mapped into as the "top" of an
	 *         n-dimensional simplex of which the base is defined by the
	 *         reference points
	 */
	public abstract double[] formSimplex(T p);

	public static float l2(float[] xs, float[] ys) {
		float acc = 0;
		int length = Math.min(xs.length, ys.length);
		for (int i = 0; i < length; i++) {
			double d = xs[i] - ys[i];
			acc += d * d;
		}
		return (float) Math.sqrt(acc);
	}

	public abstract float[] formSimplexF(T p);

}
