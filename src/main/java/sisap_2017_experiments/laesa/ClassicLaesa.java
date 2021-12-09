package sisap_2017_experiments.laesa;

import java.util.ArrayList;
import java.util.List;

import util.Range;
import util.Util_ISpaper;
import coreConcepts.Metric;

public class ClassicLaesa<T> extends Laesa<T> {

	protected double[] memory;

	public ClassicLaesa(Metric<T> metric, int dimension) {
		super(metric, dimension);
	}

	@Override
	public void setDataAndRefPoints(List<T> newData, List<T> refPoints) {
		super.setDataAndRefPoints(newData, refPoints);
		this.memory = new double[newData.size() * this.dim];
	}

	@Override
	public void setupTable() {
		int dataPtr = 0;
		for (T p : this.data) {
			for (int i : Range.range(0, this.dim)) {
				this.memory[(dataPtr * this.dim) + i] = this.metric.distance(p,
						this.refPoints.get(i));
			}
			dataPtr++;
		}
	}

	@Override
	public List<T>[] filter(T query, double threshold) {
		double[] dists = getRefDists(query);
		List<T>[] res = new ArrayList[2];
		res[0] = new ArrayList<>();
		res[1] = new ArrayList<>();
		for (int i : Range.range(0, dists.length)) {
			if (dists[i] <= threshold) {
				res[1].add(this.refPoints.get(i));
			}
		}

		for (int i : Range.range(0, this.data.size())) {
			boolean excluded = false;
			int ptr = 0;
			while (!excluded && ptr < this.dim) {
				double lwb = Math.abs(dists[ptr]
						- this.memory[(i * this.dim) + ptr]);
				if (lwb > threshold) {
					excluded = true;
				} else {
					ptr++;
				}
			}
			if (!excluded) {
				res[0].add(this.data.get(i));
			}
		}
		return res;
	}

	@Override
	public String getName() {
		return "classic";
	}

}
