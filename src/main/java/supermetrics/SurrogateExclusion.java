package supermetrics;

import java.util.ArrayList;
import java.util.List;

import n_point_surrogate.SimplexExclusion;
import n_point_surrogate.SimplexND;
import searchStructures.GHTree;
import searchStructures.SearchIndex;
import searchStructures.VPTree;
import testloads.TestContext;
import testloads.TestContext.Context;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;

public class SurrogateExclusion<T> {

	protected List<SimplexExclusion<T>> simplexes;
	protected List<T> refPoints;
	protected List<T> data;
	protected Metric<T> metric;
	protected List<double[]> surrogates;
	protected SimplexND<T> sim;
	protected SearchIndex<double[]> vpt;
	protected CountedMetric<double[]> sMetric;

	SurrogateExclusion(List<T> refPoints, List<T> data, Metric<T> metric)
			throws Exception {
		this.refPoints = refPoints;
		this.data = data;
		this.metric = metric;
		this.simplexes = new ArrayList<>();

		this.sim = new SimplexND(refPoints.size(), metric, refPoints);
		this.surrogates = new ArrayList<>();
		for (T p : data) {
			double[] apex = this.sim.formSimplex(p);
			this.surrogates.add(apex);
		}

		Metric<double[]> l2 = new Metric<double[]>() {
			@Override
			public double distance(double[] x, double[] y) {
				return SimplexExclusion.l2(x, y);
			}

			@Override
			public String getMetricName() {
				return "l2";
			}
		};
		this.sMetric = new CountedMetric<>(l2);
		this.vpt = new GHTree<>(surrogates, this.sMetric, true);
		this.sMetric.reset();
	}

	public int metricCalls() {
		return this.sMetric.reset();
	}

	public List<double[]> query(T query, double threshold) {
		double[] s = this.sim.formSimplex(query);
		return this.vpt.thresholdSearch(s, threshold);
	}

	public static void main(String[] a) throws Exception {
		TestContext tc = new TestContext(Context.euc20, 100 * 1000);
		int noOfQueries = 1000;
		int dimension = 15;
		tc.setSizes(noOfQueries, dimension);

		System.out.println("original space dimensions: "
				+ tc.getQueries().get(0).getPoint().length);

		SurrogateExclusion<CartesianPoint> se = new SurrogateExclusion<>(
				tc.getRefPoints(), tc.getDataCopy(), tc.metric());

		CountedMetric<CartesianPoint> cm = new CountedMetric<>(tc.metric());
		SearchIndex<CartesianPoint> vpt = new GHTree(tc.getDataCopy(), cm, true);
		cm.reset();

		List<List<double[]>> resultsA = new ArrayList<>();
		List<List<CartesianPoint>> resultsB = new ArrayList<>();
		for (CartesianPoint q : tc.getQueries()) {
			List<double[]> res = se.query(q, tc.getThreshold());
			List<CartesianPoint> trueRes = vpt.thresholdSearch(q,
					tc.getThreshold());

			resultsA.add(res);
			resultsB.add(trueRes);
		}
		System.out.println("no of results per query: "
				+ (float) resultSize(resultsA) / noOfQueries + ":"
				+ (float) resultSize(resultsB) / noOfQueries);
		System.out.println("metric calls per query: " + se.metricCalls()
				/ noOfQueries + ":" + cm.reset() / noOfQueries);
	}

	private static <T> int resultSize(List<List<T>> l) {
		int res = 0;
		for (List<T> l0 : l) {
			res += l0.size();
		}
		return res;
	}

}
