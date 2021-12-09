package is_paper_experiments.different_sats;

import java.util.List;

import util.Util_ISpaper;
import coreConcepts.Metric;

/**
 * A generic class to produce hyperplane tree exclusion mechanisms
 * 
 * @author Richard Connor
 *
 * @param <T>
 *            the type of the data to be searched
 */
public class HPT_random<T> extends SATExclusionFactory<T> {

	protected boolean fourpoint;
	protected boolean fft;

	/**
	 * 
	 * max number of subnodes; unlimited should only be used with SATs
	 * 
	 * @author Richard Connor
	 *
	 */
	public static enum Arity {
		unlimited, log, fixed, binary
	}

	Arity arity;

	public HPT_random(Metric<T> metric, boolean fourPoint, Arity arity,
			boolean fft) {
		super(metric, false);
		assert arity != Arity.unlimited;
		this.fourpoint = fourPoint;
		this.fft = fft;
		this.arity = arity;
	}

	@Override
	protected List<T> getReferencePoints(List<T> data, T centre) {

		if (this.fft) {
			if (this.arity == Arity.binary) {
				return Util_ISpaper.getFFT(data, metric, 2);
			} else if (this.arity == Arity.fixed) {
				return Util_ISpaper.getFFT(data, metric, 4);
			} else if (this.arity == Arity.log) {
				return Util_ISpaper.getFFT(data, metric,
						Math.max(2, (int) Math.floor(Math.log(data.size()))));
			} else {
				throw new RuntimeException(
						"bad arity for generic hyperplane tree");
			}
		} else {
			if (this.arity == Arity.binary) {
				return Util_ISpaper.getRandom(data, 2);
			} else if (this.arity == Arity.fixed) {
				return Util_ISpaper.getRandom(data, 4);
			} else if (this.arity == Arity.log) {
				return Util_ISpaper.getRandom(data,
						Math.max(2, (int) Math.floor(Math.log(data.size()))));
			} else {
				throw new RuntimeException(
						"bad arity for generic hyperplane tree");
			}
		}
	}

	@Override
	protected boolean useFourPointProperty() {
		return this.fourpoint;
	}

	@Override
	protected boolean useSatProperty() {
		return false;
	}

	@Override
	public String getName() {
		return "HPT_" + (this.fft ? "fft_" : "random_") + this.arity
				+ (this.fourpoint ? "_f" : "");
	}

}
