package searchStructures;

import coreConcepts.Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SATGeneric<T> extends SearchIndex<T> {

	private boolean cosineTestEnabled;

	public enum Strategy {
		original, distal, random, richard
	};

	private class SatNode {
		T head;
		double coverRadius;
		List<SatNode> neighbours;
		/*
		 * neighbour distances required only for Hilbert exclusion
		 */
		double[][] neighbourDistances;
		/*
		 * data is populated only for leaf nodes; nb that even a leaf node has a
		 * head and a cover radius
		 */
		boolean leaf;
		List<T> leafData;

		@SuppressWarnings({ "boxing", "synthetic-access" })
		SatNode(T root, List<T> data, double coveringRadius) {
			/**
			 * TODO need to check this ropy implementation: cover radius
			 * (probably works)(checked); sat neighbours (almost definitely
			 * doesn't); min search radius (sceptical about whether that's
			 * generically applicable or not....)
			 */
			/**
			 * TODO could change this so a leaf node contains only a single
			 * datum; more infrastructure, but strictly less distance calcs...
			 * but does that make sense, as can't construct any neighbours!
			 */
			this.head = root;
			this.coverRadius = coveringRadius;
			if (data.size() < 2) {
				this.leaf = true;
				this.leafData = data;
			} else {
				// this.coverRadius = Double.MAX_VALUE;
				/**
				 * TODO do we not care about how many neighbours there are?
				 */
				List<T> neighbs = new SatNeighbours<>(root, data,
						SATGeneric.this.metric,
						SATGeneric.this.constructionStrategy);

				// first, populate the neighbours distances in case we're using
				// Hilbert partitioning...
				this.neighbourDistances = new double[neighbs.size()][neighbs
						.size()];
				for (int i = 0; i < neighbs.size(); i++) {
					for (int j = i + 1; j < neighbs.size(); j++) {
						double d = distance(neighbs.get(i), neighbs.get(j));
						this.neighbourDistances[i][j] = d;
						this.neighbourDistances[j][i] = d;
					}
				}

				// now form substructures, placing each element into the
				// SatNode corresponding to the smallest neighbour distance
				// do this by order of neighbours in list

				Map<Integer, List<T>> subsets = new HashMap<>();
				for (int i = 0; i < neighbs.size(); i++) {
					subsets.put(i, new ArrayList<T>());
				}
				double[] coveringRadii = new double[neighbs.size()];

				// put each element of data into the subset headed by the
				// closest neighbour
				for (T datum : data) {
					int closestNeighbourIndex = 0;
					double smallestDistance = Double.MAX_VALUE;
					for (int ptr = 0; ptr < neighbs.size(); ptr++) {
						T n = neighbs.get(ptr);
						final double d = distance(datum, n);
						if (d < smallestDistance) {
							closestNeighbourIndex = ptr;
							smallestDistance = d;
						}
					}// closestNeighbourIndex is now set for this datum;

					coveringRadii[closestNeighbourIndex] = Math.max(
							coveringRadii[closestNeighbourIndex],
							smallestDistance);

					subsets.get(closestNeighbourIndex).add(datum);
				}// all subsets and covering radii now calculated

				// now recursively create a new subnode for each neighbour, data
				// closest to it, and covering radius
				this.neighbours = new ArrayList<>();
				for (int pntr = 0; pntr < neighbs.size(); pntr++) {
					SatNode n = new SatNode(neighbs.get(pntr),
							subsets.get(pntr), coveringRadii[pntr]);
					this.neighbours.add(n);
				}// finished!
			}
		}

		private int dataSize() {
			if (this.leaf) {
				return 1 + this.leafData.size();
			} else {
				int subSize = 0;
				for (SatNode n : this.neighbours) {
					subSize += n.dataSize();
				}
				return 1 + subSize;
			}
		}

		@SuppressWarnings("synthetic-access")
		private void thresholdQuery(List<T> res, T query, double threshold,
				double headDistance, double dMin) {
			// the distance from the query to the head of this tree has already
			// been calculated at a higher recursive level, so it's passed down
			if (headDistance <= this.coverRadius + threshold) {
				if (headDistance < threshold) {
					res.add(this.head);
				}
				if (this.leaf) {
					for (T d : this.leafData) {
						if (distance(query, d) < threshold) {
							res.add(d);
						}
					}
				} else {

					// calculate first the distance from the query to each
					// neighbour
					double[] nDists = new double[this.neighbours.size()];
					// also, the index of the nearest neighbour, and this
					// distance
					int closestIndex = -1;
					double localDmin = Double.MAX_VALUE;

					for (int ptr = 0; ptr < this.neighbours.size(); ptr++) {
						T thisHead = this.neighbours.get(ptr).head;
						final double distance = distance(query, thisHead);
						nDists[ptr] = distance;
						// TODO sceptical about dMin, need to check
						if (distance < dMin) {
							dMin = distance;
						}

						if (distance < localDmin) {
							localDmin = distance;
							closestIndex = ptr;
						}
					} // localDmin and minIndex now set up
					T closest = this.neighbours.get(closestIndex).head;

					// now, based on the nearest neighbour to the query, and the
					// distances to others, we go through all the neighbours
					// searching only those which might contain solutions
					for (int thisIndex = 0; thisIndex < this.neighbours.size(); thisIndex++) {
						SatNode n = this.neighbours.get(thisIndex);

						if ((nDists[thisIndex] <= localDmin + (2 * threshold))) {
							double closestToThisDist = this.neighbourDistances[closestIndex][thisIndex];
							if (SATGeneric.this.cosineTestEnabled) {
								// no longer applies
							}
							if (!canExcludeCosine(closestToThisDist, localDmin,
									nDists[thisIndex], threshold)) {
								if (nDists[thisIndex] - n.coverRadius < threshold) {
									// currently blanking out the recursive
									// effect
									n.thresholdQuery(res, query, threshold,
											nDists[thisIndex], dMin);
								}
							}
						}// haven't gone in here if simple Voronoi exclusion
							// holds; nb this automatically doesn't include
							// nearest neighbour so no special case for that
					}// finished iterating over child SatNodes
				}
			}// end block checking cover radius; just exit as there are no
				// results... but is this double-checked?
		}
	}

	private SatNode root;
	private Strategy constructionStrategy;

	/**
	 * @param data
	 * @param m
	 * @param s
	 */
	public SATGeneric(List<T> data, Metric<T> m, Strategy s) {
		super(data, m);
		this.setCosineTestEnabled(false);
		this.constructionStrategy = s;
		T root = data.get(0);
		data.remove(0);
		this.root = new SatNode(root, data, Double.MAX_VALUE);
	}

	@Override
	@SuppressWarnings("synthetic-access")
	public List<T> thresholdSearch(T query, double threshold) {
		List<T> res = new ArrayList<>();
		double headDist = this.metric.distance(query, this.root.head);
		this.root.thresholdQuery(res, query, threshold, headDist,
				Double.MAX_VALUE);
		return res;
	}

	/**
	 * @param dCB
	 *            distance between pivots
	 * @param dCQ
	 *            distance from query to closer pivot
	 * @param dBQ
	 *            distance from query to further pivot
	 * @param threshold
	 * @return
	 */
	private boolean canExcludeCosine(double dCB, double dCQ, double dBQ,
			double threshold) {
		if (this.cosineTestEnabled) {
			double cosTheta = (dCB * dCB + dCQ * dCQ - dBQ * dBQ)
					/ (2 * dCB * dCQ);
			double projection = dCQ * cosTheta;
			return (dCB / 2) - projection > threshold;
		} else {
			return false;
		}
	}

	public void setCosineTestEnabled(boolean cosineTestEnabled) {
		this.cosineTestEnabled = cosineTestEnabled;
	}

	public int size() {
		return this.root.dataSize();
	}

	@Override
	public String getShortName() {
		return "satG";
	}

	private double distance(T x, T y) {
		return this.metric.distance(x, y);
	}

}
