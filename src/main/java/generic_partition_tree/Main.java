package generic_partition_tree;

import generic_partition_tree.MonotonicTree.Strategy;
import generic_partition_tree.MonotonicTree.TreeType;

import java.util.List;

import searchStructures.SearchIndex;
import searchStructures.experimental.PermutationTree;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannon;

public class Main {

	public static void main(String[] args) throws Exception {

		final SisapFile sisapFile = TestLoad.SisapFile.colors;
		TestLoad tl = new TestLoad(sisapFile);
		double[] thresholds = TestLoad.getSisapThresholds(sisapFile);

		System.out.println("testing file " + sisapFile);

		List<CartesianPoint> qs = tl.getQueries(tl.dataSize() / 10);

		System.out.println(sisapFile + " data size is "
				+ tl.getDataCopy().size());

		Metric<CartesianPoint> euc = new Euclidean<>();
		Metric<CartesianPoint> jsd = new JensenShannon<>(false, false);
		CountedMetric<CartesianPoint> cm = new CountedMetric<>(euc);

		String[] indexTypes = { "pt" };// ,"b_hil", "b_hil_h", "hil" };// ,
										// "hil", "b_hyp",
										// "hyp"
		// };
		TreeType[] types = { TreeType.monotonic };// , TreeType.normal };
		Strategy[] strats = { Strategy.quiteNear };// , Strategy.quiteNear
		// Strategy.first };

		for (TreeType treeType : types) {
			MonotonicTree.setTreeType(treeType);
			for (Strategy strategy : strats) {
				MonotonicTree.setStrategy(strategy);
				for (String indexType : indexTypes) {
					testOut(indexType, treeType, strategy, sisapFile,
							tl.getDataCopy(), qs, cm, thresholds);
				}
			}
		}
	}

	public static <T> void testOut(String index, TreeType treeType,
			Strategy strategy, SisapFile s, List<T> data, List<T> qs,
			CountedMetric<T> m, double[] ts) {

		long t0 = System.currentTimeMillis();

		SearchIndex<T> ght = getIndex(index, data, m);

		printTestOutcome(s, strategy, qs, m, ts, t0, ght);
	}

	public static <T> void printTestOutcome(SisapFile s, Strategy strategy,
			List<T> qs, CountedMetric<T> m, double[] ts, long t0,
			SearchIndex<T> tree) {
		long t1 = System.currentTimeMillis();
		int buildDists = m.reset();
		System.out.print(s + "\t" + strategy + "\t" + tree.getShortName()
				+ "\t" + +buildDists + "\t" + (t1 - t0));
		for (double t : ts) {
			long t2 = System.currentTimeMillis();
			int res = 0;
			for (T q : qs) {
				res += (tree.thresholdSearch(q, t)).size();
			}
			long t3 = System.currentTimeMillis();
			System.out.print("\t" + res + "\t" + m.reset() / (float) qs.size()
					+ "\t" + (t3 - t2) / (float) qs.size());
		}
		System.out.println();
	}

	protected static <T> SearchIndex<T> getIndex(String s, List<T> data,
			CountedMetric<T> m) {
		switch (s) {
		case "hyp": {
			UnbalHypTree<T> ght = new UnbalHypTree<>(data, m);
			return ght;
		}
		case "hil": {
			UnbalHilTree<T> ght = new UnbalHilTree<>(data, m);
			return ght;
		}
		case "b_hyp": {
			BalHypTree<T> ght = new BalHypTree<>(data, m);
			return ght;
		}
		case "b_hil": {
			BalHilVTree<T> ght = new BalHilVTree<>(data, m);
			return ght;
		}
		case "b_hil_h": {
			BalHilHTree<T> ght = new BalHilHTree<>(data, m);
			return ght;
		}
		case "pt": {
			SearchIndex<T> ght = new PermutationTree<>(data, m);
			return ght;
		}

		}
		return null;
	}
}
