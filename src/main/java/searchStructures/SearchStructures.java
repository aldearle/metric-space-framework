package searchStructures;

import java.util.ArrayList;
import java.util.List;

import coreConcepts.Metric;

public class SearchStructures<T> {

	Metric<T> metric;
	List<T> data;

	public SearchStructures(List<T> data, Metric<T> metric) {
		this.metric = metric;
		this.data = data;
	}

	public SearchIndex<T> getSearchStructure(String name) throws Exception {
		SearchIndex<T> res = null;
		switch (name) {
		case "Serial":
			res = new SerialSearch<>(copyData(), this.metric);
			break;
		case "VPT":
			res = new VPTree<>(copyData(), this.metric);
			break;
		case "GHT": {
			final GHTree<T> ghTree = new GHTree<>(copyData(), this.metric);
			ghTree.setCrExclusionEnabled(true);
			ghTree.setVorExclusionEnabled(true);
			res = ghTree;
		}
			break;
		case "GHTr": {
			final GHTree<T> ghTree = new GHTree<>(copyData(), this.metric);
			ghTree.setCrExclusionEnabled(true);
			ghTree.setCosExclusionEnabled(true);
			res = ghTree;
		}
			break;
		default:
			throw new Exception("no such search index");
		}

		return res;
	}

	public List<String> getSearchStructureNames() {
		String[] names = { "Serial", "VPT", "GHT", "GHTr", "DiSAT", "SAT" };

		List<String> res = new ArrayList<>();
		for (String name : names) {
			res.add(name);
		}

		return res;
	}

	private List<T> copyData() {
		List<T> res = new ArrayList<>();
		for (T d : this.data) {
			res.add(d);
		}
		return res;
	}
}
