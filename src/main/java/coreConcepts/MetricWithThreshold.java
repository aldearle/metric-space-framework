package coreConcepts;

import coreConcepts.Metric;

/**
 * @author Richard Connor
 * 
 * @param <T>
 *            The class of the object over which the threshold distance is
 *            defined.
 */
public interface MetricWithThreshold<T> extends Metric<T> {

	/**
	 * Returns the distance between two objects, only if it is within the
	 * threshold supplied
	 * 
	 * @param x
	 *            the first object
	 * @param y
	 *            the second object
	 * @param threshold
	 *            the threshold. nb that this is not a distance, but a value
	 *            returned from the threshold method of the class
	 * @return the distance, if within the threshold, and -1 otherwise
	 */
	public double thresholdDistance(T x, T y, double threshold);

	/**
	 * This returns a threshold value corresponding to a given distance which
	 * should be passed back in to the thesholdDistance method. As this
	 * calculation may be fairly expensive, this method allows its cost to be
	 * amortised over many different distance calculations.
	 * 
	 * @param distance
	 *            the distance threshold required
	 * @return the threshold value to pass in to the thresholdDistance method
	 */
	public double getThreshold(double distance);

	/**
	 * A utility method for performing analysis, not required for use
	 * "in anger".
	 * 
	 * @return the number of comparisons made
	 */
	public int noOfComparisons();

}
