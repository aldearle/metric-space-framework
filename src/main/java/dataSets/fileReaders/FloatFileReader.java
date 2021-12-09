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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

/**
 * 
 * A utility class to read a collection of text files each of which contain, on
 * each line, a series of float-formatted strings representing the dimensions
 * of a vector representing an object
 * 
 * Once read, the data can be saved as just the Java array which can save a lot
 * of time and a little space
 * 
 * @author Richard Connor
 * 
 */
public class FloatFileReader extends ArrayList<float[]> {

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

	public FloatFileReader(String s, int dataPoints, int dimensions)
			throws Exception {
		
		File nextFile = new File(s);
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
			this.add(point);
		}
		lr.close();
	}

}
