package util;

import java.util.Random;

/**
 * @author Richard
 * 
 *         a unit hypersphere
 * 
 */
public class Hypersphere {

	public static double eqSideHalfLength(int dim) {
		double sphVol = getVolume(dim);

		double side = (Math.pow(sphVol, (double) 1 / dim)) / 2;

		return side;
	}

	public static double getVolume(int dimensions) {

		assert dimensions % 2 == 0;
		int k = dimensions / 2;

		double topLine = Math.pow(pi, k);

		return topLine / factorial(k);
	}

	/**
	 * @return a normally distributed number between -1 and 1 inc, mean 0, sd
	 *         one-third
	 */
	public double nextGauss() {
		double g = 1.1;
		while (g < -1 || g > 1) {
			g = rand.nextGaussian() / 3;
		}
		return g;
	}

	public static void main(String[] args) {

		for (int i : Range.range(2, 20)) {
			if (i % 2 == 0) {
				System.out.println(getVolume(i));
			}
		}
		Hypersphere h = new Hypersphere(10);

		printCubePointsInSphere(h);

	}

	private static void printSpherePointsInCube(Hypersphere h) {
		for (double cubeHalfSide = 0; cubeHalfSide < 1; cubeHalfSide += 0.1) {
			double in = 0;
			for (int i = 0; i < 5000; i++) {
				double[] point = h.genPointInSphere(false, 1.0);

				double maxDimPlus = 0;
				double maxDimMinus = 0;
				for (double dim : point) {
					maxDimPlus = Math.max(maxDimPlus, dim);
					maxDimMinus = Math.min(maxDimMinus, dim);
				}
				if (maxDimPlus < cubeHalfSide) {
					in++;
				}
			}
			System.out.println(cubeHalfSide + "\t" + in / (float) 5000);
		}
	}

	private static void printCubePointsInSphere(Hypersphere h) {
		final int noOfRefPoints = 10000;
		for (float cubeHalfSide = 0; cubeHalfSide <= 1.001; cubeHalfSide += 0.01) {
			double in = 0;
			for (int i = 0; i < noOfRefPoints; i++) {
				double[] point = h.genPointInCube(false, cubeHalfSide);

				if (distanceFromOrigin(point) <= 1.0) {
					in++;
				}
			}
			System.out
					.println(cubeHalfSide + "\t" + in / (float) noOfRefPoints);
		}
	}

	private static void getCubePointsInSphere(Hypersphere h) {
		for (int testSide = 0; testSide <= 100; testSide++) {
			int cubePointsInSphere = 0;
			double thisRad = testSide * (1 / (float) 100);
			int testNo = 10000;
			for (int i = 0; i < testNo; i++) {
				double[] point = h.genPointInCube(false, thisRad);
				if (h.distanceFromOrigin(point) <= 1) {
					cubePointsInSphere++;
				}
			}
			System.out.println(testSide + "\t" + thisRad + "\t"
					+ cubePointsInSphere / (float) testNo);
		}
	}

	private static void testOverlaps(Hypersphere h) {
		int spherePointsInCube = 0;
		int cubePointsInSphere = 0;

		boolean gaussian = false;

		for (int i = 0; i < 1000000; i++) {
			double[] point = h.genPointInCube(gaussian, h.eqCubeSideHalfLength);
			if (h.distanceFromOrigin(point) <= 1) {
				cubePointsInSphere++;
			}
		}

		System.out.println(cubePointsInSphere + " cube points were in sphere");

		for (int i = 0; i < 1000; i++) {
			double[] point = h.genPointInSphere(gaussian, 1);
			if (h.pointIsInHypercube(point)) {
				spherePointsInCube++;
			}
			// System.out.println(i + " points generated, " + spherePointsInCube
			// + " in cube");
		}

		System.out.println(spherePointsInCube + " sphere points were in cube");
	}

	public static double volRatio(int dim) {
		assert dim % 2 == 0;
		int k = dim / 2;
		double top = Math.pow(pi, k);
		double bottom = Math.pow(2, dim);
		double temp = top / bottom;
		temp = temp / factorial(k);
		return temp;
	}

	int dimensions;

	double eqCubeSideHalfLength;

	double volume;

	Random rand;

	static double pi = Math.PI;

	public static long factorial(long n) {
		if (n == 1) {
			return 1;
		} else {
			return n * factorial(n - 1);
		}
	}

	private static void print(double[] ds) {
		for (double d : ds) {
			System.out.print(d + ", ");
		}
		System.out.println();
	}

	Hypersphere(int dimensions) {
		assert dimensions % 2 == 0;
		this.dimensions = dimensions;
		rand = new Random(0);

		this.volume = Hypersphere.getVolume(dimensions);
		this.eqCubeSideHalfLength = eqSideHalfLength(dimensions);

	}

	public static double distanceFromOrigin(double[] p) {
		double acc = 0;
		for (double d : p) {
			acc += d * d;
		}
		return Math.sqrt(acc);
	}

	public boolean pointIsInHypercube(double[] p) {
		boolean inside = true;
		for (double d : p) {
			if (inside && Math.abs(d) > this.eqCubeSideHalfLength) {
				inside = false;
			}
		}
		return inside;
	}

	/**
	 * @param gaussian
	 * @return a randomly generated point in a unit hypersphere centred at the
	 *         origin
	 */
	public double[] genPointInSphere(boolean gaussian, double radius) {
		/*
		 * should use polar coordinates for more efficient generation, but this
		 * will do for now!
		 */

		boolean foundOne = false;
		double[] res = new double[dimensions];
		while (!foundOne) {
			res = this.genPointInCube(gaussian, 1);
			foundOne = distanceFromOrigin(res) <= radius;
		}
		return res;
	}

	public double[] genPointInSpherePolar(double radius) {
		/*
		 * should use polar coordinates for more efficient generation, but this
		 * will do for now!
		 */
		double[] angles = new double[dimensions - 1];
		for (int i = 0; i < angles.length; i++) {
			angles[i] = rand.nextDouble() * (Math.PI / 2);
		}
		double magnitude = rand.nextDouble();

		return polarToCartesian(magnitude, angles);

	}

	private double[] genPointInSphereMarsaglia(double radius) {

		double magnitude = radius * rand.nextDouble();
		double[] point = genPointInCube(true, 1);
		double dist = distanceFromOrigin(point);
		for (int i = 0; i < point.length; i++) {
			point[i] = ((1 / dist) * point[i]);
		}

		return point;
	}

	private double[] polarToCartesian(double magnitude, double[] angles) {
		double[] res = new double[angles.length + 1];

		double[] sines = new double[angles.length];
		double[] coses = new double[angles.length];
		for (int i = 0; i < sines.length; i++) {
			sines[i] = Math.sin(angles[i]);
			coses[i] = Math.cos(angles[i]);
		}

		for (int i = 0; i < res.length; i++) {
			double sinProd = 1;
			for (int j = 0; j < i - 1; j++) {
				sinProd = sinProd * sines[j];
			}
			if (i != angles.length) {
				sinProd = sinProd * coses[i];
			}
			res[i] = sinProd * magnitude;
		}
		return res;
	}

	/**
	 * @param gaussian
	 * @param halfCubeSide
	 * @return a randomly generated point in a hypercube centered at the origin
	 */
	public double[] genPointInCube(boolean gaussian, double halfCubeSide) {
		double[] res = new double[dimensions];
		for (int i = 0; i < res.length; i++) {
			double r;
			if (gaussian) {
				r = this.nextGauss() * halfCubeSide;
			} else {
				r = rand.nextDouble() * halfCubeSide;
				// boolean neg = rand.nextBoolean();
				// if (neg) {
				// r = -r;
				// }
			}

			res[i] = r;
		}
		rand.nextDouble();
		return res;
	}

}
