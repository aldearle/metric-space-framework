package dataPoints.sparseCartesian;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * The intention of this class is to create an inverted index from a
 * DataSet<CompactEnsemble> so that it can be queried using the thresholded
 * version of JS
 * 
 * @author Richard Connor
 * @param <T>
 * 
 */
public abstract class InvertedIndex<T extends SparseCartesian> {

	/*
	 * the core data structure of the index is a map from event id -> data id ->
	 * frequency
	 * 
	 * frequency, to start with, we are going to store as a single integer
	 * containing count and cardinality
	 * 
	 * or... a single integer can contain id, cardinality and count 16/8/8 bits
	 */

	protected static final double LOG2 = Math.log(2);

	public static void writeDub(String[] args) throws IOException {

		byte[] bytes = new byte[8];
		ByteBuffer.wrap(bytes).putDouble(1.1);

		OutputStream os = new FileOutputStream(new File("test.bin"));
		os.write(bytes);
		os.close();
	}

	protected static double getTermValue(double v_i, double w_i) {
		return (v_i + w_i) * Math.log(v_i + w_i) - v_i * Math.log(v_i) - w_i
				* Math.log(w_i);

	}

	protected Map<Integer, List<Integer>> invertedIndexIds;

	protected Map<Integer, List<Double>> invertedIndexVals;

	protected int noOfDataPoints;

	public InvertedIndex(Collection<T> data) {
		this.invertedIndexIds = new TreeMap<Integer, List<Integer>>();
		this.invertedIndexVals = new TreeMap<Integer, List<Double>>();
		this.noOfDataPoints = data.size();

		int dataId = 0;
		for (T datum : data) {
			int[] dims = datum.getDims();
			double[] vals = datum.getValues();

			int pntr = 0;
			for (int dim : dims) {
				addToIndex(dataId, dim, vals[pntr]);
				pntr++;
			}
			dataId++;
		}
	}

	/**
	 * creates a stored inverted index
	 * 
	 * @param f
	 *            the file, which should be a CSV file stored in the verbose
	 *            format
	 * @throws IOException
	 *             if the file can't be opened
	 */
	@SuppressWarnings("boxing")
	public InvertedIndex(File f) throws IOException {
		this.invertedIndexIds = new TreeMap<Integer, List<Integer>>();
		this.invertedIndexVals = new TreeMap<Integer, List<Double>>();

		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		this.noOfDataPoints = 0;

		for (String line = lr.readLine(); line != null; line = lr.readLine()) {

			final Scanner dimInfoLine = new Scanner(line).useDelimiter(",\\s");
			if (dimInfoLine.hasNextInt()) {

				int dimNo = dimInfoLine.nextInt();
				int noOfEntries = dimInfoLine.nextInt();

				String idIndexLine = lr.readLine();
				Scanner idLine = (new Scanner(idIndexLine))
						.useDelimiter(",\\s");
				String valsIndexLine = lr.readLine();
				Scanner valsLine = (new Scanner(valsIndexLine))
						.useDelimiter(",\\s");

				List<Integer> ids = new ArrayList<Integer>();
				List<Double> vals = new ArrayList<Double>();

				for (int i = 0; i < noOfEntries; i++) {
					final int nextInt = idLine.nextInt();
					ids.add(nextInt);
					if (i == noOfEntries - 1) {
						noOfDataPoints = Math.max(noOfDataPoints, nextInt + 1);
					}
				}
				assert !idLine.hasNextInt() : "id line not fully read";

				for (int i = 0; i < noOfEntries; i++) {
					final double nextDouble = valsLine.nextDouble();
					vals.add(nextDouble);
				}
				assert !idLine.hasNextDouble() : "id line not fully read";

				this.invertedIndexIds.put(dimNo, ids);
				this.invertedIndexVals.put(dimNo, vals);
			}
		}
		assert this.invertedIndexIds != null : "no index has been read from file";
	}

	/**
	 * reads a compact representation from the InputStream
	 * 
	 * @param inputStream
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	public InvertedIndex(InputStream inputStream) throws Exception {
		this.invertedIndexIds = new TreeMap<Integer, List<Integer>>();
		this.invertedIndexVals = new TreeMap<Integer, List<Double>>();

		this.noOfDataPoints = 0;

		byte[] doubleBuffer = new byte[8];
		byte[] intBuffer = new byte[4];

		inputStream.read(intBuffer);
		final int noOfDims = ByteBuffer.wrap(intBuffer).getInt();

		for (int i = 0; i < noOfDims; i++) {

			inputStream.read(intBuffer);
			final int dimNo = ByteBuffer.wrap(intBuffer).getInt();

			inputStream.read(intBuffer);
			final int noOfValues = ByteBuffer.wrap(intBuffer).getInt();

			List<Integer> ids = new ArrayList<Integer>();
			List<Double> vals = new ArrayList<Double>();

			for (int idRef = 0; idRef < noOfValues; idRef++) {

				inputStream.read(intBuffer);
				final int objectId = ByteBuffer.wrap(intBuffer).getInt();
				ids.add(objectId);

				if (idRef == noOfValues - 1) {
					this.noOfDataPoints = Math.max(this.noOfDataPoints,
							objectId + 1);
				}
			}
			for (int idRef = 0; idRef < noOfValues; idRef++) {

				inputStream.read(doubleBuffer);
				double nextDouble = ByteBuffer.wrap(doubleBuffer).getDouble();
				vals.add(nextDouble);
			}

			this.invertedIndexIds.put(dimNo, ids);
			this.invertedIndexVals.put(dimNo, vals);
		}
		inputStream.close();
	}

	public Map<Integer, List<Integer>> getInvertedIndexIds() {
		return invertedIndexIds;
	}

	public Map<Integer, List<Double>> getInvertedIndexVals() {
		return invertedIndexVals;
	}

	/**
	 * @return
	 */
	public String getStats() {
		StringBuffer res = new StringBuffer();
		res.append("total dimensions:" + invertedIndexIds.size());
		res.append(" total data points: " + noOfDataPoints);

		int totalPops = 0;
		for (List<Integer> l : invertedIndexIds.values()) {
			totalPops += l.size();
		}
		res.append(" mean dims per point: " + totalPops
				/ (double) noOfDataPoints);
		return res.toString();
	}

	/**
	 * 
	 * @param query
	 * @param threshold
	 * @return
	 */
	@SuppressWarnings("boxing")
	public abstract List<Integer> nearestNeighbour(T query,
			int numberOfNeighbours);

	/**
	 * @return the number of objects contained in this inverted index
	 */
	public int size() {
		return noOfDataPoints;
	}

	/**
	 * 
	 * @param query
	 * @param threshold
	 * @return
	 */
	@SuppressWarnings("boxing")
	public abstract List<Integer> thresholdQuery(T query, double threshold);

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i : invertedIndexIds.keySet()) {
			List<Integer> ids = invertedIndexIds.get(i);
			List<Double> vals = invertedIndexVals.get(i);
			sb.append("dimension " + i + "\n");
			for (int id : ids) {
				sb.append(id + "\t");
			}
			sb.append("\n");
			for (double val : vals) {
				sb.append(val + "\t");
			}
			sb.append("\n");

		}

		return sb.toString();
	}

	/**
	 * writes a compact version of the index to the given relative filename
	 * 
	 * first is an int for the number of dimensions; then, for each dimension,
	 * ints for the dimension number and the number of non-zero values of that
	 * dimension; then ints represneting the object ids, then 8-byte sequences
	 * representing the doubles for each object at that dimension
	 * 
	 * @param filename
	 *            relative filename for writing the file, probably use ".csv"
	 *            extension
	 * @throws IOException
	 */
	@SuppressWarnings("boxing")
	public void writeCompactIndexFile(String filename) throws IOException {

		OutputStream outputStream = new FileOutputStream(filename);
		byte[] intBuffer = new byte[4];
		byte[] doubleBuffer = new byte[8];

		// write the number of dimensions
		ByteBuffer.wrap(intBuffer).putInt(this.invertedIndexIds.size());
		outputStream.write(intBuffer);

		// then, for each dimension...
		for (int dimension : this.invertedIndexIds.keySet()) {
			List<Integer> ids = this.invertedIndexIds.get(dimension);
			List<Double> vals = this.invertedIndexVals.get(dimension);

			// write the dimension number followed by the number of non-zero
			// objects
			ByteBuffer.wrap(intBuffer).putInt(dimension);
			outputStream.write(intBuffer);
			ByteBuffer.wrap(intBuffer).putInt(ids.size());
			outputStream.write(intBuffer);

			// write the id of each object which is non-zero for this dimension
			for (int idRef = 0; idRef < ids.size(); idRef++) {
				ByteBuffer.wrap(intBuffer).putInt(ids.get(idRef));
				outputStream.write(intBuffer);
			}
			// write the values for each dimension
			for (int idRef = 0; idRef < ids.size(); idRef++) {

				ByteBuffer.wrap(doubleBuffer).putDouble(vals.get(idRef));
				outputStream.write(doubleBuffer);

			}
		}

		outputStream.close();
	}

	/**
	 * writes a long-winded version of the index to the given relative filename
	 * 
	 * each dimension of the index is encoded in three lines: first line is
	 * dimension number followed by number of entries; second line is object
	 * ids, third line is values for respective ids in this dimension
	 * 
	 * all fields are separated by ", " making this a valid CSV file
	 * 
	 * the space was an annoying mistake but it's there now!
	 * 
	 * @param filename
	 *            relative filename for writing the file, probably use ".csv"
	 *            extension
	 * @throws FileNotFoundException
	 *             if the file can't be created
	 */
	@SuppressWarnings("boxing")
	public void writeVerboseIndexFile(String filename)
			throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(filename);

		for (int dimension : invertedIndexIds.keySet()) {
			List<Integer> ids = this.invertedIndexIds.get(dimension);
			List<Double> vals = this.invertedIndexVals.get(dimension);
			pw.println(dimension + ", " + ids.size());
			for (int idRef = 0; idRef < ids.size(); idRef++) {
				pw.print(ids.get(idRef));
				if (idRef + 1 < ids.size()) {
					pw.print(", ");
				}

			}
			pw.println();
			for (int idRef = 0; idRef < ids.size(); idRef++) {
				pw.print(vals.get(idRef));
				if (idRef + 1 < ids.size()) {
					pw.print(", ");
				}
			}
			pw.println();
		}

		FileOutputStream fo;
		pw.close();
	}

	@SuppressWarnings("boxing")
	protected void addToIndex(int dataId, int dimension, double value) {
		List<Integer> l = this.invertedIndexIds.get(dimension);
		List<Double> v = this.invertedIndexVals.get(dimension);
		if (l == null) {
			l = new ArrayList<Integer>();
			v = new ArrayList<Double>();
			this.invertedIndexIds.put(dimension, l);
			this.invertedIndexVals.put(dimension, v);
		}
		l.add(dataId);
		v.add(value);
	}

}
