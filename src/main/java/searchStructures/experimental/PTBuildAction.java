package searchStructures.experimental;

import java.util.List;

import coreConcepts.Metric;
import searchStructures.SearchIndex;

public class PTBuildAction<T> implements Actions {

	public static void depth_first(int max_depth, int depth, Actions actions) {
		if (depth <= max_depth) {
			actions.nodeAction();
			depth_first(max_depth, depth + 1, actions);
			depth_first(max_depth, depth + 1, actions);
		} else {
			actions.leafAction();
		}
	}

	protected PTBuildAction(List<T> data, Metric<T> metric) {
		int n = data.size();
	}

	static public class PNumGen {
		int left;
		int right;

		public PNumGen() {
			this.left = 0;
			this.right = 1;
		}

		PNumGen getNext() {
			if (this.right - this.left == 1) {
				this.left = 0;
				this.right++;
			} else {
				this.left++;
			}
			return this;
		}
	}

	final PNumGen p = new PNumGen();
	final int[] box = new int[1];

	int leafCount = 0;

	@Override
	public void nodeAction() {
		p.getNext();
	}

	@Override
	public void leafAction() {
		this.leafCount++;
	}

	public static void main(String[] args) {

	}

}
