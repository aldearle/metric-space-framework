package searchStructures;

import java.util.ArrayList;
import java.util.List;

import n_point_surrogate.SimplexND;
import testloads.TestLoad;
import util.OrderedList;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

public class ListOfClusters<T> extends SearchIndex<T> {

	private int clusterSize;
	private ListNode entryPoint;
	private boolean greatestFirst;

	class ListNode {

		T head, previousHead;
		double cr;
		List<T> cluster;
		ListNode next;
		SimplexND<T> simp;
		double maxY;
		double maxX;
		double minX;

		@SuppressWarnings("boxing")
		ListNode(T head, T previousHead, List<T> data) {

			int dSize = data.size();
			this.head = head;
			if (previousHead != null) {
				this.minX = Double.MAX_VALUE;
				try {
					this.simp = new SimplexND<>(2, metric, head, previousHead);
				} catch (Exception e) {
					throw new RuntimeException("couldn't create simplex in loc");
				}
			}
			this.cr = 0;
			if (data.size() <= clusterSize) {
				for (T d : data) {
					final double distance = metric.distance(head, d);
					if (distance > this.cr) {
						this.cr = distance;
					}
				}
				this.cluster = data;
			} else {
				OrderedList<T, Double> ol = new OrderedList<>(clusterSize);
				Double threshold = null;
				for (T d : data) {
					final double distance = metric.distance(d, head);
					if (threshold == null || distance <= threshold) {
						ol.add(d, distance);
						threshold = ol.getThreshold();
					}
				}
				this.cluster = ol.getList();
				this.cr = ol.getThreshold();
				for (T d : this.cluster) {
					data.remove(d);
					// this.cr = Math.max(this.cr, metric.distance(d,
					// this.head));
				}
				T nextHead = data.remove(0);
				this.next = new ListNode(nextHead, head, data);
			}
			if (this.simp != null) {
				for (T d : this.cluster) {
					final double distance = metric.distance(head, d);
					final double preDist = metric.distance(previousHead, d);
					double[] dists = { distance, preDist };
					double[] ap = this.simp.formSimplex(dists);
					this.maxX = Math.max(this.maxX, ap[0]);
					this.minX = Math.min(this.minX, ap[0]);
					this.maxY = Math.max(this.maxY, ap[1]);
				}
			}
		}

		public int cardinality() {
			if (this.next == null) {
				return 1 + this.cluster.size();
			} else {
				return 1 + this.cluster.size() + this.next.cardinality();
			}
		}

		public void thresholdQuery(T q, double threshold, List<T> res,
				double previousHdistance) {

			double qTohDist = metric.distance(q, head);
			boolean excludeFp = false;

			if (qTohDist <= this.cr + threshold) {

				// silly technicality; this is almost free, but can only be true
				// if we're in this code...
				if (qTohDist <= threshold) {
					res.add(this.head);
				}

				if (this.simp != null) {
					double[] dists = { qTohDist, previousHdistance };
					double[] ap = this.simp.formSimplex(dists);
					if (ap[0] > this.maxX + threshold) {
						excludeFp = true;
					} else if (ap[0] < this.minX - threshold) {
						excludeFp = true;
					} else if (ap[1] > this.maxY + threshold) {
						excludeFp = true;
					}
				}

				if (!excludeFp) {
					for (T d : this.cluster) {
						if (metric.distance(d, q) <= threshold) {
							res.add(d);
						}
					}
				}

			}

			if (this.next != null) {
				if (qTohDist >= this.cr - threshold) {
					this.next.thresholdQuery(q, threshold, res, qTohDist);
				}
			}
		}
	}

	public ListOfClusters(List<T> data, Metric<T> metric, int clustersize) {
		super(data, metric);
		// start with a stupid algorithm and take it from there...
		this.clusterSize = clustersize;
		this.setGreatestFirst(false);

		T head = data.remove(0);
		this.entryPoint = new ListNode(head, null, data);

	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		this.entryPoint.thresholdQuery(query, t, res, 0);
		return res;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return "loc_" + this.clusterSize;
	}

	public static void main(String[] a) throws Exception {

		TestLoad.SisapFile testfile = TestLoad.SisapFile.colors;
		TestLoad t = new TestLoad(testfile);
		Metric<CartesianPoint> euc = new Euclidean<>();
		CountedMetric<CartesianPoint> m1 = new CountedMetric<>(euc);
		CountedMetric<CartesianPoint> m2 = new CountedMetric<>(euc);
		double[] thresh = TestLoad.getSisapThresholds(testfile);

		List<CartesianPoint> qs = t.getQueries(t.dataSize() / 10);
		final List<CartesianPoint> d = t.getDataCopy();
		final List<CartesianPoint> d2 = t.getDataCopy();

		System.out.println("dataset contains " + d.size() + " elements");
		ListOfClusters<CartesianPoint> lc = new ListOfClusters<>(d, m2, 50);

		System.out.println("dataset contains " + d2.size() + " elements");
		VPTree<CartesianPoint> vpt = new VPTree<>(d2, m1);

		m1.reset();
		m2.reset();

		System.out.println("structure contains " + lc.entryPoint.cardinality()
				+ " elements");

		double th = thresh[0];
		int vpRes = 0;
		int lcRes = 0;
		for (CartesianPoint q : qs) {
			lcRes += (lc.thresholdSearch(q, th)).size();
			vpRes += (vpt.thresholdSearch(q, th)).size();

		}
		System.out.println("lc search reqd " + m2.reset() / qs.size() + " ds");
		System.out.println("vp search reqd " + m1.reset() / qs.size() + " ds");

		System.out.println(vpRes + ";" + lcRes);
	}

	public boolean isGreatestFirst() {
		return greatestFirst;
	}

	public void setGreatestFirst(boolean greatestFirst) {
		this.greatestFirst = greatestFirst;
	}
}
