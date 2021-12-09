package is_paper_experiments.different_sats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import util.Range;
import util.Util_ISpaper;
import coreConcepts.Metric;

public class SAT_global<T> extends SATExclusionFactory<T> {

	protected boolean fourpoint;
	private Arity arity;
	private Map<T, Double> globalDists;

	public static enum Arity {
		unlimited, log, fixed
	}

	public SAT_global(Metric<T> metric, boolean fourPoint, Arity arity,
			boolean satOut) {
		super(metric, satOut);
		assert arity != Arity.unlimited;
		this.fourpoint = fourPoint;
		this.arity = arity;
	}

	@Override
	protected List<T> getReferencePoints(List<T> data, T centre) {
		assert (data.size() > 0);

		/*
		 * this is taken directly from the paper in IS journal
		 */
		List<T> neighbours = new ArrayList<>();
		/*
		 * now fix an order for data - if it exists, use it; otherwise create
		 * and use it
		 */
		if (this.firstCall) {
			this.globalDists = new HashMap<>();
			for (T datum : data) {
				this.globalDists
						.put(datum, this.metric.distance(centre, datum));
			}
		}

		ObjectWithDistance<T>[] owds = new ObjectWithDistance[data.size()];
		for (int i : new Range(0, data.size())) {
			final Double distance = this.globalDists.get(data.get(i));
			assert distance != null;
			owds[i] = new ObjectWithDistance<>(data.get(i), distance);
		}
		Quicksort.sort(owds);

		boolean finished = false;
		int objPtr = owds.length - 1;
		assert (objPtr >= 0);

		while (!finished && objPtr >= 0) {
			neighbours.add(owds[objPtr].getValue());
			if (this.arity == Arity.fixed && neighbours.size() >= 4) {
				finished = true;
			} else if (this.arity == Arity.log
					&& neighbours.size() >= Math.log(data.size())) {
				finished = true;
			}
			objPtr--;
		}
		assert neighbours.size() > 0;
		return neighbours;
	}

	@Override
	protected boolean useFourPointProperty() {
		return this.fourpoint;
	}

	@Override
	protected boolean useSatProperty() {
		return false;
		// return this.arity == Arity.unlimited;
	}

	@Override
	public String getName() {
		return "SAT_global_" + this.arity + (this.fourpoint ? "_f" : "");
	}

}
