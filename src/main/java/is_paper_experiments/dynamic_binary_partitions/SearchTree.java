package is_paper_experiments.dynamic_binary_partitions;

import searchStructures.SearchIndex;

import java.util.ArrayList;
import java.util.List;

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
public class SearchTree<T, C, Q> extends SearchIndex<T> {

	/**
	 * Intention is for this to be used to instantiate types C and Q when no
	 * context information is required
	 * 
	 * @author Richard Connor
	 *
	 */
	public static class Null {
		// placeholder for when no context info is required
	}

	private class TreeNode {
		BinaryExclusion<T, C, Q> exclusion;
		private int depth;
		TreeNode left;
		TreeNode right;

		@SuppressWarnings("synthetic-access")
		TreeNode(List<T> data, int depth, C creationContext) {
			this.exclusion = SearchTree.this.exclusionFactory.getExclusion(
					data, creationContext);

			this.depth = depth;

			final List<T> leftData = this.exclusion.leftData();
			if (leftData.size() > 0) {
				this.left = new TreeNode(leftData, depth + 1,
						this.exclusion.getLeftCreationContext());
			}

			final List<T> rightData = this.exclusion.rightData();
			if (rightData.size() > 0) {
				this.right = new TreeNode(rightData, depth + 1,
						this.exclusion.getRightCreationContext());
			}
		}

		public int cardinality() {
			int res = 0;
			res += this.exclusion.storedDataSize();
			if (this.left != null) {
				res += this.left.cardinality();
			}
			if (this.right != null) {
				res += this.right.cardinality();
			}
			return res;
		}

		public int maxDepth() {
			int res = this.depth;
			if (this.left != null) {
				res = Math.max(this.depth, this.left.maxDepth());
			}
			if (this.right != null) {
				res = Math.max(this.depth, this.right.maxDepth());
			}
			return res;
		}

		public void thresholdSearch(T query, List<T> res, double t,
				Q queryContext) {

			BinaryExclusion<T, C, Q>.ExclusionTest queryInfo = this.exclusion
					.getQueryInfo(query, t, queryContext);

			res.addAll(queryInfo.getResults());

			if (this.left != null && !queryInfo.canExcludeLeft()) {
				this.left.thresholdSearch(query, res, t,
						queryInfo.getLeftQueryContext());
			}
			if (this.right != null && !queryInfo.canExcludeRight()) {
				this.right.thresholdSearch(query, res, t,
						queryInfo.getRightQueryContext());
			}

		}

	}

	private TreeNode theTree;

	private BinaryExclusionFactory<T, C, Q> exclusionFactory;

	/**
	 * creates a searchable index structure according to an exclusion strategy
	 * passed in
	 * 
	 * @param data
	 *            data to be searched
	 * @param e
	 *            the exclusion strategy
	 */
	public SearchTree(List<T> data, BinaryExclusionFactory<T, C, Q> e) {
		super(data, e.getMetric());
		this.exclusionFactory = e;
		this.theTree = new TreeNode(data, 0, null);
	}

	/**
	 * mostly for test purposes
	 * 
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
