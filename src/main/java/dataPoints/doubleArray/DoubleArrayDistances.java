package dataPoints.doubleArray;

import java.util.HashMap;

import coreConcepts.Metric;

public class DoubleArrayDistances extends HashMap<String, Metric<double[]>> {

	{
		this.put("sed", new SEDDoubleArray());
	}

}
