package dataPoints.permutations;

public class QuantisedPermutation extends Permutation {

	public QuantisedPermutation(double[] distances, int numberofBuckets) {
		super(distances);

		int length = this.size();
		for (int i = 0; i < length; i++) {
			int n = this.get(i);
			int q = n / numberofBuckets;
			this.set(i, q);
		}
	}

	public static void main(String[] b) {

		Permutation p = new Permutation(dists);
		QuantisedPermutation q = new QuantisedPermutation(dists, 10);

		Spearman s = new Spearman();
		IdenticalPlaces id = new IdenticalPlaces(10);

		System.out.println(id.distance(p, p));
		//
		// System.out.println(p);
		// System.out.println(q);
	}

}
