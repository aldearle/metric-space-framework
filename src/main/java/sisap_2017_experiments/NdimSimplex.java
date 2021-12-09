package sisap_2017_experiments;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;
import util.Range;

public class NdimSimplex<T> {

	public static double getZenith(double[] apex1, double[] apex2) {
		int l = apex1.length;
		assert l == apex2.length;

		double acc = 0;
		for (int i = 0; i < l - 1; i++) {
			double diff = apex1[i] - apex2[i];
			acc += diff * diff;
		}
		double alt1 = apex1[l - 1];
		double alt2 = apex2[l - 1];

		return Math.sqrt(acc + alt1 * alt1 + alt2 * alt2);
	}

	public static double[] getBounds(double[] apex1, double[] apex2) {
		double[] res = new double[2];
		int l = apex1.length;
		assert l == apex2.length;

		double acc = 0;
		for (int i = 0; i < l - 1; i++) {
			double diff = apex1[i] - apex2[i];
			acc += diff * diff;
		}
		double lastDiff = apex1[l - 1] - apex2[l - 1];
		double lastSum = apex1[l - 1] + apex2[l - 1];
		res[0] = Math.sqrt(acc + lastDiff * lastDiff);
		res[1] = Math.sqrt(acc + lastSum * lastSum);
		return res;
	}

	public static double l2(double[] xs, double[] ys) {
		double acc = 0;
		int max = xs.length;
		for (int i = 0; i < max; i++) {
			double diff = xs[i] - ys[i];
			acc += diff * diff;
		}
		return Math.sqrt(acc);
	}

	public static double l2Flex(double[] xs, double[] ys) {
		double acc = 0;
		for (int i : Range.range(0, Math.min(xs.length, ys.length))) {
			double diff = xs[i] - ys[i];
			acc += diff * diff;
		}
		return Math.sqrt(acc);
	}

	public static double l2(double[] xs, double[] ys, int dims) {
		double acc = 0;
		for (int i : Range.range(0, dims)) {
			double diff = xs[i] - ys[i];
			acc += diff * diff;
		}
		return Math.sqrt(acc);
	}

	public static double[] getApex(double[][] points, double[] distances) {
		int dimension = distances.length;
		double res[] = new double[dimension];
		res[0] = distances[0];
		assert res[0] > 0;
		for (int i = 1; i < dimension; i++) {
			double l2 = l2Flex(res, points[i]); // +ve or zero
			double d = distances[i]; // +ve or zero
			// assert d > 0;
			double xN = points[i][i - 1];// I think, positive or zero, 'cos this
											// is the
											// last altitude
											// assert xN > Math.pow(10, -20) : d
											// + ":" + l2 + ":" + xN;
			double yN = res[i - 1];// no constraint
			double secondLastVal = yN - (d * d - l2 * l2) / (2 * xN);
			if (!Double.isFinite(secondLastVal)) {
				secondLastVal = yN;
			}
			res[i - 1] = secondLastVal;
			// assert yN * yN >= secondLastVal * secondLastVal : yN + ":"
			// + secondLastVal + " (" + i + "th dimension";
			double lastVal = Math.sqrt(yN * yN - secondLastVal * secondLastVal);
			// assert lastVal != 0;
			// assert Double.isFinite(lastVal);
			if (!Double.isFinite(lastVal)) {
				// assert Math.abs(yN - secondLastVal) == 0 :
				// "SimplexND: bad zeroing in getApex";
				lastVal = 0;
			}
			res[i] = lastVal;
		}
		return res;
	}

	private static <T> List<T> toList(T[] refPoints) {
		List<T> vals = new ArrayList<>();
		for (T p : refPoints) {
			vals.add(p);
		}
		return vals;
	}

	private List<T> referencePoints;

	// in fact a lower triangular matrix
	private double[][] baseSimplex;

	private Metric<T> metric;

	private int dimension;

	public NdimSimplex(Metric<T> metric, List<T> refPoints) {
		this.dimension = refPoints.size();
		this.metric = metric;
		this.referencePoints = refPoints;
		initialise();
	}

	@SafeVarargs
	public NdimSimplex(Metric<T> metric, T... refPoints) {
		this.dimension = refPoints.length;
		this.metric = metric;
		this.referencePoints = toList(refPoints);
		initialise();
	}

	public double[] getApex(double[] dists) {
		return getApex(this.baseSimplex, dists);
	}

	public double[] getApex(T p) {
		double[] dists = new double[this.referencePoints.size()];
		for (int i = 0; i < dists.length; i++) {
			dists[i] = this.metric.distance(p, this.referencePoints.get(i));
		}
		return getApex(this.baseSimplex, dists);
	}

	private void initialise() {
		this.baseSimplex = new double[this.dimension][this.dimension - 1];
		// form the inductive base case
		this.baseSimplex[1][0] = this.metric.distance(this.referencePoints.get(0), this.referencePoints.get(1));
		if (this.baseSimplex[1][0] == 0) {
			throw new RuntimeException("zero distance points for base simplex");
		}
		// for every further reference point, add the apex
		for (int i = 2; i < this.referencePoints.size(); i++) {
			// distances to all previous points
			double[] dists = new double[i];
			for (int j = 0; j < i; j++) {
				dists[j] = this.metric.distance(this.referencePoints.get(i), this.referencePoints.get(j));
			}
			double[] apex = getApex(this.baseSimplex, dists);
			this.baseSimplex[i] = apex;
		}
	}
}
