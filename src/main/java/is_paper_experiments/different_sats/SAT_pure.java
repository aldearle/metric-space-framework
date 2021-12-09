package is_paper_experiments.different_sats;

import java.util.ArrayList;
import java.util.List;

import searchStructures.ObjectWithDistance;
import searchStructures.Quicksort;
import coreConcepts.Metric;

public class SAT_pure<T> extends SATExclusionFactory<T> {

	private boolean fourPoint;

	public SAT_pure(Metric metric, boolean fourPoint, boolean satOut) {
		super(metric, satOut);
		this.fourPoint = fourPoint;
	}

	@Override
	protected List<T> getReferencePoints(List<T> data, T centre) {

		ObjectWithDistance<T>[] owds = new ObjectWithDistance[data.size()];
		int ptr = 0;
		for (T dat : data) {
			double dist = this.metric.distance(centre, dat);
			owds[ptr++] = new ObjectWithDistance<>(dat, dist);
		}
		Quicksort.sort(owds);

		List<T> referencePoints = new ArrayList<>();

		for (ObjectWithDistance<T> nowd : owds) {
			T next = nowd.getValue();
			double pDist = nowd.getDistance();

			/*
			 * keep adding closest to neighbours until one of the neighbours is
			 * closer than the centre point - preserve invariant that all
			 * neighbours are closer to centre point than any other neighbour
			 */
			boolean centreIsClosest = true;
			for (T n : referencePoints) {
				if (metric.distance(next, n) <= pDist) {
					centreIsClosest = false;
				}
			}
			if (centreIsClosest) {
				referencePoints.add(next);
			}
		}

		return referencePoints;
	}

	@Override
	protected boolean useFourPointProperty() {
		return fourPoint;
	}

	@Override
	protected boolean useSatProperty() {
		return true;
	}

	@Override
	public String getName() {
		return "SAT_true";
	}

}
