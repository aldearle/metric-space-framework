package is_paper_experiments.binary_partitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import coreConcepts.Metric;

/**
 * Simply selects two random elements from the data
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data
 */
public class RandomPartition<T> extends BinaryPartitionFactory<T> {

	private static Random rand = new Random();
	private T givenRef;
	private boolean refProvided;

	/**
	 * Create a new random partition strategy; just takes two random elements
	 * from the provided data
	 * 
	 * @param data
	 * @param metric
	 */
	public RandomPartition(Metric<T> metric) {
		super(metric);
	}

	private class RandPartition extends BinaryPartition<T> {
		List<T> refs;

		RandPartition(List<T> data, Metric<T> metric) {
			super(data, metric);
		}

		@SuppressWarnings("synthetic-access")
		@Override
		protected void intialise() {
			this.refs = new ArrayList<>();
			this.refs.add(this.data.remove(rand.nextInt(this.data.size())));
			if (!refProvided) {
				this.refs.add(this.data.remove(rand.nextInt(this.data.size())));
			}
		}

		@Override
		public List<T> getReferencePoints() {
			return this.refs;
		}

		@Override
		public List<T> getData() {
			return this.data;
		}
	}

	@Override
	public BinaryPartition<T> getPartition(List<T> data) {
		this.refProvided = false;
		return new RandPartition(data, this.metric);
	}

	@Override
	public BinaryPartition<T> getPartition(List<T> data, T givenRef) {
		this.refProvided = true;
		this.givenRef = givenRef;
		return new RandPartition(data, this.metric);
	}

}
