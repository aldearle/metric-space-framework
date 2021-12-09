package searchStructures;

import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import testloads.TestLoad;
import util.OrderedList;

import java.util.ArrayList;
import java.util.List;

public class ListOfMonPartitions<T> extends SearchIndex<T> {

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
		ListNode(T headl, List<T> data) {

			this.headL = headl;
			// this.headR = data.remove(0);
			this.offset = Double.MIN_VALUE;

			List<Double> headLdists = new ArrayList<>();
			double greatestHeadLdist = 0;
			int furthestFromHeadL = -1;
			int ptr0 = 0;
			for (T d : data) {
				final double thisD = metric.distance(this.headL, d);
				headLdists.add(thisD);
				if (thisD > greatestHeadLdist) {
					greatestHeadLdist = thisD;
					furthestFromHeadL = ptr0;
				}
				ptr0++;
			}
			this.headR = data.remove(furthestFromHeadL);
			headLdists.remove(furthestFromHeadL);
			this.pivotDistance = metric.distance(this.headL, this.headR);

			if (data.size() <= clusterSize) {

				int ptr = 0;
				for (T d : data) {
					final double d1 = headLdists.get(ptr++);
					final double d2 = metric.distance(headR, d);
					this.offset = Math.max(this.offset,
							projectionDistance(pivotDistance, d1, d2));
				}
				this.cluster = data;
			} else {

				OrderedList<T, Double> ol = new OrderedList<>(clusterSize);
				int ptr = 0;
				for (T d : data) {
					final double d1 = headLdists.get(ptr++);
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
				this.next = new ListNode(this.headR, data);
			}
		}

		public int cardinality() {
			if (this.next == null) {
				return 1 + this.cluster.size();
			} else {
				return 1 + this.cluster.size() + this.next.cardinality();
			}
		}

		public void thresholdQuery(T q, double dLeft, double threshold,
				List<T> res) {

			double leftD = dLeft;
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
					this.next.thresholdQuery(q, rightD, threshold, res);
				}
			}
		}
	}

	public ListOfMonPartitions(List<T> data, Metric<T> metric, int clustersize) {
		super(data, metric);
		this.clusterSize = clustersize;

		T headl = data.remove(0);
		this.entryPoint = new ListNode(headl, data);

	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		double dLeft = metric.distance(query, this.entryPoint.headL);
		this.entryPoint.thresholdQuery(query, dLeft, t, res);
		return res;
	}

	@Override
	public String getShortName() {
		return "lomp_" + this.clusterSize;
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
		ListOfMonPartitions lc = new ListOfMonPartitions(d, m, 50);

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
