package is_paper_experiments.different_sats;

import java.util.ArrayList;
import java.util.List;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import coreConcepts.Metric;

public class SAT_hybrid<T> extends SATExclusionFactory<T> {

	private boolean fourPoint;
	private boolean satProperty;

	public SAT_hybrid(Metric<T> metric, boolean fourPoint, boolean satProperty,
			boolean satOut) {
		super(metric, satOut);
		this.fourPoint = fourPoint;
		this.satProperty = true;
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
		int ptr = 0;
		for (T dat : data) {
			double dist = this.metric.distance(centre, dat);
			owds[ptr++] = new ObjectWithDistance<>(dat, dist);
		}
		Quicksort.sort(owds);

		int bottom = 0;
		int top = owds.length - 1;
		while (top >= bottom) {
			addNeighbour(neighbours, owds[bottom]);
			if (top > bottom) {
				addNeighbour(neighbours, owds[top]);
			}
			bottom++;
			top--;
		}
		// for (ObjectWithDistance<T> owd : owds) {
		// addNeighbour(neighbours, owd);
		// }

		return neighbours;
	}

	private void addNeighbour(List<T> neighbours, ObjectWithDistance<T> owd) {
		boolean centreNearest = true;
		for (T n : neighbours) {
			if (metric.distance(owd.getValue(), n) < owd.getDistance()) {
				centreNearest = false;
			}
		}
		if (centreNearest) {
			neighbours.add(owd.getValue());
		}
	}

	@Override
	protected boolean useFourPointProperty() {
		return this.fourPoint;
	}

	@Override
	protected boolean useSatProperty() {
		return this.satProperty;
	}

	@Override
	public String getName() {
		return "SAT_hybrid" + (this.satProperty ? "_sat" : "_nosat");
	}

}
