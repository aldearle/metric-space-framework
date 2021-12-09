package coreConcepts;

import java.util.Iterator;

public class DataSetImpl<T> implements DataSet<T> {

	Iterable<T> it;
	private String datasetName;
	private boolean isFinite;

	/**
	 * @param it
	 * @param shortname
	 * @param isFinite
	 * 
	 *            Creates a DataSet from an iterable; randomValue() and size()
	 *            are not implemented and should not be called
	 */
	public DataSetImpl(Iterable<T> it, String shortname, boolean isFinite) {
		this.it = it;
		this.datasetName = shortname;
		this.isFinite = isFinite;
	}

	@Override
	public Iterator<T> iterator() {
		return this.it.iterator();
	}

	@Override
	public boolean isFinite() {
		return this.isFinite;
	}

	@Override
	public T randomValue() {
		return null;
	}

	@Override
	public String getDataSetName() {
		return this.datasetName;
	}

	@Override
	public String getDataSetShortName() {
		return this.datasetName;
	}

	@Override
	public int size() {
		return -1;
	}

}
