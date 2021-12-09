package dataSets.fileReaders;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Scanner;

import coreConcepts.NamedObject;

/**
 * 
 * A utility class to read a collection of text files each of which contain, on
 * each line, a series of float-formatted strings representing the dimensions of
 * a vector representing an object
 * 
 * Once read, the data can be saved as just the Java array which can save a lot
 * of time and a little space
 * 
 * @author Richard Connor
 * 
 */
public class NamedFloatFileReader extends ArrayList<NamedObject<float[]>> {

	private static NamedObject<float[]> getFloatVectorFromLine(String s,
			int dimension) throws Exception {
		Scanner s1 = new Scanner(s);

		String name = s1.next();
		float[] p = new float[dimension];

		try {
			populateValueArray(s1, p, dimension);
		} catch (Throwable t) {
			throw new Exception(
					"get vector: badly formed line in file, maybe too few values");
		}
		if (s1.hasNextDouble()) {
			throw new Exception("too many data values in line");
		}
		return new NamedObject<>(p, name);
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

	public NamedFloatFileReader(String s, int dimensions) throws Exception {

		File nextFile = new File(s);
		/*
		 * tracks the global item id
		 */

		LineNumberReader lr = new LineNumberReader(new FileReader(nextFile));
		/*
		 * and for each line in the file, ie each data point
		 */
		for (String line = lr.readLine(); line != null; line = lr.readLine()) {
			NamedObject<float[]> point = getFloatVectorFromLine(line,
					dimensions);
			this.add(point);
		}
		lr.close();
	}

}
