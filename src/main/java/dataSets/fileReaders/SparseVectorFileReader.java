package dataSets.fileReaders;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import coreConcepts.DataSet;
import dataPoints.sparseCartesian.SparseCartesianPoint;

/**
 * reads a list of SparseCartesianPoints from the file in the verbose format
 * 
 * @author Richard Connor
 * 
 */
public class SparseVectorFileReader extends ArrayList<SparseCartesianPoint>
		implements DataSet<SparseCartesianPoint> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2454734236078113192L;
	Random rand = new Random(0);

	@SuppressWarnings({ "boxing" })
	public SparseVectorFileReader(File f) throws IOException {

		LineNumberReader lr = new LineNumberReader(new FileReader(f));

		for (String line = lr.readLine(); line != null; line = lr.readLine()) {

			final Scanner dimInfoLine = new Scanner(line).useDelimiter(",\\s");
			if (dimInfoLine.hasNextInt()) {

				int objectId = dimInfoLine.nextInt();
				int noOfDimensions = dimInfoLine.nextInt();

				String idIndexLine = lr.readLine();
				Scanner idLine = (new Scanner(idIndexLine))
						.useDelimiter(",\\s");
				String valsIndexLine = lr.readLine();
				Scanner valsLine = (new Scanner(valsIndexLine))
						.useDelimiter(",\\s");

				int[] dimNumbers = new int[noOfDimensions];
				double[] vals = new double[noOfDimensions];

				for (int i = 0; i < noOfDimensions; i++) {
					final int nextInt = idLine.nextInt();
					dimNumbers[i] = nextInt;
				}
				assert !idLine.hasNextInt() : "id line not fully read";

				for (int i = 0; i < noOfDimensions; i++) {
					final double nextDouble = valsLine.nextDouble();
					vals[i] = nextDouble;
				}
				assert !idLine.hasNextDouble() : "id line not fully read";

				this.add(new SparseCartesianPoint(dimNumbers, vals));

			}
		}
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	@Override
	public SparseCartesianPoint randomValue() {
		// TODO Auto-generated method stub
		return this.get(this.rand.nextInt(this.size()));
	}

	@Override
	public String getDataSetName() {
		// TODO Auto-generated method stub
		return "sparse vector";
	}

	@Override
	public String getDataSetShortName() {
		// TODO Auto-generated method stub
		return "sparse";
	}

}
