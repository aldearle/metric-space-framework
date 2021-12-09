package searchStructures;

import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import testloads.TestLoad;
import util.OrderedList;

import java.util.ArrayList;
import java.util.List;

public class DistalListOfClusters<T> extends SearchIndex<T> {

	private int clusterSize;
	private ListNode entryPoint;
	private boolean greatestFirst;

	class ListNode {
		T head;
		double minRadius;
		List<T> cluster;
		ListNode next;

		@SuppressWarnings("boxing")
		ListNode(T head, List<T> data) {

			this.head = head;
			this.minRadius = Double.MAX_VALUE;
			if (data.size() <= clusterSize) {
				for (T d : data) {
					final double distance = metric.distance(head, d);
					if (distance < this.minRadius) {
						this.minRadius = distance;
					}
				}
				this.cluster = data;
			} else {
				OrderedList<T, Double> ol = new OrderedList<>(clusterSize);
				Double threshold = null;
				for (T d : data) {
					final double negDistance = -metric.distance(d, head);
					if (threshold == null || negDistance <= threshold) {
						ol.add(d, negDistance);
						threshold = ol.getThreshold();
					}
				}
				this.cluster = ol.getList();
				this.minRadius = -ol.getThreshold();
				for (T d : this.cluster) {
					data.remove(d);
				}
				T nextHead = data.remove(0);
				this.next = new ListNode(nextHead, data);
			}
		}

		public int cardinality() {
			if (this.next == null) {
				return 1 + this.cluster.size();
			} else {
				return 1 + this.cluster.size() + this.next.cardinality();
			}
		}

		public void thresholdQuery(T q, double threshold, List<T> res) {

			double qTohDist = metric.distance(q, head);
			
			if (qTohDist <= threshold) {
				res.add(this.head);
			}
			
			if (!(qTohDist < this.minRadius - threshold)) {
				for (T d : this.cluster) {
					if (metric.distance(d, q) <= threshold) {
						res.add(d);
					}
				}
			}
			
			if (!(qTohDist >= this.minRadius + threshold)) {
				if (this.next != null) {
					this.next.thresholdQuery(q, threshold, res);
				}
			}
		}
	}

	public DistalListOfClusters(List<T> data, Metric<T> metric, int clustersize) {
		super(data, metric);
		// start with a stupid algorithm and take it from there...
		this.clusterSize = clustersize;
		this.setGreatestFirst(false);

		T head = data.remove(0);
		this.entryPoint = new ListNode(head, data);

	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		this.entryPoint.thresholdQuery(query, t, res);
		return res;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] a) throws Exception {

		TestLoad.SisapFile testfile = TestLoad.SisapFile.nasa;
		TestLoad t = new TestLoad(testfile);
		Metric<CartesianPoint> euc = new Euclidean<>();
		CountedMetric<CartesianPoint> m = new CountedMetric<>(euc);
		double[] thresh = TestLoad.getSisapThresholds(testfile);

		List<CartesianPoint> qs = t.getQueries(t.dataSize() / 10);
		final List<CartesianPoint> d = t.getDataCopy();
		final List<CartesianPoint> d2 = t.getDataCopy();

		System.out.println("dataset contains " + d.size() + " elements");
		DistalListOfClusters lc = new DistalListOfClusters(d, m, 50);

		System.out.println("dataset contains " + d2.size() + " elements");
		VPTree vpt = new VPTree(d2, m);

		System.out.println("structure contains " + lc.entryPoint.cardinality()
				+ " elements");

		List<CartesianPoint> res = lc.thresholdSearch(qs.get(3), thresh[2]);
		List<CartesianPoint> res2 = vpt.thresholdSearch(qs.get(3), thresh[2]);

		System.out.println(res.size() + ";" + res2.size());
		// for (CartesianPoint p1 : res) {
		//
		// }
	}

	public boolean isGreatestFirst() {
		return greatestFirst;
	}

	public void setGreatestFirst(boolean greatestFirst) {
		this.greatestFirst = greatestFirst;
	}
}
