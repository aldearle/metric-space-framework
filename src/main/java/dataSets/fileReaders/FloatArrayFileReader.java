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
public class FloatArrayFileReader implements DataSet<float[]> {

	private static float[] getFloatVectorFromLine(String s, int itemId,
			int dimension) throws Exception {
		Scanner s1 = new Scanner(s);
		float[] p = new float[dimension];

		try {
			populateValueArray(s1, p, dimension);
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

	private static void populateValueArray(Scanner s1, float[] p, int dimension) {

		/*
		 * only scan the strings once; might as well keep them here as anywhere
		 * else
		 */
		for (int i = 0; i < dimension; i++) {
			p[i] = s1.nextFloat();
		}

	}

	/*
	 * left as default access to potentially improve efficiency of Iterator
	 * access methods
	 */
	float[][] data;

	private String pathname;

	private Random rand;

	/**
	 * Creates a RealArrayFileReader from a previously saved version; this might
	 * be a bit smaller and a lot faster to read
	 * 
	 * @param f
	 *            the file containing the data
	 * @throws Exception 
	 */
	public FloatArrayFileReader(String s, int dataPoints, int dimensions)
			throws Exception {
		File nextFile = new File(s);

		this.data = new float[dataPoints][0];
		/*
		 * tracks the global item id
		 */
		int item = 0;

		LineNumberReader lr = new LineNumberReader(new FileReader(nextFile));
		/*
		 * and for each line in the file, ie each data point
		 */
		for (String line = lr.readLine(); line != null; line = lr.readLine()) {
			float[] point = getFloatVectorFromLine(line, item, dimensions);
			this.data[item++] = point;
		}
		lr.close();
	}

	public FloatArrayFileReader(File f) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		this.pathname = f.getAbsolutePath();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
		this.data = (float[][]) ois.readObject();
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
	public FloatArrayFileReader(int dataPoints, int dimensions, String dirPath,
			String fName, String fExt, int first, int last, boolean normalised)
			throws Exception {
		this.pathname = dirPath;
		this.data = new float[dataPoints][0];
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
				float[] point = getFloatVectorFromLine(line, item, dimensions);
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
	public float[][] getRawData() {
		return this.data;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	@Override
	public Iterator<float[]> iterator() {
		Iterator<float[]> res = new Iterator<float[]>() {
			int ptr = 0;

			@Override
			public boolean hasNext() {
				return this.ptr < FloatArrayFileReader.this.data.length;
			}

			@Override
			public float[] next() {
				return FloatArrayFileReader.this.data[this.ptr++];
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
	public float[] randomValue() {
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

	@Override
	public String getDataSetShortName() {
		// TODO Auto-generated method stub
		return "floatsFromFile";
	}

}
