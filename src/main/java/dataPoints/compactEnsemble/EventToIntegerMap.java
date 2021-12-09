package dataPoints.compactEnsemble;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import util.ConstantsAndArith;
import util.Multiset;
import util.OrderedList;

/**
 * 
 * class to keep a map of event/cardinality to integers as used in a compact
 * ensemble
 * 
 * @author Richard Connor
 * 
 * @param <T>
 *            the type of event being encoded
 */
public class EventToIntegerMap<T> {

	/**
	 * returns the cardinality of an encoded event
	 * 
	 * @param encoded
	 *            the encoded event
	 * @return the cardinality
	 */
	public static int getCard(int encoded) {
		return encoded % ConstantsAndArith.sixteenBitModMask;
	}

	/**
	 * returns the event code of an encoded event
	 * 
	 * @param encoded
	 *            the encoded event
	 * @return the event
	 */
	public static int getEventCode(int encoded) {
		return encoded >> 16;
	}

	/**
	 * splits the integer space into two 16-bit chunks, uses the upper for the
	 * event and the lower for the cardinality
	 * 
	 * @param eventId
	 * @param cardinality
	 * @return the encoded event/cardinality
	 * @throws Exception
	 */
	public static int toEncodedEventId(int eventId, int cardinality)
			throws Exception {
		if (eventId >= ConstantsAndArith.sixteenBitModMask
				|| cardinality >= ConstantsAndArith.sixteenBitModMask) {
			throw new Exception("encoded event threshold exceeded");
		}
		return (eventId << 16) + cardinality;
	}

	private Map<T, Integer> eventMap = new HashMap<>();

	private int nextEventId = 0;

	private Map<Integer, T> reverseEventMap = new HashMap<>();

	/**
	 * should only ever be used during debug mode; add overhead but not a huge
	 * amount...
	 * 
	 * @param eventId
	 * @return the encoded event
	 */
	@SuppressWarnings("boxing")
	public T getEncodedEvent(int eventId) {
		return this.reverseEventMap.get(eventId);
	}

	/**
	 * @param event
	 * @return the integer code that is associated with this event
	 */
	@SuppressWarnings("boxing")
	public int getEventCode(T event) {
		if (this.eventMap.containsKey(event)) {
			return this.eventMap.get(event);
		} else {
			this.eventMap.put(event, this.nextEventId);
			this.reverseEventMap.put(this.nextEventId, event);

			return this.nextEventId++;
		}
	}

	/**
	 * If you have a collection of CompactEnsembles based on an existing
	 * EventToIntegerMap, then this returns a new EventToIntegerMap in which the
	 * smallest integers correspond to the least common events; useful for
	 * optimising distances
	 * 
	 * @param eToi
	 *            the EventToIntegerMap used to build the collection
	 * 
	 * @param dataSet
	 *            the test collection
	 * 
	 * @return the new EventToIntegerMap
	 */
	@SuppressWarnings("boxing")
	public static <S extends CompactEnsemble,T> EventToIntegerMap<T> getOrderedVersion(
			EventToIntegerMap<T> eToi, Collection<S> dataSet) {
		Multiset<Integer> all = new Multiset<>();

		for (S ce : dataSet) {
			int[] v = ce.getEnsemble();
			for (int i : v) {
				int ev = EventToIntegerMap.getEventCode(i);
				int card = EventToIntegerMap.getCard(i);
				all.add(ev, card);
			}
		}

		OrderedList<Integer, Integer> ol = new OrderedList<>(
				all.keySet().size());
		for (int ev : all.keySet()) {
			ol.add(ev, all.get(ev));
		}

		EventToIntegerMap<T> res = new EventToIntegerMap<>();
		for (int evCoded : ol.getList()) {
			res.getEventCode(eToi.getEncodedEvent(evCoded));
		}

		return res;
	}

	/**
	 * splits the integer space into two 16-bit chunks, uses the upper for the
	 * event and the lower for the cardinality
	 * 
	 * @param event
	 * @param cardinality
	 * @return the encoded event/cardinality
	 * @throws Exception
	 */
	public int toEncodedEvent(T event, int cardinality) throws Exception {
		int eventId = getEventCode(event);
		if (eventId >= ConstantsAndArith.sixteenBitModMask
				|| cardinality >= ConstantsAndArith.sixteenBitModMask) {
			throw new Exception("encoded event threshold exceeded");
		}
		return (eventId << 16) + cardinality;
	}

}
