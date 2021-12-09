package dataPoints.doubleArray;

public class KullbackLieblerBaseE {

	public static double KL(double[] p, double[] q) throws Exception {
		if (p.length != q.length) {
			throw new Exception("different size arguments in KL");
		}
		try {
			double acc = 0;
			for (int i = 0; i < p.length; i++) {
				acc += p[i] * Math.log(p[i] / q[i]);
			}
			return acc;
		} catch (Throwable t) {
			throw new Exception("bad arguments in KL");
		}
	}

	public static double[] merge(double[] p, double[] q) throws Exception {
		if (p.length != q.length) {
			throw new Exception("different size arguments in KL");
		}
		double[] res = new double[p.length];
		for (int i = 0; i < p.length; i++) {
			res[i] = (p[i] + q[i]) / 2;
		}
		return res;
	}

}
