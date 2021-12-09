package query;

import histogram.MetricHistogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import query.InvertedIndexList.IndexPair;
import semanticDataTypes.StringShingle;
import util.ConstantsAndArith;
import util.OrderedList;
import coreConcepts.DataSet;
import coreConcepts.Metric;
import coreConcepts.MetricSpace;
import dataPoints.compactEnsemble.CompactEnsemble;
import dataPoints.compactEnsemble.SEDCompactEnsemble;

/**
 * don't really know what this class is all about as I've forgotten....!
 * 
 * I'm pretty sure it needs to be split into some more general classes!
 * 
 * @author Richard Connor
 * 
 * @param <Ensemble>
 */
public class InvertedIndexQueryableSet<Ensemble extends CompactEnsemble> {

	private DataSet<Ensemble> dataset;
	private InvertedIndexList theInvertedIndex;

	/**
	 * takes in a dataset and builds the inverted index.
	 * 
	 * @param dataSet
	 * @throws Exception
	 */
	public InvertedIndexQueryableSet(DataSet<Ensemble> dataSet)
			throws Exception {
		this.dataset = dataSet;
		buildInvertedIndex(dataSet);
	}

	public int size() {
		return dataset.size();
	}

	public void analyse() {
		long acc = 0;
		int min = 100;
		int max = 0;

		Set<Integer> allEvents = new HashSet<Integer>();
		Set<Integer> allEnsembles = new HashSet<Integer>();

		for (Ensemble s : dataset) {
			final int[] ensemble = s.getEnsemble();

			for (int i : ensemble) {
				allEvents.add(i >> 16);
				allEnsembles.add(i);
			}

			final int length = ensemble.length;
			acc += length;
			min = Math.min(min, length);
			max = Math.max(max, length);
		}
		System.out.println("mean length is " + acc / (double) dataset.size());
		System.out.println("max length is " + max);
		System.out.println("min length is " + min);
		System.out.println("total number of events  is " + allEvents.size());
		System.out.println("total number of event/vals  is "
				+ allEnsembles.size());
	}

	public void doHistogram() throws FileNotFoundException {
		MetricSpace<Ensemble> ms = getSEDMetricSpace();
		MetricHistogram<Ensemble> h = new MetricHistogram<Ensemble>(ms, 2000,
				1000, true, false, 1.0);
		PrintStream ps = new PrintStream(new File(
				"data/occupations/sedHistogram.csv"));
		h.printToStream(ps);
		ps.close();
	}

	private MetricSpace<Ensemble> getSEDMetricSpace() {
		Metric<Ensemble> sed = new SEDCompactEnsemble<Ensemble>();
		MetricSpace<Ensemble> ms = new MetricSpace<Ensemble>(dataset, sed);
		return ms;
	}

	private void buildInvertedIndex(DataSet<Ensemble> data) {
		this.theInvertedIndex = new InvertedIndexList();
		int index = 0;

		for (Ensemble dataPoint : data) {

			int card = dataPoint.getCardinality();
			int[] ens = dataPoint.getEnsemble();

			for (int event : ens) {
				int count = event % ConstantsAndArith.sixteenBitModMask;
				double freq = count / (double) card;

				short ev = (short) (event >> 16);

				this.theInvertedIndex.add(ev, index, freq);
			}
			index++;

		}
	}

	public List<Integer> getClosest(StringShingle stringShingle,
			double threshold) {

		double rawThreshold = Math.pow(Math.E, Math.log(threshold) / 0.486);
		double simRequired = 2 * (1 - (Math.log(rawThreshold + 1) / Math.log(2)));

		List<Integer> res = new ArrayList<Integer>();

		int[] query = stringShingle.getEnsemble();
		int card = stringShingle.getCardinality();

		Map<Integer, Double> comparands = new HashMap<Integer, Double>();

		for (int event : query) {
			int count = event % ConstantsAndArith.sixteenBitModMask;
			short ev = (short) (event >> 16);
			double freq = count / (double) card;

			List<InvertedIndexList.IndexPair> closeList = theInvertedIndex
					.getSedClosenessList(ev, freq);
			for (InvertedIndexList.IndexPair pair : closeList) {

				if (comparands.containsKey(pair.id)) {
					comparands.put(pair.id, comparands.get(pair.id) + pair.val);
				} else {
					comparands.put(pair.id, pair.val);
				}
			}

		}

		for (int i : comparands.keySet()) {
			if (comparands.get(i) > simRequired) {
				res.add(i);
			}
		}

		return res;

	}

	/**
	 * @param ce
	 *            the CompactEnsemble
	 * @return a list of IndexPairs, ordered inversely with the number of
	 *         entries per event in the inverted index
	 */
	private List<IndexPair> getQuery(CompactEnsemble ce) {
		int[] ens = ce.getEnsemble();
		int card = ce.getCardinality();

		OrderedList<IndexPair, Integer> orderedQuery = new OrderedList<IndexPair, Integer>(
				ens.length);

		for (int event : ens) {
			int count = event % ConstantsAndArith.sixteenBitModMask;
			short ev = (short) (event >> 16);
			double freq = count / (double) card;
			IndexPair ip = new InvertedIndexList.IndexPair(ev, freq);

			/*
			 * TODO this is ordering the terms by how uncommon they are in the
			 * dataset which might be a good thing to do or might not...
			 * certainly shouldn't be built in!
			 */

			int size = 0;
			if (this.theInvertedIndex.get(ev) != null) {
				size = this.theInvertedIndex.get(ev).size();
			}

			orderedQuery.add(ip, size);
		}

		return orderedQuery.getList();
	}

	public Map<Integer, Double> getThresholdQuery(StringShingle stringShingle,
			double threshold) {

		double rawThreshold = Math.pow(Math.E, Math.log(threshold) / 0.486);
		double simRequired = 2 * (1 - (Math.log(rawThreshold + 1) / Math.log(2)));
		double max_droppable = 2 - simRequired;

		List<IndexPair> queryList = getQuery(stringShingle);

		double max_possible = 0;
		Map<Integer, Double> contenders = new HashMap<Integer, Double>();

		int queryPointer = 0;

		/*
		 * phase 1
		 */

		while (queryPointer < queryList.size() && max_possible < max_droppable) {
			IndexPair queryPair = queryList.get(queryPointer++);
			short ev = (short) queryPair.id;
			double freq = queryPair.val;
			max_possible += 2 * freq;

			List<InvertedIndexList.IndexPair> closeList = theInvertedIndex
					.getSedClosenessList(ev, freq);
			for (InvertedIndexList.IndexPair pair : closeList) {

				if (contenders.containsKey(pair.id)) {
					contenders.put(pair.id, contenders.get(pair.id) + pair.val);
				} else {
					contenders.put(pair.id, pair.val);
				}
			}

		}
		/*
		 * down to here
		 */
		/*
		 * Phase 2
		 */
		while (queryPointer < queryList.size()) {

			IndexPair queryPair = queryList.get(queryPointer++);
			short thisEvent = (short) queryPair.id;
			double queryFreq = queryPair.val;
			max_possible += 2 * queryFreq;

			Set<Integer> removalList = new HashSet<Integer>();

			double t1 = theInvertedIndex.xLog2x(queryFreq);
			Map<Integer, Double> eventFrequencyList = theInvertedIndex
					.get(thisEvent);
			for (int contenderId : contenders.keySet()) {
				/*
				 * so here we need the inverted index to give us a
				 * Map<Integer,Double> instead of the List<IndexPair> -
				 * shouldn't be much of a space overhead...
				 */
				double contenderAcc = contenders.get(contenderId);
				if (eventFrequencyList.containsKey(contenderId)) {
					double contendorFreq = eventFrequencyList.get(contenderId);
					double t2 = theInvertedIndex.xLog2x(contendorFreq);
					double t3 = theInvertedIndex.xLog2x(queryFreq
							+ contendorFreq);
					contenderAcc += t1 + t2 - t3;
				}
				if (max_possible - contenderAcc > max_droppable) {
					removalList.add(contenderId);
				} else {
					contenders.put(contenderId, contenderAcc);
				}
			}
			for (int chop : removalList) {
				contenders.remove(chop);
			}
		}

		return contenders;
	}

}
