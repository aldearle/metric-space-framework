package supermetrics;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;

import testloads.CartesianThresholds;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Cosine;
import dataPoints.cartesian.CosineNormalised;
import dataPoints.cartesian.Euclidean;

public class CayleyMenger {

	double[][] matrix;

	/**
	 * parameters for this in trad style for tet with edges p,q,r,P,Q,R,
	 * capitals opposite lowercase, are:
	 * 
	 * @param t
	 *            is p
	 * @param bs
	 *            is q
	 * @param bq
	 *            is r
	 * @param ab
	 *            is P
	 * @param aq
	 *            is Q
	 * @param as
	 *            is R
	 */
	public CayleyMenger(double t, double bs, double bq, double ab, double aq,
			double as) {
		this.matrix = new double[5][5];

		this.matrix[0][1] = 1;
		this.matrix[0][2] = 1;
		this.matrix[0][3] = 1;
		this.matrix[0][4] = 1;
		this.matrix[1][2] = ab * ab;
		this.matrix[1][3] = as * as;
		this.matrix[1][4] = aq * aq;
		this.matrix[2][3] = bs * bs;
		this.matrix[2][4] = bq * bq;
		this.matrix[3][4] = t * t;

		for (int i = 0; i < 4; i++) {
			for (int j = i + 1; j < 5; j++) {
				this.matrix[j][i] = this.matrix[i][j];
			}
		}

	}

	protected void printMatrix() {
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				System.out.print(this.matrix[i][j] + "\t");
			}
			System.out.println();
		}
	}

	public double determinant() {
		Array2DRowRealMatrix m = new Array2DRowRealMatrix(this.matrix);
		LUDecomposition l = new LUDecomposition(m);
		return l.getDeterminant();
	}

	public static void testMetric() {
		TestLoad tl = new TestLoad(3, 20, false);
		List<CartesianPoint> ps = tl.getDataCopy();
		Metric<CartesianPoint> euc = new Euclidean<>();
		Metric<CartesianPoint> cos1 = new Cosine<>();
		Metric<CartesianPoint> cos2 = new CosineNormalised<>();
		Metric<CartesianPoint> m = cos2;
		/*
		 * try every selection of four points
		 */
		int perms = 0;
		for (int i = 0; i < ps.size() - 3; i++) {
			CartesianPoint p1 = ps.get(i);
			for (int j = i + 1; j < ps.size() - 2; j++) {
				CartesianPoint p2 = ps.get(j);
				double ab = m.distance(p1, p2);
				for (int k = j + 1; k < ps.size() - 1; k++) {
					CartesianPoint p3 = ps.get(k);
					double aq = m.distance(p1, p3);
					double bq = m.distance(p2, p3);

					for (int l = k + 1; l < ps.size(); l++) {
						CartesianPoint p4 = ps.get(l);
						double as = m.distance(p1, p4);
						double bs = m.distance(p2, p4);
						double t = m.distance(p3, p4);

						CayleyMenger cm = new CayleyMenger(t, bs, bq, ab, aq,
								as);
						System.out
								.print(Math.min(
										Math.min(Math.min(
												Math.min(Math.min(t, bs), bq),
												ab), aq), as));
						System.out.println("\t" + cm.determinant());
						// if (cm.determinant() < 0) {
						// System.out.println("got a baddy");
						// System.out.print(p1);
						// System.out.print(p2);
						// System.out.print(p3);
						// System.out.println(p4);
						// }
						perms++;
					}
				}
			}
		}
	}

	public static long factorial(int n) {
		if (n == 0) {
			return 1;
		} else {
			return (n * factorial(n - 1));
		}
	}

	public static long choose(int n, int k) {
		return factorial(n) / (factorial(n - k) * factorial(k));
	}

	public static void main(String[] args) throws Exception {
		SisapFile f = TestLoad.SisapFile.colors;
		double thresh = TestLoad.getSisapThresholds(f)[0];
		TestLoad tl = new TestLoad(10, 1003, false);
		List<CartesianPoint> dat = tl.getDataCopy();
		Metric<CartesianPoint> euc = new Euclidean<>();

		CartesianPoint a = dat.remove(0);
		CartesianPoint b = dat.remove(0);
		CartesianPoint s = dat.remove(0);

		double ab = euc.distance(a, b);
		double as = euc.distance(a, s);
		double bs = euc.distance(b, s);
		double[] vols = new double[1000];

		for (int ptr = 0; ptr < 1000; ptr++) {
			CartesianPoint q = dat.get(ptr);
			double aq = euc.distance(a, q);
			double bq = euc.distance(b, q);
			double t = euc.distance(q, s);
			CayleyMenger cm = new CayleyMenger(t, bs, bq, ab, aq, as);
			vols[ptr] = cm.determinant();
		}

		for (double v : vols) {
			System.out.println(v);
		}

		System.out.println("thresh:"
				+ CartesianThresholds.getThreshold("euc", 10, 1));
		System.out.println("thresh:" + thresh);
	}
}
