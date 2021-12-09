package examples;

import histogram.MetricHistogram;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import semanticDataTypes.StringShingle;
import semanticDataTypes.StringShingle.ShingleType;
import util.Multiset;
import util.OrderedList;
import util.Timer;
import util.Timer.Command;
import coreConcepts.DataSet;
import coreConcepts.Metric;
import coreConcepts.MetricSpace;
import dataPoints.compactEnsemble.CompactEnsemble;
import dataPoints.compactEnsemble.EventToIntegerMap;
import dataPoints.compactEnsemble.JensenShannonDef2a;
import dataPoints.sparseCartesian.InvertedIndex;
import dataPoints.sparseCartesian.InvertedIndexDef2;
import dataPoints.sparseCartesian.InvertedIndexDef3;
import dataPoints.sparseCartesian.JensenShannonDef1;
import dataPoints.sparseCartesian.JensenShannonDef2;
import dataPoints.sparseCartesian.JensenShannonDef3;
import dataPoints.sparseCartesian.SparseCartesianPoint;
import dataSets.fileReaders.StringFileReader;

public class ExamplesForSisapPaper {

	public static String occFile = "/Users/newrichard/Documents/Research/collated SISAP research/historical occupations/input/occupations_randomised_first_1k.txt";
	public static String dicFile = "/Users/newrichard/dropbox/research/JSSparseArrays/data/English1k.dic";
	public static String dbBase = "/Users/newrichard/dropbox/research/JSSparseArrays/data/";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		doDicHisto(dicFile);
		// doAllTimings(occFile);

	}

	public static void doDicHisto(String dicFile) throws Exception {
		EventToIntegerMap<String> eToi = new EventToIntegerMap<String>();
		DataSet<StringShingle> d = new StringFileReader(dicFile,
				ShingleType.singlesAndPairs, eToi);
		Metric<StringShingle> m = new JensenShannonDef2a();
		MetricSpace<StringShingle> ms = new MetricSpace(d, m);
		PrintStream ps = new PrintStream(dbBase + "engDicHist.csv");
		MetricHistogram.printHeaderRow(ps);
		MetricHistogram<StringShingle> mh = new MetricHistogram<StringShingle>(
				ms, 1000, 1000, true, false, 1.0);
		mh.printToStream(ps);
		ps.close();
	}

	/**
	 * call from outside project with string for occupations data pathname
	 * 
	 * @param occFile
	 * @throws Exception
	 */
	public static void doAllTimings(String occFile) throws Exception {
//		final double t5 = 0.244;
//		final double t6 = 0.195;
		final double t5 = 0.341;
		final double t6 = 0.258;
		final List<SparseCartesianPoint> oc = getOccs1k(occFile);

		final InvertedIndex<SparseCartesianPoint> ii1 = new InvertedIndexDef2(
				oc);
		final InvertedIndex<SparseCartesianPoint> ii2 = new InvertedIndexDef3(
				oc);

		final Metric<SparseCartesianPoint> js1 = new JensenShannonDef1<SparseCartesianPoint>();
		final Metric<SparseCartesianPoint> js2 = new JensenShannonDef2<SparseCartesianPoint>();
		final Metric<SparseCartesianPoint> js3 = new JensenShannonDef3<SparseCartesianPoint>(
				t5);
		final Metric<SparseCartesianPoint> js4 = new JensenShannonDef3<SparseCartesianPoint>(
				t6);

		doExhaustiveTiming(oc, js1);
		doExhaustiveTiming(oc, js2);
		doExhaustiveTiming(oc, js3);
		doExhaustiveTiming(oc, js4);

		doIITiming(t5, oc, ii1);
		doIITiming(t5, oc, ii2);
		doIITiming(t6, oc, ii2);
	}

	private static void doIITiming(final double t5,
			final List<SparseCartesianPoint> oc,
			final InvertedIndex<SparseCartesianPoint> ii1) {
		Command c = new Command() {

			@Override
			public void execute() {
				for (SparseCartesianPoint p : oc) {
					ii1.thresholdQuery(p, t5);
				}

			}
		};
		double d = Timer.time(c, true);
		System.out.println("ii - no name! (" + t5 + ") " + "\t&"
				+ ((d * 1000) / (double) (oc.size() * oc.size())));
	}

	private static void doExhaustiveTiming(final List<SparseCartesianPoint> oc,
			final Metric<SparseCartesianPoint> js1) {
		Command c = new Command() {

			@Override
			public void execute() {
				for (SparseCartesianPoint p : oc) {
					for (SparseCartesianPoint q : oc) {
						double d = js1.distance(p, q);
					}
				}

			}
		};

		double d = Timer.time(c, true);
		System.out.println(js1.getMetricName() + "\t&"
				+ ((d * 1000) / (double) (oc.size() * oc.size())));
	}

	/**
	 * @param occFile
	 * @return
	 * @throws Exception
	 */
	public static EventToIntegerMap<String> constructInverseOrderEIMap(
			String occFile) throws Exception {
		EventToIntegerMap<String> eToi = new EventToIntegerMap<String>();
		final StringFileReader occupationsDataSet = new StringFileReader(
				occFile, ShingleType.singlesAndPairs, eToi);

		Multiset<Integer> all = new Multiset<Integer>();

		for (CompactEnsemble ce : occupationsDataSet) {
			int[] v = ce.getEnsemble();
			for (int i : v) {
				int ev = EventToIntegerMap.getEventCode(i);
				int card = EventToIntegerMap.getCard(i);
				all.add(ev, card);
			}
		}

		OrderedList<Integer, Integer> ol = new OrderedList<Integer, Integer>(
				all.keySet().size());
		for (int ev : all.keySet()) {
			ol.add(ev, all.get(ev));
		}

		EventToIntegerMap<String> res = new EventToIntegerMap<String>();
		for (int evCoded : ol.getList()) {
			res.getEventCode(eToi.getEncodedEvent(evCoded));
		}

		return res;
	}

	/**
	 * @param occFile
	 * @return
	 * @throws Exception
	 */
	public static List<SparseCartesianPoint> getOccs1k(String occFile)
			throws Exception {

		EventToIntegerMap<String> goodEtoI = constructInverseOrderEIMap(occFile);

		List<StringShingle> occRefs = new StringFileReader(occFile,
				ShingleType.singlesAndPairs, goodEtoI);

		List<SparseCartesianPoint> res = new ArrayList<SparseCartesianPoint>();

		for (StringShingle ss : occRefs) {
			res.add(new SparseCartesianPoint(ss));
		}
		return res;
	}

}
