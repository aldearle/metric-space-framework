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
public class SimpleWidePartition<T> extends BinaryPartitionFactory<T> {

	private static Random rand = new Random();
	private T givenRef;
	private boolean refProvided;

	/**
	 * Create a new wide partition strategy; randomly takes an object from the
	 * set, and chooses also the element furthest from that
	 * 
	 * @param data
	 * @param metric
	 */
	public SimpleWidePartition(Metric<T> metric) {
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

			T ref1 = SimpleWidePartition.this.givenRef;
			if (!SimpleWidePartition.this.refProvided) {
				ref1 = this.data.remove(rand.nextInt(this.data.size()));
				this.refs.add(ref1);
			}

			T ref2 = this.data.get(0);
			double ref2dist = this.metric.distance(ref1, ref2);
			for (T p : this.data.subList(1, this.data.size())) {
				double d = this.metric.distance(p, ref1);
				if (d > ref2dist) {
					ref2 = p;
					ref2dist = d;
				}
			}
			this.refs.add(ref2);
			final boolean removed = this.data.remove(ref2);
			assert removed : "datum not removed from list";
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
