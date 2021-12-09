package dataPoints.permutations;

import coreConcepts.Metric;

public class IdenticalPlaces implements Metric<Permutation> {

	private int threshold;

	public IdenticalPlaces(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * note that this is an asymmetric implementation, if the query (assumed
	 * first parameter) is greater than the threshold then nothing is added to
	 * the accumulator. This doesn't however mean that the function is
	 * asymmetric.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	@SuppressWarnings("boxing")
	@Override
	public double distance(Permutation x, Permutation y) {
		assert x.size() == y.size();

		int acc = 0;
		for (int n = 0; n < x.size(); n++) {
			int xVal = x.get(n);
			if (xVal <= this.threshold) {
				int yVal = y.get(n);
				if (xVal != yVal) {
					acc++;
				}
			}
		}
		return acc;
	}

	@Override
	public String getMetricName() {
		return "Spearman";
	}

}
