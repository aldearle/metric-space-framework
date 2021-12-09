package test;

import semanticDataTypes.StringShingle;
import semanticDataTypes.StringShingle.ShingleType;
import util.Multiset;
import util.OrderedList;
import coreConcepts.Metric;
import dataPoints.compactEnsemble.CompactEnsemble;
import dataPoints.compactEnsemble.EventToIntegerMap;
import dataPoints.compactEnsemble.JensenShannonDef2a;
import dataPoints.compactEnsemble.JensenShannonDef3;
import dataPoints.compactEnsemble.SEDCompactEnsemble;
import dataSets.fileReaders.StringFileReader;

public class CompactEnsembleTester {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		EventToIntegerMap<String> eToi = constructInverseOrderEIMap();

		StringShingle s2 = new StringShingle("Woolen Pattern Designer",
				ShingleType.singlesAndPairs, eToi);
		StringShingle s1 = new StringShingle("Woollen Pattern Designer",
				ShingleType.singlesAndPairs, eToi);

		Metric<StringShingle> sed = new SEDCompactEnsemble<StringShingle>();
		Metric<StringShingle> js2a = new JensenShannonDef2a<StringShingle>();
		Metric<StringShingle> js3 = new JensenShannonDef3<StringShingle>(0.285);
		Metric<StringShingle> js4 = new JensenShannonDef3<StringShingle>(0.284);
		/*
		 * value should be (close to) 0.28474739872574967 for Brick Moulder Wife
		 * and Brick Moulder Wise
		 */

		System.out.println(sed.distance(s1, s2));
		System.out.println(js2a.distance(s1, s2));
		System.out.println(js3.distance(s1, s2));
		System.out.println(js4.distance(s1, s2));
	}

	private static EventToIntegerMap<String> constructInverseOrderEIMap()
			throws Exception {
		EventToIntegerMap<String> eToi = new EventToIntegerMap<String>();
		final StringFileReader occupationsDataSet = new StringFileReader(
				"testdata/occupations_randomised.txt",
				ShingleType.singlesAndPairs, eToi);

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
}
