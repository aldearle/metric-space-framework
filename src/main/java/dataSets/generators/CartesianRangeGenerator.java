package dataSets.generators;

import java.util.Iterator;

import coreConcepts.DataSet;
import dataPoints.cartesian.CartesianPoint;



public class CartesianRangeGenerator implements DataSet<CartesianPoint>,
		Iterator<CartesianPoint> {

	private double increment;
	private double[] nextPoint;

	public CartesianRangeGenerator() {
		this.increment = 0.01;
		this.nextPoint = new double[2];
		this.nextPoint[0] = 0 - this.increment;
		this.nextPoint[1] = 1 + this.increment;

	}

	@Override
	public boolean hasNext() {
		boolean res = this.nextPoint[1] > 0;
		return res;
	}

	@Override
	public Iterator<CartesianPoint> iterator() {
		return this;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	@Override
	public CartesianPoint next() {

		double[] res = new double[2];
		double newDim0Val = Math.round((this.nextPoint[0] + this.increment) * 1000)
				/ (double) 1000;
		res[0] = newDim0Val;
		double newDim1Val = Math.round((this.nextPoint[1] - this.increment) * 1000)
				/ (double) 1000;
		res[1] = newDim1Val;
		this.nextPoint[0] = newDim0Val;
		this.nextPoint[1] = newDim1Val;

		return new CartesianPoint(res);
	}

	@Override
	public CartesianPoint randomValue() {

		double[] res = new double[2];
		res[0] = Math.random();
		res[1] = 1 - res[0];

		return new CartesianPoint(res);
	}

	@Override
	public String getDataSetName() {
		return "Cartesian 2-dim Range Generator";
	}

	@Override
	public void remove() {
		System.out.println("remove not implemented in " + this.getClass());
	}

	@Override
	public int size() {
		return -1;
	}

	@Override
	public String getDataSetShortName() {
		// TODO Auto-generated method stub
		return "rangeGen";
	}

}
