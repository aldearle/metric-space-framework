package semanticDataTypes;

import util.Multiset;
import dataPoints.compactEnsemble.CompactEnsemble;
import dataPoints.compactEnsemble.EventToIntegerMap;

/**
 * This class is used to represent a string as some set of substrings depending
 * on the ShingleType
 * 
 * @author Richard Connor
 * 
 */
public class StringShingle implements CompactEnsemble {

	/**
	 * enumeration of the different types of shingles there may be
	 */
	public enum ShingleType {
		charPair, singlesAndPairs, weightedSingles;
	}

	private int[] fastFingerPrint;
	private int ffpCard = 0;
	private double magnitude = -1;

	private String stringValue;
	private ShingleType type;

	/**
	 * create a new StringShingle based on a string and ShingleType
	 * 
	 * getEnsemble() will be evaluated lazily as required although this is
	 * possibly a worthless optimisation in most cases
	 * 
	 * currently undergoing change to use an EventToIntegerMap instead of
	 * doubling up the characters to comply with updated concept of
	 * CompactEnsemble
	 * 
	 * @param theString
	 * @param type
	 * @param eToiMap
	 * @throws Exception
	 */
	public StringShingle(String theString, ShingleType type,
			EventToIntegerMap<String> eToiMap) throws Exception {
		this.stringValue = theString;
		this.type = type;
		switch (type) {
		case charPair: {
			this.ffpCard = theString.length() + 1;
		}
			;
			break;
		case singlesAndPairs: {
			this.ffpCard = (2 * theString.length()) + 1;
		}
			;
			break;
		case weightedSingles: {
			this.ffpCard = (3 * theString.length()) + 1;
		}
			;
			break;
		default: {
			System.out.println("ERROR: bad shingle type");
		}
		}

		this.createEnsemble(eToiMap);

	}

	@Override
	public int getCardinality() {
		if (this.fastFingerPrint == null) {
			getEnsemble();
		}
		return this.ffpCard;
	}

	/**
	 * @return An integer array encoding the shingle multiset of the string
	 * 
	 */
	@Override
	public int[] getEnsemble() {
		return this.fastFingerPrint;
	}

	/*
	 * The string of length n is represented as a multiset of all character
	 * pairs within the string, also a single pair <space,c> for the first
	 * character and a pair <c,space> for the last character - this is to allow
	 * word reordering to give equivalent strings
	 * 
	 * Each element of the array contains a multiset field encoded as a single
	 * integer: the top two bytes are the event key of the shingle, and the last
	 * two bytes contains the cardinality of that shingle
	 */
	@SuppressWarnings("boxing")
	private void createEnsemble(EventToIntegerMap<String> eToi)
			throws Exception {

		Multiset<Integer> events = new Multiset<Integer>();

		char prevChar = ' ';
		for (int i = 0; i < this.stringValue.length(); i++) {
			char nextChar = this.stringValue.charAt(i);

			if (this.type == ShingleType.singlesAndPairs) {
				events.add(eToi.getEventCode("" + nextChar));
			} else if (this.type == ShingleType.weightedSingles) {
				events.add(eToi.getEventCode("" + nextChar), 2);
			}

			String charPair = ("" + prevChar) + nextChar;
			events.add(eToi.getEventCode(charPair));

			if (i == this.stringValue.length() - 1) {
				String lastPair = ("" + nextChar) + ' ';
				events.add(eToi.getEventCode(lastPair));
			}

			prevChar = nextChar;
		}

		int[] newRes = new int[events.keySet().size()];

		int counter = 0;
		for (int i : events.keySet()) {
			int cell = EventToIntegerMap.toEncodedEventId(i, events.get(i));
			newRes[counter++] = cell;
		}

		this.fastFingerPrint = newRes;

	}

	/**
	 * gives the value of the vector magnitude as required for Cosine distance
	 * 
	 * @return the magnitude
	 */
	public double getVectorMagnitude() {
		if (this.magnitude == -1) {
			int[] v = getEnsemble();

			double acc = 0.0;
			for (int i : v) {
				int mag = EventToIntegerMap.getCard(i);
				acc += mag * mag;
			}

			this.magnitude = Math.sqrt(acc);
		}

		return this.magnitude;
	}

	@Override
	public String toString() {
		return this.stringValue;
	}

}
