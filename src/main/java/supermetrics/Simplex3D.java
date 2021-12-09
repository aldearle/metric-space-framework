package supermetrics;

import java.util.List;

import n_point_surrogate.SimplexExclusion;
import coreConcepts.Metric;

public class Simplex3D<T> extends SimplexExclusion<T> {

	double[] refPointDists;

	public Simplex3D(int dimension, Metric<T> metric, T... refPoints)
			throws Exception {
		super(dimension, metric, refPoints);
		this.refPointDists = new double[3];
		this.refPointDists[0] = this.metric.distance(this.referencePoints[0],
				this.referencePoints[1]);
		this.refPointDists[1] = this.metric.distance(this.referencePoints[1],
				this.referencePoints[2]);
		this.refPointDists[2] = this.metric.distance(this.referencePoints[0],
				this.referencePoints[2]);
	}

	@Override
	public double[] formSimplex(T p) {
		return formSimplexTest(p, false);
	}

	public double[] formSimplexTest(T p, boolean printTest) {
		double[] res = new double[3];
		/*
		 * ref points define a base triangle; let's call these sides a, b and c
		 */
		double a = this.refPointDists[0];
		double b = this.refPointDists[1];
		double c = this.refPointDists[2];

		// so the uppercase dist is the side opposing the lowercase one
		double A = this.metric.distance(this.referencePoints[2], p);
		double B = this.metric.distance(this.referencePoints[0], p);
		double C = this.metric.distance(this.referencePoints[1], p);

		double baseArea = Simplex2D.getArea(a, b, c);
		double volume = getVolume(a, b, c, A, B, C);

		// volume = 1/3 area of base * height
		double z = (volume * 3) / baseArea;
		res[2] = z;

		/*
		 * so let's say a is the distance along the X axis:
		 * 
		 * p_1 = (0,0,0), p_2 = (a,0,0)
		 * 
		 * we know the z coordinate so lets measure the distance d0 from the
		 * apex to the X axis... by the fact that the area of triangle with
		 * sides a,B,C is half a times d0
		 */
		double area = Simplex2D.getArea(a, B, C);
		double d0 = (area * 2) / a;
		/*
		 * TODO: actually y can be negative, as can x, so this is not correct
		 */
		// // so the Y coordinate can be calculated from this and the z
		// coordinate
		double y = Math.sqrt(d0 * d0 - z * z);
		res[1] = y;
		//
		// // and X can then be found from d0 and the distance from p_1 to
		// apex...
		// // can
		// // use either B or C as long as it's consistent
		res[0] = Math.sqrt(B * B - d0 * d0);

		if (printTest) {
			/*
			 * so the distance from the origin to the apex should be B
			 */
			double[] p_1 = { 0, 0, 0 };
			System.out.println(l2(p_1, res) + ":" + B);
			/*
			 * and the distance from the point (0,a,0) to the apex should be C
			 */
			double[] p_2 = { a, 0, 0 };
			System.out.println(l2(p_2, res) + ":" + C);
			/*
			 * which it doesn't seem to be!!!
			 */

			System.out.println();
		}
		return res;
	}

	private static double getVolume(double p, double q, double r, double P,
			double Q, double R) {
		CayleyMenger cm1 = new CayleyMenger(p, q, r, P, Q, R);
		return Math.sqrt(cm1.determinant() / 288);
	}

	public static void main1(String[] a) {
		System.out.println(1 / (6 * Math.sqrt(2)));
		System.out.println(getVolume(1, 1, 1, 1, 1, 1));
		System.out.println(8 / (6 * Math.sqrt(2)));
		System.out.println(getVolume(2, 2, 2, 2, 2, 2));
		System.out.println(6);
		System.out.println(getVolume(3, 4, 5, 5, Math.sqrt(18), 3));

	}

	@Override
	public float[] formSimplexF(T p) {
		// TODO Auto-generated method stub
		return null;
	}
}
