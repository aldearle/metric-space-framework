package dataSets.fileReaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import coreConcepts.DataSet;

/**
 * 
 * A utility class to read a collection of text files each of which contain, on
 * each line, a series of double-formatted strings representing the dimensions
 * of a vector representing an object
 * 
 * Once read, the data can be saved as just the Java array which can save a lot
 * of time and a little space
 * 
 * @author Richard Connor
 * 
 */
public class RealArrayFileReader implements DataSet<double[]> {

	private static double[] getRealVectorFromLine(String s, int itemId,
			int dimension, boolean normalised) throws Exception {
		Scanner s1 = new Scanner(s);
		double[] p = new double[dimension];

		try {
			populateValueArray(s1, p, dimension, normalised);
		} catch (Throwable t) {
			throw new Exception(
					"get vector: badly formed line in file at item " + itemId
							+ ", maybe too few values");
		}
		if (s1.hasNextDouble()) {
			throw new Exception("too many data values in file at item "
					+ itemId);
		}
		return p;
	}

	private static void populateValueArray(Scanner s1, double[] p,
			int dimension, boolean normalised) {

		/*
		 * only scan the strings once; might as well keep them here as anywhere
		 * else
		 */
		double lineSum = 0;
		for (int i = 0; i < dimension; i++) {
			p[i] = s1.nextDouble();
			lineSum += p[i];
		}
		if (normalised) {
			normalise(p, dimension, lineSum);
		}
	}

	private static void normalise(double[] p, int dimension, double lineSum) {
		for (int i = 0; i < dimension; i++) {
			p[i] = p[i] / lineSum;
		}
	}

	/*
	 * left as default access to potentially improve efficiency of Iterator
	 * access methods
	 */
	double[][] data;

	private String pathname;

	private Random rand;

	/**
	 * Creates a RealArrayFileReader from a previously saved version; this might
	 * be a bit smaller and a lot faster to read
	 * 
	 * @param f
	 *            the file containing the data
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public RealArrayFileReader(File f) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		this.pathname = f.getAbsolutePath();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
		this.data = (double[][]) ois.readObject();
		ois.close();
	}

	/**
	 * 
	 * Creates a two-dimensional array representation of a set of data files
	 * each containing double points
	 * 
	 * @param dataPoints
	 *            The total number of points to be read, if this is not the same
	 *            as the number of lines in the files an exception will be
	 *            raised
	 * @param dimensions
	 *            The number of dimensions of each point; every line of every
	 *            file must have this many doubles
	 * @param dirPath
	 *            The root path to the directory where the files are to be found
	 * @param fName
	 *            The base file name, must be the same for every file
	 * @param fExt
	 *            The file extension, must be the same for every file
	 * @param first
	 *            The first file number
	 * @param last
	 *            The last file number
	 * @param normalised
	 *            true if the values read from the file are to be normalised, ie
	 *            so that the values in each data point sum to 1
	 * @throws Exception
	 *             If the files are not correctly present or formatted
	 */
	public RealArrayFileReader(int dataPoints, int dimensions, String dirPath,
			String fName, String fExt, int first, int last, boolean normalised)
			throws Exception {
		this.pathname = dirPath;
		this.data = new double[dataPoints][0];
		/*
		 * tracks the global item id
		 */
		int item = 0;
		/*
		 * for each file in the input set, eg /path/file23.txt...
		 */
		for (int i = first; i <= last; i++) {
			String nextFile = dirPath + '/' + fName + i + fExt;

			LineNumberReader lr = new LineNumberReader(new FileReader(nextFile));
			/*
			 * and for each line in the file, ie each data point
			 */
			for (String line = lr.readLine(); line != null; line = lr
					.readLine()) {
				double[] point = getRealVectorFromLine(line, item, dimensions,
						normalised);
				this.data[item++] = point;

			}

			lr.close();
		}
		/*
		 * by which time we should have fully populated the array
		 */
		if (item != dataPoints) {
			throw new Exception(
					"number of data points specified does not match number found in files");
		}
	}

	@Override
	public String getDataSetName() {
		return "file array from path " + this.pathname;
	}

	/**
	 * @return The underlying two-dimensional array of doubles; please don't
	 *         update this if any other interfaces are being used!
	 */
	public double[][] getRawData() {
		return this.data;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	@Override
	public Iterator<double[]> iterator() {
		Iterator<double[]> res = new Iterator<double[]>() {
			int ptr = 0;

			@Override
			public boolean hasNext() {
				return this.ptr < RealArrayFileReader.this.data.length;
			}

			@Override
			public double[] next() {
				return RealArrayFileReader.this.data[this.ptr++];
			}

			@Override
			public void remove() {
				/*
				 * not implemented
				 */
				assert false : "remove should not be called in RealArrayFileReader";
			}
		};
		return res;
	}

	@Override
	public double[] randomValue() {
		if (this.rand == null) {
			this.rand = new Random(0);
		}
		return this.data[this.rand.nextInt(this.data.length)];
	}

	/**
	 * Save this collection as a Java double[][] object to save reading those
	 * text files again! This might be a lot faster to read
	 * 
	 * @param filename
	 *            where the object is to be saved
	 * @throws IOException
	 *             if the file can't be opened or written to
	 */
	public void saveCollectionToFile(String filename) throws IOException {

		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		oos.writeObject(this.data);

		oos.close();
	}

	@Override
	public int size() {
		return this.data.length;
	}

	/**
	 * normalises each element in the collection, so that the sum of each row is
	 * 1.0
	 */
	public void normalise() {
		int dimension = this.data[0].length;
		for (double[] v : this.data) {
			double sum = 0;
			for (double d : v) {
				sum += d;
			}
			normalise(v, dimension, sum);
		}

	}

	@Override
	public String getDataSetShortName() {
		// TODO Auto-generated method stub
		return "no short name....";
	}

}
