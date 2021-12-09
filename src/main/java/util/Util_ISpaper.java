package util;

import coreConcepts.Metric;

import java.util.*;

public class Util_ISpaper {
	private static Random rand = new Random();

	public static <T> boolean isSubset(List<T> big, List<T> small) {
		boolean res = true;
		for (T item : small) {
			if (!big.contains(item)) {
				res = false;
			}
		}
		return res;
	}

	public static <T> boolean isSet(List<T> data) {
		Set<T> s = new HashSet<>();
		for (T item : data) {
			s.add(item);
		}
		return data.size() == s.size();
	}

	public static <T> T getOutlier(List<T> data, Metric<T> metric,
			int iterations) {
		assert data.size() > 0;
		if (data.size() == 1) {
			return data.get(0);
		} else {
			Set<T> used = new HashSet<>();
			int maxIterations = iterations;
			T x = data.get(rand.nextInt(data.size()));
			used.add(x);
			while (maxIterations-- > 0) {
				int bestY = -1;
				double maxDist = 0;
				for (int i : new Range(0, data.size())) {
					T datum = data.get(i);
					if (!used.contains(datum)) {
						double d = metric.distance(x, datum);
						if (d > maxDist) {
							bestY = i;
							maxDist = d;
						}
					}
				}
				if (bestY != -1) {
					x = data.get(bestY);
					used.add(x);
				}
			}
			return x;
		}

	}

	/**
	 * @param req
	 *            the size of the list
	 * @param outOf
	 *            maximum value, exclusive
	 * @return a set of integers greater than or equal to zero
	 */
	@SuppressWarnings("boxing")
	public static Set<Integer> getRandomInts(int req, int outOf) {
		assert req < outOf : "wrong numbers given in getRandomInts";
		boolean collectComplement = req >= outOf / 2 ? true : false;
		if (collectComplement) {
			req = outOf - req;
		}
		Set<Integer> res = new HashSet<>();
		while (res.size() < req) {
			res.add(rand.nextInt(outOf));
		}
		if (!collectComplement) {
			return res;
		} else {
			Set<Integer> realRes = new HashSet<>();
			for (int i : Range.range(0, outOf)) {
				if (!res.contains(i)) {
					realRes.add(i);
				}
			}
			return realRes;
		}
	}

	/**
	 * @param data
	 *            from which to choose the new list
	 * @param noOfPoints
	 *            number to select
	 * @return a new list containing a random selection of elements of data,
	 *         which is not changed by side-effect
	 */
	public static <T> List<T> getRandom(List<T> data, int noOfPoints) {
		if (noOfPoints >= data.size()) {
			return data;
		} else {
			Set<Integer> indexes = getRandomInts(noOfPoints, data.size());
			List<T> res = new ArrayList<>();
			for (int i : indexes) {
				res.add(data.get(i));
			}
			assert res.size() == noOfPoints : "wrong number of data points in getRandom";
			return res;
		}
	}

	public static <T> List<T> getFFT(List<T> data, Metric<T> metric,
			int noOfPoints) {
		if (noOfPoints >= data.size()) {
			return data;
		} else {
			List<T> pivotSet = new ArrayList<>();
			pivotSet.add(data.get(rand.nextInt(data.size())));
			while (pivotSet.size() < noOfPoints) {
				double max = 0;
				int furthest = -1;
				for (int i : new Range(0, data.size())) {
					T d = data.get(i);
					if (!pivotSet.contains(d)) {
						double min = Double.MAX_VALUE;
						for (T n : pivotSet) {
							double dist = metric.distance(d, n);
							if (dist < min) {
								min = dist;
							}
						}
						if (min >= max) {
							max = min;
							furthest = i;
						}
					}
				}
				pivotSet.add(data.get(furthest));
			}
			assert pivotSet.size() == noOfPoints : "required:  " + noOfPoints
					+ "; got " + pivotSet.size();
			return pivotSet;
		}

	}

	public static <T> List<T> getFFTInverse(List<T> data, Metric<T> metric,
			int noOfPoints) {
		if (noOfPoints >= data.size()) {
			return data;
		} else {
			List<T> res = new ArrayList<>();
			res.add(data.get(rand.nextInt(data.size())));
			while (res.size() < noOfPoints) {
				double minN = Double.MAX_VALUE;
				int furthest = -1;
				for (int i : new Range(0, data.size())) {
					T d = data.get(i);
					if (!res.contains(d)) {
						double maxN = 0;
						for (T n : res) {
							double dist = metric.distance(d, n);
							if (dist != 0 && dist >= maxN) {
								maxN = dist;
							}
						}
						if (maxN <= minN) {
							minN = maxN;
							furthest = i;
						}
					}
				}
				res.add(data.get(furthest));
			}
			assert res.size() == noOfPoints : "required:  " + noOfPoints
					+ "; got " + res.size();
			return res;
		}

	}

	public static <T> double minPivot(List<T> data, Metric<T> m) {
		double res = Double.MAX_VALUE;
		for (T d1 : data) {
			for (T d2 : data) {
				if (d1 != d2) {
					res = Math.min(res, m.distance(d1, d2));
				}
			}
		}
		return res;
	}

	public static <T> double maxPivot(List<T> data, Metric<T> m) {
		double res = 0;
		for (T d1 : data) {
			for (T d2 : data) {
				if (d1 != d2) {
					res = Math.max(res, m.distance(d1, d2));
				}
			}
		}
		return res;
	}
}
