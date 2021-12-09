package dataPoints.compactEnsemble;

/**
 * @author Richard Connor
 * 
 *         A CompactEnsemble is an array of integers each containing an encoded
 *         event along with its cardinality
 * 
 *         the top two bytes of each integer is the event and the last two bytes
 *         are its cardinality
 * 
 *         this should perhaps not be fixed in this manner and there may well be
 *         much more efficient ways of encoding most data sets...
 * 
 */
public interface CompactEnsemble {

	/**
	 * so that this structure can work properly in an inverted index
	 * implementation, it is essential that the two bytes used to encode the
	 * event are dense, ie number from 0 up to the number of different events.
	 * 
	 * @return an array of integers in whcih event ids and cardinalities are
	 *         packed.
	 */
	public int[] getEnsemble();

	/**
	 * @return the sum of cardinalities returned by getEnsemble
	 */
	public int getCardinality();

}
