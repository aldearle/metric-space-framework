package is_paper_experiments.different_sats;

import java.util.ArrayList;
import java.util.List;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import util.Range;
import coreConcepts.Metric;

public class SAT_distal<T> extends SATExclusionFactory<T> {

	protected boolean fourpoint;

	public static enum Arity {
		unlimited, log, fixed
	}

	Arity arity;

	public SAT_distal(Metric<T> metric, boolean fourPoint, Arity arity,
			boolean satOut) {
		super(metric, satOut);
		this.fourpoint = fourPoint;
		this.arity = arity;
		if (satOut) {
			this.useSatOut = true;
		}
	}

	@Override
	protected List<T> getReferencePoints(List<T> data, T centre) {
		/*
		 * this is taken directly from the paper in IS journal
		 */
		List<T> neighbours = new ArrayList<>();
		/*
		 * now fix an order for data - we'll use nearest first
		 */
		ObjectWithDistance<T>[] owds = new ObjectWithDistance[data.size()];
		for (int i : new Range(0, data.size())) {
			T dat = data.get(i);
			double dist = this.metric.distance(centre, dat);
			owds[i] = new ObjectWithDistance<>(dat, dist);
		}

		Quicksort.sort(owds);

		boolean finished = false;
		int ptr = owds.length - 1;
		while (!finished) {
			double dVtoA = owds[ptr].getDistance();
			boolean shouldAdd = true;
			for (T n : neighbours) {
				double dVtoN = metric.distance(owds[ptr].getValue(), n);
				if (dVtoN <= dVtoA) {
					// there exists a neighbour closer to this point than the
					// centre
					shouldAdd = false;
				}
			}
			if (shouldAdd) {
				neighbours.add(owds[ptr].getValue());
			}

			if (this.arity == Arity.log
					&& neighbours.size() >= Math.log(data.size())) {
				finished = true;
			} else if (this.arity == Arity.fixed && neighbours.size() == 4) {
				finished = true;
			}
			if (--ptr < 0) {
				finished = true;
			}
		}

		return neighbours;
	}

	@Override
	protected boolean useFourPointProperty() {
		return this.fourpoint;
	}

	@Override
	protected boolean useSatProperty() {
		return this.arity == Arity.unlimited;
	}

	@Override
	public String getName() {
		return "SAT_distal_" + this.arity + (this.fourpoint ? "_f" : "");
	}

}
