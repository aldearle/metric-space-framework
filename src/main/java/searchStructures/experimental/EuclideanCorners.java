package searchStructures.experimental;

import java.util.List;

import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import dataPoints.cartesian.CartesianPoint;

public class EuclideanCorners {

	private int dims;
	private int phase;
	private int phase2;
	double[] minsPerDim;
	double[] maxsPerDim;

	public EuclideanCorners(List<CartesianPoint> data) {
		this.dims = data.get(0).getPoint().length;
		this.minsPerDim = new double[this.dims];
		for (int i = 0; i < this.dims; i++) {
			minsPerDim[i] = Double.MAX_VALUE;
		}
		this.maxsPerDim = new double[this.dims];
		setBounds(data);
		this.phase = 0;
		this.phase2 = 0;
	}

	private void setBounds(List<CartesianPoint> data) {
		for (CartesianPoint p : data) {
			double[] ds = p.getPoint();
			int ptr = 0;
			for (double d : ds) {
				minsPerDim[ptr] = Math.min(minsPerDim[ptr], d);
				maxsPerDim[ptr] = Math.max(maxsPerDim[ptr], d);
				ptr++;
			}
		}

	}

	public double[] nextCorner() {
		double[] res = minsPerDim.clone();
		res[phase] = maxsPerDim[phase];
		phase++;
		if (phase == res.length) {
			minsPerDim[phase2] = maxsPerDim[phase2++];
			phase = phase2 + 1;
		}
		return res;
	}

	public static void main(String[] a) throws Exception {
		final SisapFile sisFile = SisapFile.colors;
		// colors is 112 dimensions
		TestLoad colors = new TestLoad(sisFile);
		double[] thresh = TestLoad.getSisapThresholds(sisFile);

		EuclideanCorners ec = new EuclideanCorners(colors.getDataCopy());
		int ptr = 0;
		for (double d : ec.minsPerDim) {
			System.out.print(d + "\t");
			System.out.println(ec.maxsPerDim[ptr++]);
		}
	}
}
