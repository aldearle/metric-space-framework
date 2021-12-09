package n_point_surrogate;

import is_paper_experiments.binary_partitions.BinaryPartitionFactory;
import is_paper_experiments.binary_partitions.SimpleWidePartition;
import is_paper_experiments.dynamic_binary_partitions.MonotoneHyperplaneTree;
import is_paper_experiments.dynamic_binary_partitions.SearchTree;

import java.util.ArrayList;
import java.util.List;

import searchStructures.SearchIndex;
import searchStructures.VPTree;
import util.Range;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import coreConcepts.NamedObject;

/**
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data
 * 
 *            this class creates reference points and distances
 */
public class SurrogateSpaceCreator<T> extends SearchIndex<T> {

	public enum Premeasured {
		gist
	};

	public enum Type {
		threePoint, fourPoint, nPoint
	};

	List<T> refPoints;
	List<float[]> surrogatePoints3p;
	List<double[][]> surrogatePoints4p;
	List<NamedObject<float[]>> surrogatePointsNp;
	Simplex_float<T> surrogateSimplexNp;
	List<SimplexND<T>> surrogateSimplexes4p;
	private int dimensions;
	VPTree<float[]> surrogateIndex3p;
	VPTree<double[][]> surrogateIndex4p;
	SearchIndex<NamedObject<float[]>> surrogateIndexNp;
	CountedMetric<float[]> surMetric;
	CountedMetric<double[][]> surMetric4p;
	CountedMetric<NamedObject<float[]>> surMetricNp;
	private Type surrogateType;

	SurrogateSpaceCreator(List<T> data, Metric<T> metric, Type type) {
		super(data, metric);
		// this.metric = metric;
		this.surrogateType = type;
	}

	SurrogateSpaceCreator(Premeasured type) {
		super(null, null);
	}

	public void addDataNp(List<T> refPoints) throws Exception {
		this.refPoints = refPoints;
		this.dimensions = refPoints.size();

		this.surrogatePointsNp = new ArrayList<>();
		this.surrogateSimplexNp = new Simplex_float(refPoints.size(), metric,
				refPoints);
		for (int i : Range.range(0, this.data.size())) {
			T datum = this.data.get(i);
			float[] surP = getNpSurPoint(datum);
			NamedObject<float[]> no = new NamedObject<>(surP, "" + i);
			this.surrogatePointsNp.add(no);
		}

		this.surMetricNp = new CountedMetric<>(l2());
		// this.surrogateIndexNp = new VPTree<>(this.surrogatePointsNp,
		// this.surMetricNp);

		BinaryPartitionFactory<NamedObject<float[]>> sw = new SimpleWidePartition<>(
				this.surMetricNp);
		final MonotoneHyperplaneTree<NamedObject<float[]>> mhpt = new MonotoneHyperplaneTree<>(
				this.surMetricNp);
		mhpt.setFourPoint(true);
		mhpt.setPartitionStrategy(sw);

		this.surrogateIndexNp = new SearchTree<>(this.surrogatePointsNp, mhpt);

		this.surMetricNp.reset();
	}

	public void addData4p(List<T> refPoints) {
		this.refPoints = refPoints;
		this.dimensions = refPoints.size();
		formSimplexes();

		this.surrogatePoints4p = new ArrayList<>();
		for (T datum : this.data) {
			double[][] surP = get4pSurPoint(datum);
			this.surrogatePoints4p.add(surP);
		}

		this.surMetric4p = new CountedMetric<>(maxL2());
		this.surrogateIndex4p = new VPTree<>(this.surrogatePoints4p,
				this.surMetric4p);

		this.surMetric4p.reset();
	}

	private void formSimplexes() {
		this.surrogateSimplexes4p = new ArrayList<>();
		for (int i : Range.range(0, this.dimensions - 1)) {
			for (int j : Range.range(i + 1, this.dimensions)) {
				assert i != j : "i and j are the same";
				final T x = this.refPoints.get(i);
				final T y = this.refPoints.get(j);
				assert x != y : "x and y are the same";
				List<T> refs = new ArrayList<>();
				refs.add(x);
				refs.add(y);
				try {
					SimplexND<T> sim = new SimplexND<>(2, this.metric, refs);
					this.surrogateSimplexes4p.add(sim);
				} catch (Exception e) {
					throw new RuntimeException("failed to construct simplex: "
							+ e);
				}
			}
		}
		assert this.surrogateSimplexes4p.size() == nChoose2(this.refPoints
				.size()) : "wrong number of simpexes";
	}

	private int nChoose2(int size) {
		return (size * (size - 1)) / 2;
	}

	public double[][] get4pSurPoint(T datum) {
		double[][] surP = new double[nChoose2(dimensions)][2];
		double[] refDists = new double[this.dimensions];
		for (int i = 0; i < refDists.length; i++) {
			refDists[i] = this.metric.distance(datum, this.refPoints.get(i));
		}
		int ptr = 0;
		for (int i : Range.range(0, nChoose2(this.dimensions))) {
			for (int j : Range.range(i + 1, this.dimensions)) {
				double[] pDists = { refDists[i], refDists[j] };
				SimplexND<T> sim = this.surrogateSimplexes4p.get(ptr++);
				double[] apex = sim.formSimplex(pDists);
				surP[i] = apex;
			}
		}
		return surP;
	}

	public float[] getNpSurPoint(T datum) {
		float[] dists = new float[this.refPoints.size()];
		for (int i : Range.range(0, this.refPoints.size())) {
			T ref = this.refPoints.get(i);
			dists[i] = (float) metric.distance(datum, ref);
		}
		return this.surrogateSimplexNp.formSimplex(dists);
	}

	public void addData3p(List<T> refPoints) {
		this.refPoints = refPoints;
		this.dimensions = refPoints.size();
		this.surrogatePoints3p = new ArrayList<>();
		for (T datum : this.data) {
			float[] sur = get3pSurPoint(datum);
			this.surrogatePoints3p.add(sur);
		}

		this.surMetric = new CountedMetric<>(cheby());
		this.surrogateIndex3p = new VPTree<>(this.surrogatePoints3p,
				this.surMetric);
		this.surMetric.reset();
	}

	public float[] get3pSurPoint(T datum) {
		float[] sur = new float[this.dimensions];
		for (int i : Range.range(0, this.dimensions)) {
			T ref = this.refPoints.get(i);
			float dist = (float) metric.distance(datum, ref);
			sur[i] = dist;
		}
		return sur;
	}

	public SearchIndex<T> threePointIndex() {
		return null;
	}

	public Metric<float[]> cheby() {
		return new Metric<float[]>() {

			@Override
			public double distance(float[] x, float[] y) {
				double max = 0;
				for (int i : Range.range(0, x.length)) {
					max = Math.max(max, Math.abs(x[i] - y[i]));
				}
				return max;
			}

			@Override
			public String getMetricName() {
				return "l1_cheby";
			}
		};
	}

	private double l2(float[] x, float[] y) {
		float acc = 0;
		for (int i : Range.range(0, x.length)) {
			double diff = x[i] - y[i];
			acc += diff * diff;
		}
		return Math.sqrt(acc);
	}

	private double l2(double[] x, double[] y) {
		float acc = 0;
		for (int i : Range.range(0, x.length)) {
			double diff = x[i] - y[i];
			acc += diff * diff;
		}
		return Math.sqrt(acc);
	}

	public Metric<NamedObject<float[]>> l2() {
		return new Metric<NamedObject<float[]>>() {

			@SuppressWarnings("synthetic-access")
			@Override
			public double distance(NamedObject<float[]> x,
					NamedObject<float[]> y) {
				return l2(x.object, y.object);
			}

			@Override
			public String getMetricName() {
				return "l2";
			}

		};
	}

	public Metric<double[][]> maxL2() {
		return new Metric<double[][]>() {

			@Override
			public double distance(double[][] x, double[][] y) {
				double max = 0;
				for (int i : Range.range(0, x.length)) {
					max = Math.max(max, l2(x[i], y[i]));
				}
				return max;
			}

			@Override
			public String getMetricName() {
				return "max_l2";
			}
		};
	}

	public List<T> thresholdSearch(T query, double t, double reduction) {
		double surThreshold = t * reduction;
		List<Integer> surs = null;
		switch (this.surrogateType) {
		case threePoint: {
			surs = get3pQuery(query, surThreshold);
		}
			;
			break;
		case fourPoint: {
			surs = get4pQuery(query, surThreshold);
		}
			;
			break;
		case nPoint: {
			surs = new ArrayList<>();
			List<NamedObject<float[]>> sur = getNpQuery(query, surThreshold);
			for (int i : Range.range(0, sur.size())) {
				String nam = sur.get(i).getName();
				surs.add(Integer.parseInt(nam));
			}
		}
			;
			break;
		}

		List<T> res = new ArrayList<>();
		for (int sur : surs) {
			if (this.metric.distance(query, this.data.get(sur)) <= t) {
				res.add(this.data.get(sur));
			}
		}
		return res;
	}

	@Override
	public List<T> thresholdSearch(T query, double t) {
		return thresholdSearch(query, t, 1.0);
	}

	private List<NamedObject<float[]>> getNpQuery(T query, double t) {
		float[] qSur = getNpSurPoint(query);
		NamedObject<float[]> qSurN = new NamedObject<>(qSur, "");
		List<NamedObject<float[]>> surs = this.surrogateIndexNp
				.thresholdSearch(qSurN, t);
		return surs;
	}

	private List<Integer> get4pQuery(T query, double t) {
		double[][] qSur = get4pSurPoint(query);
		List<Integer> surs = this.surrogateIndex4p.thresholdQueryByReference(
				qSur, t);
		return surs;
	}

	private List<Integer> get3pQuery(T query, double t) {
		float[] qSur = new float[this.dimensions];

		for (int i : Range.range(0, this.dimensions)) {
			qSur[i] = (float) this.metric
					.distance(query, this.refPoints.get(i));
		}
		List<Integer> surs = this.surrogateIndex3p.thresholdQueryByReference(
				qSur, t);
		return surs;
	}

	public int countMetricCalls() {
		if (this.surrogateType == Type.fourPoint) {
			return this.surMetric4p.reset();
		} else if (this.surrogateType == Type.nPoint) {
			return this.surMetricNp.reset();
		} else {
			return this.surMetric.reset();
		}
	}

	@Override
	public String getShortName() {
		return "sur_cheby";
	}

}
