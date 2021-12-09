package dataPoints.sparseCartesian;

import java.util.Iterator;

import util.OrderedList;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.compactEnsemble.CompactEnsemble;
import dataPoints.compactEnsemble.EventToIntegerMap;

/**
 * 
 * implements a normalised, sparse carrtesian point, represented as a vector of
 * integers which are the non-zero dimensions, and a vector of doubles which are
 * the corresponding values
 * 
 * @author Richard Connor
 * 
 */
public class SparseCartesianPoint implements SparseCartesian {

	private int[] dimensions;
	private double[] values;

	/**
	 * @param p
	 * 
	 */
	public SparseCartesianPoint(CartesianPoint p) {
		int nonZeros = 0;
		final double[] normalisedPoint = p.getNormalisedPoint();
		for (double d : normalisedPoint) {
			if (d != 0) {
				nonZeros++;
			}
		}

		this.dimensions = new int[nonZeros];
		this.values = new double[nonZeros];

		int nzPntr = 0;
		for (int i = 0; i < normalisedPoint.length; i++) {
			double d = normalisedPoint[i];
			if (d != 0) {
				this.dimensions[nzPntr] = i;
				this.values[nzPntr++] = d;
			}
		}
	}

	/**
	 * @param ce
	 */
	public SparseCartesianPoint(CompactEnsemble ce) {
		double card = (double) ce.getCardinality();
		int[] dims = ce.getEnsemble();

		this.dimensions = new int[dims.length];
		this.values = new double[dims.length];

		int pntr = 0;
		for (int compound : dims) {
			this.dimensions[pntr] = EventToIntegerMap.getEventCode(compound);
			this.values[pntr] = EventToIntegerMap.getCard(compound) / card;
			pntr++;
		}
	}

	/**
	 * Used to create points from the raw data
	 * 
	 * @param dims
	 * @param vals
	 */
	public SparseCartesianPoint(int[] dims, double[] vals) {
		this.dimensions = dims;
		this.values = vals;
	}

	/**
	 * this reorders the dimensions and values such that the greatest values
	 * come first in the arrays
	 */
	@SuppressWarnings("boxing")
	public void reorder() {
		OrderedList<Integer, Double> ol = new OrderedList<Integer, Double>(
				this.dimensions.length);
		for (int i = 0; i < this.dimensions.length; i++) {
			ol.add(this.dimensions[i], this.values[i]);
		}
		Iterator<Integer> orderedDims = ol.getList().iterator();
		Iterator<Double> orderedVals = ol.getComparators().iterator();
		for (int i = this.dimensions.length - 1; i >= 0; i--) {
			this.dimensions[i] = orderedDims.next();
			this.values[i] = orderedVals.next();
		}
	}

	@Override
	public int[] getDims() {
		return this.dimensions;
	}

	@Override
	public double[] getValues() {
		return this.values;
	}

}
