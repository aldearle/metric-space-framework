package is_paper_experiments.n_ary_trees_fourpoint;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard Connor
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
public abstract class NaryExclusion<T, CreationInfoType, QueryInfoType> {

	protected NaryExclusion(List<T> data, CreationInfoType creationInfo) {
		// deliberately left blank but constructor is required
	}

	/**
	 * result when a query is applied to a given tree node
	 * 
	 * @author Richard Connor
	 *
	 */
	public class QueryResult {

		private List<T> newResults;
		private boolean[] exclusions;
		private List<QueryInfoType> queryContexts;

		/**
		 * a class to return the result of making a query to a tree node in this
		 * context
		 * 
		 * @param arity
		 *            how many branches are below this tree node
		 */
		public QueryResult(int arity) {
			this.newResults = new ArrayList<>();
			this.queryContexts = new ArrayList<>();
			for (int i = 0; i < arity; i++) {
				this.queryContexts.add(null);
			}
			this.exclusions = new boolean[arity];

		}

		/**
		 * @return an array corresponding to the list returned, on tree
		 *         creation, by getDataPartitions; for any entry which is true,
		 *         the corresponding data partition may be safely excluded from
		 *         an ongoing search
		 */
		public boolean[] getExclusions() {
			return this.exclusions;
		}

		/**
		 * @return any data checked in this mechanisms where the query distance
		 *         is within the search radius
		 */
		public List<T> getResults() {
			return this.newResults;
		}

		/**
		 * @return a list of creation context values which must be passed in to
		 *         the constructor one level down
		 */
		public List<QueryInfoType> getContexts() {
			return this.queryContexts;
		}

		/**
		 * @param exc
		 *            set this branch of the data cannot contain any query
		 *            results
		 */
		public void setExclusion(int exc) {
			this.exclusions[exc] = true;
		}

		/**
		 * @param branchNo
		 *            branch of the data...
		 * @param queryContext
		 *            requires this dynamic context info
		 */
		public void setQueryContext(int branchNo, QueryInfoType queryContext) {
			this.queryContexts.set(branchNo, queryContext);
		}

		/**
		 * @param result
		 *            one of the data values checked here is a solution to the
		 *            query
		 */
		public void addResult(T result) {
			this.newResults.add(result);
		}
	}

	/**
	 * must be called before either excludeLeft or excludeRight at each node
	 * 
	 * @param query
	 *            the query
	 * @param threshold
	 *            the search threshold
	 * @param queryContext
	 *            the context suppled by calling this method one level up in the
	 *            tree
	 * @return all the info required when a query is applied to the node
	 *         governed by this exclusion mechanism
	 */
	public abstract QueryResult getQueryInfo(T query, double threshold,
			QueryInfoType queryContext);

	/**
	 * 
	 * @return data to be stored in the left subtree, recursively
	 */
	protected abstract List<List<T>> getDataPartitions();

	protected abstract int getArity();

	protected abstract boolean isDataNode();

	protected abstract List<CreationInfoType> getCreationContexts();

	/**
	 * the volume of the original dataset which has been removed from the
	 * recursive search and is therefore now stored only in this object
	 * 
	 * @return the size of any data stored within this exclusion mechanism
	 */
	public abstract int storedDataSize();

}
