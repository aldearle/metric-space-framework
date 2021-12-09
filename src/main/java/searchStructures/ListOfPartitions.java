package searchStructures;

import java.util.ArrayList;
import java.util.List;

import testloads.TestLoad;
import util.OrderedList;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

public class ListOfPartitions<T> extends SearchIndex<T> {

	int clusterSize;
	ListNode entryPoint;

	class ListNode {
		T headL;
		T headR;
		double offset;
		double pivotDistance;
		List<T> cluster;
		ListNode next;

		@SuppressWarnings("boxing")
		ListNode(T headl, T headr, List<T> data) {

			this.headL = headl;
			this.headR = headr;
			this.pivotDistance = metric.distance(headl, headr);
			this.offset = Double.MIN_VALUE;
			if (data.size() <= clusterSize) {
				for (T d : data) {
					final double d1 = metric.distance(headL, d);
					final double d2 = metric.distance(headR, d);
					this.offset = Math.max(this.offset,
							projectionDistance(pivotDistance, d1, d2));
				}
				this.cluster = data;
			} else {

				OrderedList<T, Double> ol = new OrderedList<>(clusterSize);
				for (T d : data) {
					final double d1 = metric.distance(headL, d);
					final double d2 = metric.distance(headR, d);
					double dOffset = projectionDistance(pivotDistance, d1, d2);
					Double localMax = null;
					if (localMax == null || dOffset <= localMax) {
						ol.add(d, dOffset);
						localMax = ol.getThreshold();
					}
				}
				this.cluster = ol.getList();
				this.offset = ol.getThreshold();
				for (T d : this.cluster) {
					data.remove(d);
				}
				T nextHead1 = data.remove(0);
				T nextHead2 = data.remove(0);
				this.next = new ListNode(nextHead1, nextHead2, data);
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

			double leftD = metric.distance(q, headL);
			double rightD = metric.distance(q, headR);
			double projDist = projectionDistance(this.pivotDistance, leftD,
					rightD);
			
			if (leftD <= threshold) {
				res.add(this.headL);
			}
			if (rightD <= threshold) {
				res.add(this.headR);
			}

			if (projDist <= offset + threshold) {
				for (T d : this.cluster) {
					if (metric.distance(d, q) <= threshold) {
						res.add(d);
					}
				}
			}

			if (!(projDist < offset - threshold)) {
				if (this.next != null) {
					this.next.thresholdQuery(q, threshold, res);
				}
			}
		}
	}

	public ListOfPartitions(List<T> data, Metric<T> metric, int clustersize) {
		super(data, metric);
		// start with a stupid algorithm and take it from there...
		this.clusterSize = clustersize;

		T headl = data.remove(0);
		T headr = data.remove(0);
		this.entryPoint = new ListNode(headl, headr, data);

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

		TestLoad.SisapFile testfile = TestLoad.SisapFile.colors;
		TestLoad t = new TestLoad(testfile);
		Metric<CartesianPoint> euc = new Euclidean<>();
		CountedMetric<CartesianPoint> m = new CountedMetric<>(euc);
		double[] thresh = TestLoad.getSisapThresholds(testfile);

		List<CartesianPoint> qs = t.getQueries(t.dataSize() / 10);
		final List<CartesianPoint> d = t.getDataCopy();
		final List<CartesianPoint> d2 = t.getDataCopy();

		System.out.println("dataset contains " + d.size() + " elements");
		ListOfPartitions lc = new ListOfPartitions(d, m, 50);

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
}
