package dataPoints.cartesian;

/**
 * @author Richard Connor
 *
 */
public class NamedCartesianPoint extends CartesianPoint {

	private String name;

	/**
	 * Creates a CartesianPoint associated with a name
	 * 
	 * @param name the name of the point
	 * @param point the point
	 */
	public NamedCartesianPoint(String name, double[] point) {
		super(point);
		this.name = name;
	}

	/**
	 * @return the name of the point
	 */
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dataPoints.cartesian.CartesianPoint#toString()
	 */
	@Override
	public String toString() {
		return this.name + ':' + super.toString();
	}

}
