package dataSets.generators;

import java.util.Iterator;
import java.util.Random;

import coreConcepts.DataSet;
import dataPoints.cartesian.CartesianPoint;

public class DoubleArrayGenerator implements DataSet<double[]>,
		Iterator<double[]> {

	protected int dimension;
	private Random rand;
	private boolean gaussian;

	/**
	 * @param dimension
	 *            The number of dimensions the generated points will contain
	 * 
	 * 
	 * @param gaussian
	 *            Whether the distribution is to be Gaussian or flat for each
	 *            dimension
	 */
	public DoubleArrayGenerator(int dimension, boolean gaussian) {
		this.dimension = dimension;
		this.rand = new Random(0);
		this.gaussian = gaussian;
	}

	@Override
	public double[] randomValue() {
		return next();
	}

	public double[] next() {

		double[] point = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			if (gaussian) {
				/*
				 * this is a norm dist value with mean zero and sd 1.0, so
				 * convert it to mean 0.5 and sd 0.2
				 */
				double val = rand.nextGaussian();
				// compress it so most value are in the range
				val = val / 5;
				val += 0.5;
				if (val < 0) {
					val = 0;
				} else if (val > 1) {
					val = 1;
				}
				point[i] = val;
			} else {
				point[i] = rand.nextDouble();
			}
		}

		return point;
	}

	@Override
	public boolean isFinite() {
		return false;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public void remove() {
		System.out.println("remove not implemented in " + this.getClass());
	}

	@Override
	public Iterator<double[]> iterator() {
		return this;
	}

	@Override
	public String getDataSetName() {
		String res = "double array " + dimension + " dimensions";
		if (gaussian) {
			res += " (Gaussian)";
		} else {
			res += " (Non-Gaussian)";
		}
		return res;
	}

	@Override
	public int size() {
		return -1;
	}

	@Override
	public String getDataSetShortName() {
		// TODO Auto-generated method stub
		return "arrayGen";
	}

}