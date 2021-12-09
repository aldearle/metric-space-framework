package searchStructures.experimental;

import java.util.ArrayList;
import java.util.List;

import searchStructures.GHMTree;
import searchStructures.SearchIndex;
import coreConcepts.Metric;

/**
 * @author the idea is to create a metric and an indexed data set that can be
 *         used as standard, when we know that most of the distance calculations
 *         will be against a reference set
 *
 */
public class PreCalcDists<T> {

	private List<T> orig_data;
	private List<T> queries;
	private Metric<T> orig_metric;
	private double dists[][];
	private double qdists[][];
	private int noOfRefPoints;
	private int dataUpb;

	/**
	 * Use this constructor when the whole data set can be present
	 * 
	 * @param data
	 * @param metric
	 */
	public PreCalcDists(List<T> data, Metric<T> metric, int refPoints) {
		this.orig_data = data;
		this.orig_metric = metric;
		this.noOfRefPoints = refPoints;
		this.dataUpb = data.size();
		this.dists = new double[data.size()][refPoints];

		//
		// List<Integer> refs = PivotFinder.getPivotIndices(data, metric,
		// leafDepth + 2);
		//
		// for (int i = refs.size() - 1; i >= 0; i--) {
		// int r = refs.get(i);
		// this.referencePoints.add(data.remove(r));
		// }
		//

		List<Integer> rp = PivotFinder.getPivotIndices(data, metric, refPoints);

		for (int i = 0; i < refPoints; i++) {
			for (int j = i + 1; j < data.size(); j++) {
				double d = metric.distance(data.get(i), data.get(j));
				dists[j][i] = d;
				if (j < refPoints) {
					dists[i][j] = d;
				}
			}
		}

	}

	/**
	 * @param queries
	 * @return the first integer which doens't index into the original data set
	 */
	public int addQueries(List<T> queries) {
		this.queries = queries;
		return this.dataUpb;
	}

	public void setQueryDists() {
		this.qdists = new double[this.queries.size()][this.noOfRefPoints];
		for (int i = 0; i < this.queries.size(); i++) {
			for (int j = 0; j < this.noOfRefPoints; j++) {
				qdists[i][j] = this.orig_metric.distance(this.queries.get(i),
						this.orig_data.get(j));
			}
		}
	}

	T getValue(int i) {
		return this.orig_data.get(i);
	}

	public Metric<Integer> pseudoMetric() {
		return new Metric<Integer>() {

			@Override
			public double distance(Integer x, Integer y) {
				if ((x < noOfRefPoints || y < noOfRefPoints)
						&& (x < dataUpb && y < dataUpb)) {
					int i = Math.min(x, y);
					int j = Math.max(x, y);
					return dists[j][i];
				} else {
					T point1 = x < dataUpb ? orig_data.get(x) : queries.get(x
							- dataUpb);
					T point2 = y < dataUpb ? orig_data.get(y) : queries.get(y
							- dataUpb);
					return orig_metric.distance(point1, point2);
				}
			}

			@Override
			public String getMetricName() {
				return "pseudo";
			}
		};

	}

	private static double chebyshev(double[] a, double[] b) {
		double res = Math.abs(a[0] - b[0]);
		for (int i = 1; i < a.length; i++) {
			res = Math.max(res, Math.abs(a[i] - b[i]));
		}
		return res;
	}

	private double surrogate4p(double[] a, double[] b) {
		/*
		 * so these array are distances from the two points to each of a.length
		 * reference points we should consider only the first n of them as this
		 * will be a lot...
		 */
		double maxDist = 0;
		int tries = this.noOfRefPoints / 3;
		for (int refPoint1 = 0; refPoint1 < tries; refPoint1++) {
			// for (int refPoint2 = refPoint1 + 1; refPoint2 < tries;
			// refPoint2++) {
			int refPoint2 = refPoint1 + 1;

			double ab = dists[refPoint1][refPoint2];
			final double aq1 = a[refPoint1];
			final double bq1 = a[refPoint2];

			double x1 = SearchIndex.projectionDistance(ab, aq1, bq1);
			double y1 = SearchIndex.altitude(x1, aq1);

			final double aq2 = b[refPoint1];
			final double bq2 = b[refPoint2];

			double x2 = SearchIndex.projectionDistance(ab, aq2, bq2);
			double y2 = SearchIndex.altitude(x2, aq2);

			double xDiff = x1 - x2;
			double yDiff = y1 - y2;

			double dist = Math.sqrt(xDiff * xDiff + yDiff * yDiff);

			if (!Double.isNaN(dist)) {
				maxDist = Math.max(maxDist, dist);
			}
			// }
		}

		return maxDist;
	}

	public SearchIndex<Integer> surrogateSearchIndex() {
		List<Integer> pdat = new ArrayList<>();
		for (int i = 0; i < this.orig_data.size(); i++) {
			pdat.add(i);
		}
		Metric<Integer> cheby = new Metric<Integer>() {

			@Override
			public double distance(Integer x, Integer y) {
				double[] arr1 = x < dataUpb ? dists[x] : qdists[x - dataUpb];
				double[] arr2 = y < dataUpb ? dists[y] : qdists[y - dataUpb];
				return chebyshev(arr1, arr2);
			}

			@Override
			public String getMetricName() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		GHMTree<Integer> ghmt = new GHMTree<>(pdat, cheby);
		ghmt.setCrExclusionEnabled(true);
		ghmt.setVorExclusionEnabled(true);
		return ghmt;
	}

	public SearchIndex<Integer> surrogateSearchIndex4P() {
		List<Integer> pdat = new ArrayList<>();
		for (int i = 0; i < this.orig_data.size(); i++) {
			pdat.add(i);
		}
		Metric<Integer> cheby = new Metric<Integer>() {

			@Override
			public double distance(Integer x, Integer y) {

				double[] arr1 = x < dataUpb ? dists[x] : qdists[x - dataUpb];
				double[] arr2 = y < dataUpb ? dists[y] : qdists[y - dataUpb];
				return surrogate4p(arr1, arr2);

			}

			@Override
			public String getMetricName() {
				// TODO Auto-generated method stub
				return null;
			}
		};

		GHMTree<Integer> ghmt = new GHMTree<>(pdat, cheby);
		ghmt.setCrExclusionEnabled(true);
		ghmt.setVorExclusionEnabled(true);
		return ghmt;
	}

	public List<Integer> laesaSearch(int query, double threshold) {
		List<Integer> res = new ArrayList<>();
		Metric<Integer> m = pseudoMetric();
		double[] qDists = new double[noOfRefPoints];
		for (int i = 0; i < qDists.length; i++) {
			qDists[i] = m.distance(i, query);
			// if(qDists[i] <= threshold){
			// res.add(i);
			// }
		}
		for (int i = 0; i < orig_data.size(); i++) {
			if (chebyshev(qDists, dists[i]) <= threshold) {
				res.add(i);
			}
		}
		return res;
	}

}
