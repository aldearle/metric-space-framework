package sisap_2017_experiments.laesa;

import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;
import is_paper_experiments.dynamic_binary_partitions.MonotoneHyperplaneTree;
import is_paper_experiments.dynamic_binary_partitions.SearchTree;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;
import sisap_2017_experiments.NdimSimplex;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import coreConcepts.NamedObject;

public class NpointFPIndexedLaesa<T> extends Laesa<T> {

	private static class ApexStructure {
		public int id;
		// public double upb;
		public double[] apex;

		ApexStructure(int id, double[] apex) {
			this.id = id;
			this.apex = apex;
		}
	}

	SearchIndex<ApexStructure> index;
	NdimSimplex<T> simp;
	CountedMetric<ApexStructure> reindexMetric;

	public NpointFPIndexedLaesa(Metric<T> metric, int dimension) {
		super(metric, dimension);
		this.reindexMetric = new CountedMetric<>(getL2());
	}

	private static double upperBoundSq(double[] xs, double[] ys) {
		double acc = 0;
		int max = xs.length;
		for (int i = 0; i < max - 1; i++) {
			double diff = xs[i] - ys[i];
			acc += diff * diff;
		}
		double last = xs[max - 1] + ys[max - 1];
		acc += last * last;
		return acc;
	}

	@Override
	public void setupTable() {
		List<ApexStructure> surs = new ArrayList<>();
		this.simp = new NdimSimplex<>(this.metric, this.refPoints);

		int ptr = 0;
		for (T p : this.data) {
			double[] dists = getRefDists(p);
			double[] apex = this.simp.getApex(dists);

			surs.add(new ApexStructure(ptr++, apex));
		}

		BinaryPartitionFactory<ApexStructure> sw = new SimpleWidePartition<>(
				this.reindexMetric);
		final MonotoneHyperplaneTree<ApexStructure> mhpt = new MonotoneHyperplaneTree<>(
				this.reindexMetric);
		mhpt.setFourPoint(true);
		mhpt.setPartitionStrategy(sw);
		SearchTree<ApexStructure, ?, ?> tree = new SearchTree<>(surs, mhpt);

		this.index = tree;
	}

	@Override
	public List<T>[] filter(T query, double threshold) {

		final double tSquared = threshold * threshold;
		List<T>[] res = new ArrayList[2];
		res[0] = new ArrayList<>();
		res[1] = new ArrayList<>();

		double[] dists = getRefDists(query);
		for (int i = 0; i < dists.length; i++) {
			if (dists[i] <= threshold) {
				res[1].add(this.refPoints.get(i));
			}
		}
		double[] apex = this.simp.getApex(dists);
		List<ApexStructure> search = this.index.thresholdSearch(
				new ApexStructure(-1, apex), threshold);
		for (ApexStructure ap : search) {
			if (upperBoundSq(apex, ap.apex) <= tSquared) {
				res[1].add(this.data.get(ap.id));
			} else {
				res[0].add(this.data.get(ap.id));
			}
		}
		return res;
	}

	@Override
	public String getName() {
		return "npointFPIndexed";
	}

	private static Metric<ApexStructure> getL2() {
		return new Metric<ApexStructure>() {

			@Override
			public double distance(ApexStructure x, ApexStructure y) {
				double acc = 0;
				int max = x.apex.length;
				for (int i = 0; i < max; i++) {
					double diff = x.apex[i] - y.apex[i];
					acc += diff * diff;
				}
				return Math.sqrt(acc);
			}

			@Override
			public String getMetricName() {
				return "euc";
			}
		};
	}

	public CountedMetric<?> getReindexCounter() {
		return this.reindexMetric;
	}
}
