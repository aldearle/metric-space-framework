package query;

import java.util.List;

public interface MetricIndex<T> {
	List<T> thresholdQuery(T query, double t);
}
