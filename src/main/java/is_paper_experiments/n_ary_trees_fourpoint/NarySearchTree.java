package is_paper_experiments.n_ary_trees_fourpoint;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;

/**
 * 
 * This class builds a generic binary tree independently from the mechanism used
 * to split the data at each node, or to exclude child nodes from queries
 * 
 * this allows the same search structure to be used for lots of different index
 * types
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the object being stored
 * @param <C>
 *            the type of context required to build tree at each tree node
 * @param <Q>
 *            the type of context required to query tree at each tree node
 */
public class NarySearchTree<T, C, Q> extends SearchIndex<T> {

	/**
	 * Intention is for this to be used to instantiate types C and Q when no
	 * context information is required
	 * 
	 * @author Richard Connor
	 *
	 */

	private class TreeNode {
		NaryExclusion<T, C, Q> exclusion;
		private int depth;
		List<TreeNode> subnodes;

		@SuppressWarnings("synthetic-access")
		TreeNode(List<T> data, int depth, C creationContext) {
			this.exclusion = NarySearchTree.this.exclusionFactory.getExclusion(
					data, creationContext);

			this.depth = depth;
			if (!this.exclusion.isDataNode()) {
				this.subnodes = new ArrayList<>();
				List<C> createContexts = this.exclusion.getCreationContexts();
				int ptr = 0;
				for (List<T> x : this.exclusion.getDataPartitions()) {
					this.subnodes.add(new TreeNode(x, depth + 1, createContexts
							.get(ptr++)));
				}
			}
		}

		public int cardinality() {
			int res = 0;
			res += this.exclusion.storedDataSize();
			if (!this.exclusion.isDataNode()) {
				for (TreeNode t : this.subnodes) {
					res += t.cardinality();
				}
			}
			return res;
		}

		public int maxDepth() {
			int res = this.depth;
			if (!this.exclusion.isDataNode()) {
				for (TreeNode t : this.subnodes) {
					res = Math.max(res, t.maxDepth());
				}
			}
			return res;
		}

		public void thresholdSearch(T query, List<T> res, double t,
				Q queryContext) {

			NaryExclusion<T, C, Q>.QueryResult queryInfo = this.exclusion
					.getQueryInfo(query, t, queryContext);

			res.addAll(queryInfo.getResults());
			if (!this.exclusion.isDataNode()) {
				boolean[] excs = queryInfo.getExclusions();
				int ptr = 0;
				for (TreeNode subtree : this.subnodes) {
					if (!excs[ptr]) {
						subtree.thresholdSearch(query, res, t, queryInfo
								.getContexts().get(ptr));
					}
					ptr++;
				}
			}
		}

	}

	private TreeNode theTree;

	private NaryExclusionFactory<T, C, Q> exclusionFactory;

	/**
	 * creates a searchable index structure according to an exclusion strategy
	 * passed in
	 * 
	 * @param data
	 *            data to be searched
	 * @param e
	 *            the exclusion strategy
	 */
	public NarySearchTree(List<T> data, NaryExclusionFactory<T, C, Q> e) {
		super(data, e.getMetric());
		this.exclusionFactory = e;
		this.theTree = new TreeNode(data, 0, null);

	}

	/**
	 * @return the number of data items stored in the tree
	 */
	public int cardinality() {
		return this.theTree.cardinality();
	}

	/**
	 * @return the maximum depth of the tree
	 */
	public int depth() {
		return this.theTree.maxDepth();
	}

	@Override
	public String getShortName() {
		return this.exclusionFactory.getName();
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		List<T> res = new ArrayList<>();
		this.theTree.thresholdSearch(query, res, t, null);
		return res;
	}

}
