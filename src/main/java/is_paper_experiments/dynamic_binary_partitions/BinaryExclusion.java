package is_paper_experiments.dynamic_binary_partitions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author newrichard
 *
 * @param <T>
 */
/**
 * 
 * 
 * This class (maybe should be an interface, but may start to include utility
 * functions) is the one available to a generic metric index implementation,
 * which allows the logical separation of creation, testing etc from the actual
 * exclusion mechanisms being used
 * 
 * use: an ExclusionFactory class is used by a generic data arrangement
 * mechanism (eg a binary search tree in the simplest case) to generate an
 * exclusion according to the given context, ie the data and metric available
 * 
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of data being searched
 * @param <CreationInfoType>
 *            the type of info required from the parent context when a child
 *            node is created. For example in a monotone tree it is the shared
 *            reference point
 * @param <QueryInfoType>
 *            the type of info required from the parent context when a child
 *            node is queried; for example in a monotone tree it is the distance
 *            to the shared reference point
 */
public abstract class BinaryExclusion<T, CreationInfoType, QueryInfoType> {

	/**
	 * result when a query is applied to a given tree node
	 * 
	 * @author Richard Connor
	 *
	 */
	public abstract class ExclusionTest {

		private List<T> newResults;
		private boolean[] exclusions;

		protected ExclusionTest(CreationInfoType c) {
			this.newResults = new ArrayList<>();
			this.exclusions = new boolean[2];
		}

		public boolean canExcludeLeft() {
			return this.exclusions[0];
		}

		public boolean canExcludeRight() {
			return this.exclusions[1];
		}

		/**
		 * @return
		 */
		public List<T> getResults() {
			return this.newResults;
		}

		public void addResult(T result) {
			this.newResults.add(result);
		}

		public void setExcludeLeft() {
			this.exclusions[0] = true;
		}

		public void setExcludeRight() {
			this.exclusions[1] = true;
		}

		public abstract QueryInfoType getLeftQueryContext();

		public abstract QueryInfoType getRightQueryContext();
	}

	/**
	 * must be called before either excludeLeft or excludeRight at each node
	 * 
	 * @param m
	 */
	public abstract ExclusionTest getQueryInfo(T query, double threshold,
			QueryInfoType queryContext);

	/**
	 * 
	 * @return data to be stored in the left subtree, recursively
	 */
	public abstract List<T> leftData();

	/**
	 * 
	 * @return data to be stored in the right subtree, recursively
	 */
	public abstract List<T> rightData();

	public abstract CreationInfoType getLeftCreationContext();

	public abstract CreationInfoType getRightCreationContext();

	/**
	 * the volume of the original dataset which has been removed from the
	 * recursive search and is therefore now stored only in this object
	 * 
	 * @return
	 */
	public abstract int storedDataSize();

}
