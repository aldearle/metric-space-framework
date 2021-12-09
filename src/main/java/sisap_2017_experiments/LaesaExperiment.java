package sisap_2017_experiments;

import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;
import is_paper_experiments.dynamic_binary_partitions.MonotoneHyperplaneTree;
import is_paper_experiments.dynamic_binary_partitions.SearchTree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import searchStructures.SearchIndex;
import sisap_2017_experiments.laesa.ClassicIndexedLaesa;
import sisap_2017_experiments.laesa.ClassicLaesa;
import sisap_2017_experiments.laesa.Laesa;
import sisap_2017_experiments.laesa.NpointFPIndexedLaesa;
import sisap_2017_experiments.laesa.NpointLaesa;
import testloads.TestContext;
import testloads.TestContext.Context;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.CosineNormalised;

public class LaesaExperiment<T> {

	public static void main(String[] args) throws Exception {
		TestContext tc = new TestContext(Context.nasa);
		Metric<CartesianPoint> metric;
		double queryThreshold;

		metric = tc.metric();
		queryThreshold = tc.getThreshold();
		//
		// metric = new JensenShannon<>(false, true);
		// queryThreshold = 0.135; // 0.138 gives 180k ,0.13 about 120k results

		// metric = new TriDiscrim<>();
		// queryThreshold = 0.13 * Math.sqrt(2 * Math.log(2));

//		metric = new CosineNormalised<>();
//		queryThreshold = 0.032;		
//		queryThreshold = 0.042;


		CountedMetric<CartesianPoint> cm = new CountedMetric<>(metric);
		tc.setSizes(tc.dataSize() / 10, 0);
		

		for (int i : Range.range(1, 11)) {
			int lDim = i * 2;
			System.out.print(lDim);

			List<Laesa<CartesianPoint>> mechs = new ArrayList<>();
			List<CountedMetric<?>> cms = new ArrayList<>();

			mechs.add(new ClassicLaesa<>(cm, lDim));
			final ClassicIndexedLaesa<CartesianPoint> indexLaesa = new ClassicIndexedLaesa<>(
					cm, lDim);
			cms.add(indexLaesa.getReindexCounter());
			mechs.add(indexLaesa);

			mechs.add(new NpointLaesa<>(cm, lDim));

			// final NpointIndexedLaesa<CartesianPoint> npinl = new
			// NpointIndexedLaesa<>(
			// cm, lDim);
			// cms.add(npinl.getReindexCounter());
			// mechs.add(npinl);

			final NpointFPIndexedLaesa<CartesianPoint> fpnl = new NpointFPIndexedLaesa<>(
					cm, lDim);
			cms.add(fpnl.getReindexCounter());
			mechs.add(fpnl);

			List<CartesianPoint> newData = tc.getDataCopy();
			int noOfQueries = tc.getQueries().size();
			// List<CartesianPoint> refPoints = Util_ISpap Dim);
			List<CartesianPoint> refPoints = Util_ISpaper.getRandom(
					newData.subList(0, 1000), lDim);
			for (CartesianPoint p : refPoints) {
				newData.remove(p);
			}

			List<SearchIndex<CartesianPoint>> indexes = new ArrayList<>();
			for (Laesa<CartesianPoint> mech : mechs) {
				indexes.add(Laesa.getSearchIndex(mech, cm, newData, refPoints));
			}

			BinaryPartitionFactory<CartesianPoint> sw = new SimpleWidePartition<>(
					cm);
			final MonotoneHyperplaneTree<CartesianPoint> mhpt = new MonotoneHyperplaneTree<>(
					cm);
			mhpt.setFourPoint(true);
			mhpt.setPartitionStrategy(sw);
			SearchTree<CartesianPoint, ?, ?> in2 = new SearchTree<>(
					tc.getDataCopy(), mhpt);
			indexes.add(in2);

			Set<Integer> totalCheck = new HashSet<>();
			cm.reset();
			for (CountedMetric<?> met : cms) {
				int t = met.reset();
			}
			for (SearchIndex<CartesianPoint> index : indexes) {

				int resSize = 0;
				long t0 = System.currentTimeMillis();

				for (CartesianPoint q : tc.getQueries()) {
					List<CartesianPoint> res = index.thresholdSearch(q,
							queryThreshold);
					resSize += res.size();
				}
				totalCheck.add(resSize);

				long tDiff = System.currentTimeMillis() - t0;

				System.out
						.print("\t" + tDiff + "\t" + cm.reset() / noOfQueries);
				System.out.print("\t");
				for (CountedMetric<?> met : cms) {
					int t = met.reset();
					if (t != 0) {
						System.out.print(t / noOfQueries);
					}
				}
			}
			for (int tot : totalCheck) {
				System.out.print("\t" + tot);
			}
			System.out.println();
		}

	}
}
