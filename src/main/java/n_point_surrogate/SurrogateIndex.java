package n_point_surrogate;

import java.util.List;

import coreConcepts.Metric;
import searchStructures.SearchIndex;

public class SurrogateIndex<T> extends SearchIndex<T> {

	protected SurrogateIndex(List<T> data, Metric<T> metric) {
		super(data, metric);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortName() {
		// TODO Auto-generated method stub
		return null;
	}

}
