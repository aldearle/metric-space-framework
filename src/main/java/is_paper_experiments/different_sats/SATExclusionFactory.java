package is_paper_experiments.different_sats;

import is_paper_experiments.n_ary_trees_fourpoint.NaryExclusion;
import is_paper_experiments.n_ary_trees_fourpoint.NaryExclusionFactory;

import java.util.ArrayList;
import java.util.List;

import util.Util_ISpaper;
import coreConcepts.Metric;

/**
 * 
 * An abstract class which captures the full genericity of any n-ary tree
 * construction, including SATs of all flavours. Implementing classes have to
 * provide a mechanism for the selection of SAT neighbours, and methods to give
 * whether this satisifies the original SAT property, and whether the four-point
 * property may be used or not during exclusion (as different contextual
 * information is required during build and query)
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            type of data to be searched
 * 
 *
 */
public abstract class SATExclusionFactory<T> extends
		NaryExclusionFactory<T, List<T>, List<Double>> {

	protected boolean firstCall;
	protected boolean useSatOut;

	/**
	 * @param metric
	 * @param useSatOut
	 */
	public SATExclusionFactory(Metric<T> metric, boolean useSatOut) {
		super(metric);
		this.firstCall = false;
		this.useSatOut = useSatOut;
	}

	private class Sat2Excl extends NaryExclusion<T, List<T>, List<Double>> {

		boolean isNullNode;
		boolean isHeadNode;
		boolean isDataNode;
		List<T> ancestors;
		double[] ancestorToCentreDists;
		double[][] ancestorToNeighbourDists;
		double[] centreToNeighbourDists;
		T centreNode;
		List<T> neighbours;
		List<List<T>> dataSets;
		double[] cr;
		double[][] neighbourDists;

		@SuppressWarnings("synthetic-access")
		Sat2Excl(List<T> initialisingData, List<T> creationInfo) {
			super(initialisingData, creationInfo);

			/*
			 * first, set the head node; either from context, or random choice
			 * if this is the head node
			 */
			if (initialisingData.size() == 0) {
				this.isNullNode = true;
			} else {
				if (creationInfo == null) {
					this.isHeadNode = true;

					if (SATExclusionFactory.this.useSatOut) {
						this.centreNode = Util_ISpaper.getOutlier(
								initialisingData,
								SATExclusionFactory.this.metric, 5);
						initialisingData.remove(this.centreNode);
					} else {
						this.centreNode = initialisingData
								.remove(SATExclusionFactory.this.rand
										.nextInt(initialisingData.size()));
					}

					this.ancestors = new ArrayList<>();
				} else {
					this.centreNode = creationInfo.get(creationInfo.size() - 1);
					this.ancestors = creationInfo;
					if (useSatProperty()) {
						this.ancestorToCentreDists = new double[this.ancestors
								.size()];
						for (int i = 0; i < this.ancestors.size(); i++) {
							this.ancestorToCentreDists[i] = SATExclusionFactory.this.metric
									.distance(this.centreNode,
											this.ancestors.get(i));

						}
					}
				}

				// getReferencePoints is contracted to return any non-empty
				// subset of data, it shouldn't side-effect data
				this.neighbours = getReferencePoints(initialisingData,
						this.centreNode);

				// so if all of the data has been consumed into the neighbours,
				// it's a data node
				if (this.neighbours.size() == initialisingData.size()) {
					this.isDataNode = true;
				} else {
					// there is at least a single data node left, so create all
					// the structure
					this.dataSets = new ArrayList<>();
					for (T p : this.neighbours) {
						boolean removed = initialisingData.remove(p);
						assert removed : "neighbour not in data";
						this.dataSets.add(new ArrayList<T>());
					}

					/*
					 * measure distance from the centre to each neighbour only
					 * if we're going to use the four-point property
					 */
					if (useSatProperty() && useFourPointProperty()) {
						this.centreToNeighbourDists = new double[this.neighbours
								.size()];
						for (int neighb = 0; neighb < this.neighbours.size(); neighb++) {
							this.centreToNeighbourDists[neighb] = SATExclusionFactory.this.metric
									.distance(this.neighbours.get(neighb),
											this.centreNode);
						}
					}

					/*
					 * create the neighbour distance table for four-point
					 * exclusion
					 */
					if (useFourPointProperty()) {
						this.neighbourDists = new double[this.neighbours.size()][this.neighbours
								.size()];
						for (int i = 0; i < this.neighbours.size() - 1; i++) {
							for (int j = i + 1; j < this.neighbours.size(); j++) {
								double dist = SATExclusionFactory.this.metric
										.distance(this.neighbours.get(i),
												this.neighbours.get(j));
								this.neighbourDists[i][j] = dist;
								this.neighbourDists[j][i] = dist;
							}
						}
					}
					/*
					 * create the ancestor to neighbour distance table for
					 * four-point exclusion with the sat property
					 */
					if (useFourPointProperty() && useSatProperty()) {
						this.ancestorToNeighbourDists = new double[this.ancestors
								.size()][this.neighbours.size()];
						for (int anc = 0; anc < this.ancestors.size(); anc++) {
							for (int nei = 0; nei < this.neighbours.size(); nei++) {
								this.ancestorToNeighbourDists[anc][nei] = SATExclusionFactory.this.metric
										.distance(this.ancestors.get(anc),
												this.neighbours.get(nei));
							}
						}
					}

					// create array for cover radii
					this.cr = new double[this.neighbours.size()];
					for (T dat : initialisingData) {
						int closest = -1;
						double leastDist = Double.MAX_VALUE;
						int cptr = 0;
						for (T n : this.neighbours) {
							final double dist = SATExclusionFactory.this.metric
									.distance(n, dat);
							if (dist < leastDist) {
								closest = cptr;
								leastDist = dist;
							}
							cptr++;
						}
						this.dataSets.get(closest).add(dat);
						this.cr[closest] = Math
								.max(leastDist, this.cr[closest]);
					}
				}
			}
		}

		@SuppressWarnings({ "boxing", "synthetic-access" })
		@Override
		public QueryResult getQueryInfo(T query, double threshold,
				List<Double> ancestorToQueryDists) {

			QueryResult res = new QueryResult(this.getArity());

			if (!this.isNullNode) {

				assert this.ancestors != null : "null ancestors!"
						+ this.isHeadNode;
				assert (ancestorToQueryDists == null) ? this.ancestors.size() == 0
						: true : "this won't happen";
				assert (ancestorToQueryDists != null) ? this.ancestors.size() == ancestorToQueryDists
						.size() : true : "ancs and anc dists wrong sizes";

				/*
				 * either calculate, or import from upper node, the distance
				 * from the query to the centre node
				 */
				double queryToCentreDist;
				if (this.isHeadNode) {
					queryToCentreDist = SATExclusionFactory.this.metric
							.distance(query, this.centreNode);
					if (queryToCentreDist <= threshold) {
						res.addResult(this.centreNode);
					}
				} else {
					queryToCentreDist = ancestorToQueryDists
							.get(ancestorToQueryDists.size() - 1);
				}

				double[] queryToNeighbourDists = new double[this.neighbours
						.size()];
				for (int i = 0; i < this.neighbours.size(); i++) {
					T d = this.neighbours.get(i);
					double dist = SATExclusionFactory.this.metric.distance(d,
							query);
					queryToNeighbourDists[i] = dist;
					if (dist <= threshold) {
						res.addResult(d);
					}

					List<Double> newQcontext = new ArrayList<>();
					if (ancestorToQueryDists == null) {

						newQcontext.add(queryToCentreDist);
					} else {
						newQcontext.addAll(ancestorToQueryDists);
					}
					newQcontext.add(dist);
					res.setQueryContext(i, newQcontext);

				}
				/*
				 * now calculate exclusions
				 */
				if (!this.isDataNode) {
					/*
					 * go over these nodes in two phases; in the first phase,
					 * check the per-neighbour exclusion possibilities, eg cover
					 * radius
					 * 
					 * in the second phase, check the per-pair exclusion
					 * possibilities
					 */

					for (int partition = 0; partition < this.neighbours.size(); partition++) {
						boolean partitionExcluded = false;
						/*
						 * check cover radius
						 */
						if (queryToNeighbourDists[partition] > this.cr[partition]
								+ threshold) {
							partitionExcluded = true;
						}
						/*
						 * check sat property exclusion
						 */
						if (useSatProperty() && !useFourPointProperty()) {
							double minDist = queryToCentreDist;
							if (!this.isHeadNode) {
								for (double d : ancestorToQueryDists) {
									minDist = Math.min(minDist, d);
								}
							}
							double d2 = queryToNeighbourDists[partition];
							if (d2 - minDist > 2 * threshold) {
								partitionExcluded = true;
							}
						}

						if (!partitionExcluded && useSatProperty()
								&& useFourPointProperty() && this.isHeadNode) {
							double d1 = queryToCentreDist;
							double d2 = queryToNeighbourDists[partition];
							double d3 = this.centreToNeighbourDists[partition];
							// assert d1 != 0 : "d1 is zero";
							// assert d2 != 0 : "d2 is zero";
							// assert d3 != 0 : "d3 is zero";
							if ((d2 * d2 - d1 * d1) / d3 > 2 * threshold) {
								partitionExcluded = true;
							}
						}

						if (!partitionExcluded && useSatProperty()
								&& useFourPointProperty()) {
							for (int ancestor = 0; ancestor < this.ancestors
									.size(); ancestor++) {
								double d1 = ancestorToQueryDists.get(ancestor);
								double d2 = queryToNeighbourDists[partition];
								double d3 = this.ancestorToNeighbourDists[ancestor][partition];
								// assert d1 != 0 : "d1 is zero";
								// assert d2 != 0 : "d2 is zero";
								// // actually it can be! as can the others but
								// prob not.
								// assert d3 != 0 : "d3 is zero";

								if (ancestor == this.ancestors.size() - 1) {
									assert d1 == queryToCentreDist : "" + d1
											+ ":" + queryToCentreDist
											+ this.isHeadNode;
									assert d2 == queryToNeighbourDists[partition];
									assert d3 == this.centreToNeighbourDists[partition];
								}

								if ((d3 != 0)
										&& (d2 * d2 - d1 * d1) / d3 > 2 * threshold) {
									partitionExcluded = true;
								}
							}
						}

						if (partitionExcluded) {
							res.setExclusion(partition);
						}
					}
					/*
					 * need to check CR property here for last neighbour because
					 * of pairwise checking of other partitions
					 */

					for (int i = 0; i < this.neighbours.size() - 1; i++) {
						double d1 = queryToNeighbourDists[i];

						for (int j = i + 1; j < this.neighbours.size(); j++) {
							double d2 = queryToNeighbourDists[j];

							if (useFourPointProperty()) {

								if ((d1 * d1 - d2 * d2)
										/ this.neighbourDists[i][j] > 2 * threshold) {
									res.setExclusion(i);
								}

								if ((d2 * d2 - d1 * d1)
										/ this.neighbourDists[i][j] > 2 * threshold) {
									res.setExclusion(j);
								}

							} else {
								if ((d1 - d2) > 2 * threshold) {
									res.setExclusion(i);
								}
								if ((d2 - d1) > 2 * threshold) {
									res.setExclusion(j);
								}
							}
						}
					}
				}
			}
			return res;
		}

		@Override
		protected List<List<T>> getDataPartitions() {
			return this.dataSets;
		}

		@Override
		protected int getArity() {
			if (this.neighbours == null) {
				return 0;
			} else {
				return this.neighbours.size();
			}
		}

		@Override
		protected boolean isDataNode() {
			return this.isDataNode || this.isNullNode;
		}

		@Override
		protected List<List<T>> getCreationContexts() {
			List<List<T>> res = new ArrayList<>();
			for (int i = 0; i < this.neighbours.size(); i++) {
				List<T> next = new ArrayList<>();
				if (this.isHeadNode) {
					next.add(this.centreNode);
				} else {
					next.addAll(this.ancestors);
				}
				next.add(this.neighbours.get(i));
				res.add(next);
			}
			return res;
		}

		@Override
		public int storedDataSize() {
			if (this.isNullNode) {
				return 0;
			} else if (this.isHeadNode) {
				return this.neighbours.size() + 1;
			} else {
				return this.neighbours.size();
			}
		}
	}

	@Override
	public NaryExclusion<T, List<T>, List<Double>> getExclusion(List<T> data,
			List<T> context) {
		if (context == null) {
			this.firstCall = true;
		} else {
			this.firstCall = false;
		}
		return new Sat2Excl(data, context);
	}

	abstract protected List<T> getReferencePoints(List<T> data, T centre);

	/**
	 * @return true if, and only if, the metric used has the four-point property
	 */
	abstract protected boolean useFourPointProperty();

	/**
	 * @return true if, and only if, the reference point selection has the SAT
	 *         property; that is, there are no points within the remaining set
	 *         that are closer to the centre point than at least one of the
	 *         reference points
	 */
	abstract protected boolean useSatProperty();

	@Override
	abstract public String getName();

}
