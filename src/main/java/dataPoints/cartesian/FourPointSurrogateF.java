package dataPoints.cartesian;

import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Scanner;

import coreConcepts.Metric;

public class FourPointSurrogateF<T extends CartesianPoint> implements
		Metric<float[]> {

	// will always access this in same order so single n \choose 2 vector is
	// fine
	private float[] distanceTable;
	private List<T> refPoints;
	private Metric<T> metric;

	/**
	 * Takes as input a metric and a list of reference points; stores the table
	 * of all distances between all pairs
	 * 
	 * when used to calculate a distance the input needs to be two vectors of
	 * distances, each distance to the same reference point in the same order
	 * 
	 * so... to use this metric: (a) choose reference points, (b) calculate
	 * distances to data set, (c) create this metric
	 * 
	 * @param m
	 * @param refPoints
	 */
	public FourPointSurrogateF(Metric<T> m, List<T> refPoints) {
		this.refPoints = refPoints;
		this.metric = m;
		int nChoose2 = refPoints.size() * (refPoints.size() - 1) / 2;
		this.distanceTable = new float[nChoose2];
		int ptr = 0;
		for (int i = 0; i < refPoints.size() - 1; i++) {
			for (int j = i + 1; j < refPoints.size(); j++) {
				distanceTable[ptr++] = (float) m.distance(refPoints.get(i),
						refPoints.get(j));
			}
		}
		assert ptr == distanceTable.length : "distance table allocation";
	}

	@Override
	public double distance(float[] xs, float[] ys) {
		double max = 0;
		int ptr = 0;
		for (int a = 0; a < refPoints.size() - 1; a++) {
			for (int b = a + 1; b < refPoints.size(); b++) {
				double x0 = offset(distanceTable[ptr], xs[a], xs[b]);
				double x1 = altitude(x0, xs[a]);
				double y0 = offset(distanceTable[ptr++], ys[a], ys[b]);
				double y1 = altitude(y0, ys[a]);

				double diff1 = x0 - y0;
				double diff2 = x1 - y1;
				double dist = Math.sqrt(diff1 * diff1 + diff2 * diff2);

				max = Math.max(max, dist);
			}
		}
		return max;
	}

	@Override
	public String getMetricName() {
		return "FourPointSurrogateF";
	}

	/**
	 * x offset from point a
	 * 
	 * @param ab
	 * @param aq
	 * @param bq
	 * @return
	 */
	private static double offset(double ab, double aq, double bq) {
		return (ab / 2) + (aq * aq - bq * bq) / (2 * ab);
	}

	private static double altitude(double offset, double aq) {
		return Math.sqrt(aq * aq - offset * offset);
	}

	public float[] getDists(T p) {
		float[] res = new float[this.refPoints.size()];
		int ptr = 0;
		for (T r : this.refPoints) {
			res[ptr++] = (float) this.metric.distance(p, r);
		}
		return res;
	}

}
