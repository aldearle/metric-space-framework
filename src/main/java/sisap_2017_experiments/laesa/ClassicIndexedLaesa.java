package sisap_2017_experiments.laesa;

import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;
import is_paper_experiments.dynamic_binary_partitions.MonotoneHyperplaneTree;
import is_paper_experiments.dynamic_binary_partitions.SearchTree;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;
import util.Range;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import coreConcepts.NamedObject;
import dataPoints.doubleArray.Chebyshev;

public class ClassicIndexedLaesa<T> extends Laesa<T> {

	SearchIndex<NamedObject<double[]>> index;
	CountedMetric<NamedObject<double[]>> reindexMetric;

	public ClassicIndexedLaesa(Metric<T> metric, int dimension) {
		super(metric, dimension);
		this.reindexMetric = new CountedMetric<>(getCheby());
	}

	@Override
	public void setupTable() {
		List<NamedObject<double[]>> surs = new ArrayList<>();

		int ptr = 0;
		for (T p : this.data) {
			double[] sur = getRefDists(p);
			surs.add(new NamedObject<>(sur, Integer.toString(ptr++)));
		}

		BinaryPartitionFactory<NamedObject<double[]>> sw = new SimpleWidePartition<>(
				this.reindexMetric);
		final MonotoneHyperplaneTree<NamedObject<double[]>> mhpt = new MonotoneHyperplaneTree<>(
				this.reindexMetric);
		mhpt.setFourPoint(false);
		mhpt.setPartitionStrategy(sw);
		SearchTree<NamedObject<double[]>, NamedObject<double[]>, Double> tree = new SearchTree<>(
				surs, mhpt);

		this.index = tree;
	}

	private static Metric<NamedObject<double[]>> getCheby() {
		final Metric<double[]> ch = new Chebyshev();
		return new Metric<NamedObject<double[]>>() {

			@Override
			public double distance(NamedObject<double[]> x,
					NamedObject<double[]> y) {
				return ch.distance(x.object, y.object);
			}

			@Override
			public String getMetricName() {
				return "cheby";
			}
		};
	}

	@Override
	public List<T>[] filter(T query, double threshold) {
		double[] dists = getRefDists(query);
		List<T>[] res = new ArrayList[2];
		res[0] = new ArrayList<>();
		res[1] = new ArrayList<>();
		for (int i : Range.range(0, dists.length)) {
			if (dists[i] <= threshold) {
				res[1].add(this.refPoints.get(i));
			}
		}
		List<NamedObject<double[]>> search = this.index.thresholdSearch(
				new NamedObject<>(dists, null), threshold);
		for (NamedObject<?> no : search) {
			res[0].add(this.data.get(Integer.parseInt(no.getName())));
		}
		return res;
	}

	@Override
	public String getName() {
		return "classicIndexed";
	}

	public CountedMetric<?> getReindexCounter() {
		return this.reindexMetric;
	}

}
