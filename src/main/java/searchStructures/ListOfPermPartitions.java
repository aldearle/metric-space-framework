package searchStructures;

import java.util.ArrayList;
import java.util.List;

import testloads.TestLoad;
import util.OrderedList;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;

public class ListOfPermPartitions<T> extends SearchIndex<T> {

	int clusterSize;
	ListNode entryPoint;
	List<T> permutants;
	double[][] permdists;
	PermMap pm = new PermMap();
	double[] qToPdists;

	class PermMap {
		int p1 = -1;
		int p2 = 1;

		int[] nextPair() {
			int[] res = new int[2];
			if (p2 - p1 == 1) {
				p1 = 0;
				p2++;
				int[] r = { p1, p2 };
				res = r;
			} else {
				p1++;
				int[] r = { p1, p2 };
				res = r;
			}
			return res;
		}
	}

	class ListNode {
		int headL;
		int headR;
		double offset;
		double pivotDistance;
		List<T> cluster;
		ListNode next;

		@SuppressWarnings("boxing")
		ListNode(List<T> data) {
			int[] ps = pm.nextPair();
			headL = ps[0];
			headR = ps[1];

			this.pivotDistance = permdists[headL][headR];
			// ?metric.distance(headl, headr);
			this.offset = Double.MIN_VALUE;
			if (data.size() <= clusterSize) {
				for (T d : data) {
					final double d1 = metric.distance(permutants.get(headL), d);
					final double d2 = metric.distance(permutants.get(headR), d);
					this.offset = Math.max(this.offset,
							projectionDistance(pivotDistance, d1, d2));
				}
				this.cluster = data;
			} else {

				OrderedList<T, Double> ol = new OrderedList<>(clusterSize);
				for (T d : data) {
					final double d1 = metric.distance(permutants.get(headL), d);
					final double d2 = metric.distance(permutants.get(headR), d);
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
				this.next = new ListNode(data);
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

			double leftD = qToPdists[headL];
			double rightD = qToPdists[headR];
			double projDist = projectionDistance(this.pivotDistance, leftD,
					rightD);

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

	public ListOfPermPartitions(List<T> data, Metric<T> metric, int clustersize) {
		super(data, metric);
		// start with a stupid algorithm and take it from there...
		this.clusterSize = clustersize;
		int perms = permutantsRequired(data.size(), clustersize);
		System.out.println("perms req: " + perms);
		this.permutants = new ArrayList<>();
		for (int i = 0; i < perms; i++) {
			this.permutants.add(data.remove(0));
		}
		intitialisePivotDistances();

		this.entryPoint = new ListNode(data);

	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();

		this.qToPdists = new double[permutants.size()];
		int ptr = 0;
		for (T p : permutants) {
			double d = metric.distance(query, p);
			qToPdists[ptr++] = d;
			if (d <= t) {
				res.add(p);
			}
		}

		this.entryPoint.thresholdQuery(query, t, res);
		return res;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

	int permutantsRequired(int datasize, int clustersize) {
		int clustersRequired = datasize / clustersize;
		double f = (1 + Math.sqrt(1 + clustersRequired * 8)) / 2;
		int ceil = (int) Math.ceil(f);
		return ceil;
	}

	private void intitialisePivotDistances() {
		this.permdists = new double[this.permutants.size()][this.permutants
				.size()];
		for (int i = 0; i < this.permutants.size(); i++) {
			for (int j = i + 1; j < this.permutants.size(); j++) {
				this.permdists[i][j] = this.metric.distance(
						this.permutants.get(i), this.permutants.get(j));
				this.permdists[j][i] = this.permdists[i][j];
			}
		}
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
		ListOfPermPartitions lc = new ListOfPermPartitions(d, m, 50);

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
