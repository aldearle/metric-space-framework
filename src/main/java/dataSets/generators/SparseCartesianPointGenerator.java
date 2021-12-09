package dataSets.generators;

import java.util.Iterator;
import java.util.Random;

import coreConcepts.DataSet;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.sparseCartesian.SparseCartesianPoint;

public class SparseCartesianPointGenerator implements DataSet<CartesianPoint>,
		Iterator<CartesianPoint> {

	protected int dimension;
	private Random rand;
	private boolean gaussian;
	private double zeroProbability;

	/**
	 * @param dimension
	 *            The number of dimensions the generated points will contain
	 * @param density
	 *            The most probable number of dimensions to be non-zero
	 * 
	 * @param rand
	 *            A random number generator to seed the generation, allowing
	 *            repeatable experiments
	 * 
	 * @param gaussian
	 *            Whether the distribution is to be Gaussian or flat for each
	 *            dimension
	 */
	public SparseCartesianPointGenerator(int dimension, int density,
			boolean gaussian) {
		this.dimension = dimension;
		this.rand = new Random(0);
		this.gaussian = gaussian;
		this.zeroProbability = density / (double) dimension;
	}

	@Override
	public CartesianPoint randomValue() {
		return next();
	}

	private boolean coinToss() {
		double r = this.rand.nextDouble();
		return r < this.zeroProbability;
	}

	public CartesianPoint next() {

		double[] point = new double[this.dimension];
		for (int i = 0; i < this.dimension; i++) {
			if (coinToss()) {
				if (this.gaussian) {
					/*
					 * this is a norm dist value with mean zero and sd 1.0, so
					 * convert it to mean 0.5 and sd 0.2
					 */
					double val = this.rand.nextGaussian();
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
					point[i] = this.rand.nextDouble();
				}
			}
		}

		return new CartesianPoint(point);
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
	public Iterator<CartesianPoint> iterator() {
		return this;
	}

	@Override
	public String getDataSetName() {
		String res = "Cartesian " + this.dimension + " dimensions";
		if (this.gaussian) {
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
		return "sparseArrayGen";
	}

}
