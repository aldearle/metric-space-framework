package dataPoints.cartesian;

import util.Util;
import coreConcepts.Metric;
import dataPoints.doubleArray.JSDoubleArray;

/**
 * @author Richard Connor
 * 
 */
public class JensenShannonViaSed<T extends CartesianPoint> implements Metric<T> {

	private static JSDoubleArray js = new JSDoubleArray();
	Metric<T> sed;

	public JensenShannonViaSed() {
		this.sed = new SEDByComplexity<T>();
	}

	@Override
	public double distance(T x, T y) {
		double d = sed.distance(x, y);
		Util.jsToSed(d);
		return Util.sedToJs(d);
	}

	@Override
	public String getMetricName() {
		return "jsd";
	}

}
