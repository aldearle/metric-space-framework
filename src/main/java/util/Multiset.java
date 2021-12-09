package util;

import java.util.TreeMap;

/**
 * just a multiset!
 * 
 * @author Richard Connor
 * 
 * @param <T>
 *            the type of the elements
 */
public class Multiset<T> extends TreeMap<T, Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1696964956077428945L;
	private int card = 0;

	/**
	 * add an element to the multiset
	 * 
	 * @param e
	 *            the element to add
	 */
	@SuppressWarnings({ "boxing" })
	public void add(T e) {
		if (keySet().contains(e)) {
			put(e, get(e) + 1);
		} else {
			put(e, 1);
		}
		this.card++;
	}

	/**
	 * adds a number of a given element to the multiset
	 * 
	 * @param e
	 *            the element to add
	 * @param number
	 *            the number of elements to add
	 */
	@SuppressWarnings({ "boxing" })
	public void add(T e, int number) {
		if (keySet().contains(e)) {
			put(e, get(e) + number);
		} else {
			put(e, number);
		}
		this.card++;
	}

	/**
	 * 
	 * @return the cardinality of the multiset
	 */
	public int cardinality() {
		return this.card;
	}

	/**
	 * 
	 * @param e
	 *            the element
	 * @return the cardinality of the element
	 */
	@SuppressWarnings("boxing")
	public int cardinality(T e) {
		return get(e);
	}

}
