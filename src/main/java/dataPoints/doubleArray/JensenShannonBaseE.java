package dataPoints.doubleArray;

import coreConcepts.Metric;

public class JensenShannonBaseE implements Metric<double[]> {

	@Override
	public double distance(double[] x, double[] y) {
		try {
			double[] m = KullbackLieblerBaseE.merge(x, y);
			return (KullbackLieblerBaseE.KL(x, m) + KullbackLieblerBaseE.KL(y,
					m));
		} catch (Exception e) {
			System.out.println("that shouldn't have gone wrong");
			return 0;
		}
	}

	@Override
	public String getMetricName() {
		return "JSBaseE";
	}

}
