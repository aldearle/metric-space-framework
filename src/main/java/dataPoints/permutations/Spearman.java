package dataPoints.permutations;

import coreConcepts.Metric;

public class Spearman implements Metric<Permutation> {

	@Override
	public double distance(Permutation x, Permutation y) {
		assert x.size() == y.size();

		int acc = 0;
		for (int n = 0; n < x.size(); n++) {
			int xVal = x.get(n);
			int posY = y.indexOf(xVal);
			acc += Math.abs(n - posY);
		}
		return acc;
	}

	@Override
	public String getMetricName() {
		return "Spearman";
	}

}
