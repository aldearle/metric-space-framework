package sisap_2017_experiments.laesa;

import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;
import is_paper_experiments.dynamic_binary_partitions.MonotoneHyperplaneTree;
import is_paper_experiments.dynamic_binary_partitions.SearchTree;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;
import sisap_2017_experiments.NdimSimplex;
import util.Range;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import coreConcepts.NamedObject;
import dataPoints.doubleArray.Euclidean;

public class NpointIndexedLaesa<T> extends Laesa<T> {

	SearchIndex<NamedObject<double[]>> index;
	NdimSimplex<T> simp;
	CountedMetric<NamedObject<double[]>> reindexMetric;

	public NpointIndexedLaesa(Metric<T> metric, int dimension) {
		super(metric, dimension);
		this.reindexMetric = new CountedMetric<>(getL2());
	}

	@Override
	public void setupTable() {
		List<NamedObject<double[]>> surs = new ArrayList<>();
		this.simp = new NdimSimplex<>(this.metric, this.refPoints);

		int ptr = 0;
		for (T p : this.data) {
			double[] dists = getRefDists(p);
			double[] apex = this.simp.getApex(dists);

			surs.add(new NamedObject<>(apex, Integer.toString(ptr++)));
		}

		BinaryPartitionFactory<NamedObject<double[]>> sw = new SimpleWidePartition<>(
				this.reindexMetric);
		final MonotoneHyperplaneTree<NamedObject<double[]>> mhpt = new MonotoneHyperplaneTree<>(
				this.reindexMetric);
		mhpt.setFourPoint(true);
		mhpt.setPartitionStrategy(sw);
		SearchTree<NamedObject<double[]>, ?, ?> tree = new SearchTree<>(surs,
				mhpt);

		this.index = tree;
	}

	@Override
	public List<T>[] filter(T query, double threshold) {
		List<T>[] res = new ArrayList[2];
		res[0] = new ArrayList<>();
		res[1] = new ArrayList<>();
		double[] dists = getRefDists(query);
		for (int i : Range.range(0, dists.length)) {
			if (dists[i] <= threshold) {
				res[1].add(this.refPoints.get(i));
			}
		}
		double[] apex = this.simp.getApex(dists);
		List<NamedObject<double[]>> search = this.index.thresholdSearch(
				new NamedObject<>(apex, null), threshold);
		for (NamedObject<?> no : search) {
			res[0].add(this.data.get(Integer.parseInt(no.getName())));
		}
		return res;
	}

	@Override
	public String getName() {
		return "npointIndexed";
	}

	private static Metric<NamedObject<double[]>> getL2() {
		final Metric<double[]> euc = new Euclidean();

		return new Metric<NamedObject<double[]>>() {

			@Override
			public double distance(NamedObject<double[]> x,
					NamedObject<double[]> y) {
				return euc.distance(x.object, y.object);
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
