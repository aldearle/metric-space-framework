package dataSets.fileReaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import coreConcepts.DataSet;
import dataPoints.cartesian.NamedCartesianPoint;

/**
 * Creates a new instance of DataSet of CartesianPoint from the given filename.
 * Can be used as a DataSet<CartesianPoint> or alternatively as an
 * ArrayList<CartesianPoint> depending on context
 * 
 * @author Richard Connor
 * 
 */
@SuppressWarnings("serial")
public class NamedCartesianPointFileReader extends
		ArrayList<NamedCartesianPoint> implements DataSet<NamedCartesianPoint> {

	protected int dimension = -1;
	private String filename;
	private String shortName;
	private Random rand = new Random(0);

	/**
	 * 
	 * 
	 * 
	 * @param fileName
	 *            the name of the file to be read
	 * @param headerLine
	 *            whether a header line is present or not; if it is, this is
	 *            just ignored
	 * 
	 * @throws Exception
	 *             if something goes wrong opening or reading the file
	 */
	public NamedCartesianPointFileReader(String fileName, boolean headerLine)
			throws Exception {
		this.filename = fileName;
		File f = new File(fileName);
		this.shortName = f.getName().split("\\.")[0];
		try {
			LineNumberReader lr = new LineNumberReader(new FileReader(fileName));

			int lineNumber = 1;

			if (headerLine) {
				@SuppressWarnings("unused")
				String chuck = lr.readLine();
				lineNumber++;
			}

			for (String s = lr.readLine(); s != null; s = lr.readLine()) {
				final String[] nameAndLine = nameSplit(s);
				if (this.dimension == -1) {
					this.dimension = getDimension(nameAndLine[1]);
				}
				add(new NamedCartesianPoint(nameAndLine[0],
						getRealVectorFromLine(nameAndLine[1], lineNumber++,
								this.dimension)));

			}

			lr.close();
		} catch (FileNotFoundException e) {
			throw new Exception("can't open file: " + fileName);
		} catch (IOException e) {
			throw new Exception("can't read from file: " + fileName);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new Exception("problem with file " + fileName + " : "
					+ t.getClass());
		}
	}

	private static String[] nameSplit(String s) {
		String[] res = new String[2];
		String name = s.split("\\s")[0];
		res[0] = name;
		res[1] = s.substring(name.length() + 1);
		return res;
	}

	@Override
	public String getDataSetName() {
		String res = "Vector File: " + this.filename;
		return res;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	@Override
	public NamedCartesianPoint randomValue() {
		final int index = this.rand.nextInt(size());
		return get(index);
	}

	private static int getDimension(String s) throws Exception {
		int dim = 0;
		Scanner s1 = new Scanner(s);
		try {
			while (s1.nextDouble() <= Double.MAX_VALUE) {
				dim++;
			}
		} catch (Throwable t) {
			/*
			 * ugly termination when we can no longer read a double... not nice
			 * but pragmatically useful
			 */
		}
		if (dim == 0) {
			throw new Exception(
					"can't read double values from first line of file");
		}
		return dim;
	}

	private static double[] getRealVectorFromLine(String s, int lineNumber,
			int dim) throws Exception {
		Scanner s1 = new Scanner(s);
		double[] p = new double[dim];

		try {
			populateLineArray(s1, p, dim);
		} catch (Throwable t) {
			throw new Exception(
					"get vector: badly formed line in file at line "
							+ lineNumber);
		}
		if (s1.hasNextDouble()) {
			throw new Exception("too many data values in file at line "
					+ lineNumber);
		}
		return p;
	}

	protected static void populateLineArray(Scanner s1, double[] p,
			int dimension) {
		for (int i = 0; i < dimension; i++) {
			p[i] = s1.nextDouble();
		}
	}

	@Override
	public String getDataSetShortName() {
		return this.shortName;
	}

}
