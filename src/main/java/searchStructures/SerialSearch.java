package searchStructures;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

public class SerialSearch<T> extends SearchIndex<T> {

	private List<T> data;

	public SerialSearch(List<T> data, Metric<T> metric) {
		super(data, metric);
		this.data = data;
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		for (T d : this.data) {
			if (this.metric.distance(query, d) <= t) {
				res.add(d);
			}
		}
		return res;
	}

	@Override
	public String getShortName() {
		return "serial";
	}

}
