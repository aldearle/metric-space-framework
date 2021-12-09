package is_paper_experiments.binary_partitions;

import java.util.List;

import coreConcepts.Metric;

public abstract class BinaryPartitionFactory<T> {
	Metric<T> metric;

	BinaryPartitionFactory(Metric<T> metric) {
		this.metric = metric;
	}

	public abstract BinaryPartition<T> getPartition(List<T> data);
	
	public abstract BinaryPartition<T> getPartition(List<T> data, T givenRef);

}
