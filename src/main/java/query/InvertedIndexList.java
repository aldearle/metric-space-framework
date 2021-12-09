package query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Richard Connor
 * 
 *         The inverted index is a map from events (held as a short to save
 *         space, probably unnecessarily...) to a map from object ids to
 *         frequencies
 */
public class InvertedIndexList extends HashMap<Short, Map<Integer, Double>> {

	/**
	 * TODO could model this as a map from long to double... or from int to
	 * double if the event space and number of ids is small enough
	 */

	private static final double LOG_2 = Math.log(2);

	public static class IndexPair {
		public double val;
		public int id;

		IndexPair(int id, double val) {
			this.id = id;
			this.val = val;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1925477784940993614L;
	public int noOfEntries = 0;

	// private Map<Double, Double> logMemo = new HashMap<Double, Double>();

	public void add(short event, int id, double freq) {
		if (get(event) != null) {
			get(event).put(id, freq);
		} else {
			Map<Integer, Double> eventMap = new HashMap<Integer, Double>();
			eventMap.put(id, freq);
			put(event, eventMap);
		}

		noOfEntries++;
	}

	public List<IndexPair> getSedClosenessList(short event, double freq) {
		List<IndexPair> res = new ArrayList<IndexPair>();
		if (this.containsKey(event)) {

			double term1 = xLog2x(freq);

			final Map<Integer, Double> eventMap = this.get(event);
			for (int objectId : eventMap.keySet()) {
				final Double objFreq = eventMap.get(objectId);

				double term2 = xLog2x(objFreq);
				double term3 = xLog2x((objFreq + freq));

				res.add(new IndexPair(objectId, term1 + term2 - term3));
			}
		}

		return res;
	}

	public double xLog2x(double freq) {
		// if (logMemo.containsKey(freq)) {
		// return logMemo.get(freq);
		// } else {
		final double term = -(freq * (Math.log(freq)) / LOG_2);
		// logMemo.put(freq, term);
		return term;
		// }

	}

}
