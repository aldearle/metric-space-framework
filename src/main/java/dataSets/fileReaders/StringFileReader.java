package dataSets.fileReaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Random;

import semanticDataTypes.StringShingle;
import semanticDataTypes.StringShingle.ShingleType;
import coreConcepts.DataSet;
import dataPoints.compactEnsemble.EventToIntegerMap;

/**
 * 
 * reads a file of strings value and presents them as a DataSet<StringShingle>
 * 
 * @author Richard Connor
 * 
 */
@SuppressWarnings("serial")
public class StringFileReader extends ArrayList<StringShingle> implements
		DataSet<StringShingle> {

	private String fileName;
	private int longest = 0;
	private int maxLinesToRead;
	private Random rand = new Random(0);
	private ShingleType shingleType;
	private String shortName;

	/**
	 * @param relativeFileName
	 *            the file name
	 * @param type
	 *            the type of StringShingle to be used
	 * @param eToi
	 *            the event to integer map
	 * @throws Exception
	 *             if something goes wrong reading the file
	 */
	public StringFileReader(String relativeFileName, ShingleType type,
			EventToIntegerMap<String> eToi) throws Exception {

		readFileAndInitialise(relativeFileName, type, eToi, Integer.MAX_VALUE);
	}

	/**
	 * reads only up to the specified number of lines
	 * 
	 * @param relativeFileName
	 *            the file name
	 * @param type
	 *            the type of StringShingle to be used
	 * @param noOfLines
	 *            the number of lines to read
	 * @throws Exception
	 *             if something goes wrong reading the file
	 */
	public StringFileReader(String relativeFileName, ShingleType type,
			EventToIntegerMap<String> eToi, int noOfLines) throws Exception {
		readFileAndInitialise(relativeFileName, type, eToi, noOfLines);
	}

	@Override
	public String getDataSetName() {
		return "String shingle file: " + this.fileName + " (shingle type "
				+ this.shingleType + ")";
	}

	@Override
	public String getDataSetShortName() {
		// TODO Auto-generated method stub
		return shortName;
	}

	@Override
	public boolean isFinite() {
		return true;
	}

	/**
	 * @return the length of the longest string in the file
	 */
	public int maxLength() {
		return this.longest;
	}

	@Override
	public StringShingle randomValue() {
		// could have used nextInt!!
		int index = (int) Math.floor(this.rand.nextDouble() * (double) size());
		StringShingle ret = get(index);
		this.remove(index);
		return ret;
	}

	private void readFileAndInitialise(String relativeFileName,
			ShingleType type, EventToIntegerMap<String> eToi, int noOfLines)
			throws Exception {
		this.maxLinesToRead = noOfLines;
		this.fileName = relativeFileName;
		this.shingleType = type;
		File f = new File(relativeFileName);
		this.shortName = f.getName();
		try {
			FileReader csv = new FileReader(f);
			LineNumberReader lr = new LineNumberReader(csv);

			int linesRead = 0;
			for (String s = lr.readLine(); s != null; s = lr.readLine()) {
				if (linesRead++ < noOfLines) {
					this.longest = Math.max(this.longest, s.length());
					add(new StringShingle(s, type, eToi));
				}
			}
		} catch (FileNotFoundException e) {
			throw new Exception("can't open file: " + relativeFileName);

		} catch (IOException e) {
			throw new Exception("can't read from file: " + relativeFileName);
		}
	}

}
