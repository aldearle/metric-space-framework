package semanticDataTypes;

import semanticDataTypes.StringShingle.ShingleType;
import coreConcepts.Metric;
import dataPoints.compactEnsemble.EventToIntegerMap;
import dataPoints.compactEnsemble.Levenshtein;

public class TestLev {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ShingleType dontCare = StringShingle.ShingleType.charPair;
		EventToIntegerMap eToi = new EventToIntegerMap();
		StringShingle s1 = new StringShingle("11", dontCare, eToi);
		StringShingle s2 = new StringShingle("10", dontCare, eToi);

		Metric<StringShingle> lev = new Levenshtein<>();
		System.out.println(lev.distance(s1, s2));

		System.out.println("done");
	}

}
