package sisap2013paper;

import histogram.MetricHistogram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import util.Timer;
import util.Timer.Command;
import coreConcepts.DataSet;
import coreConcepts.Metric;
import coreConcepts.MetricSpace;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.JensenShannonViaSed;
import dataPoints.sparseCartesian.InvertedIndex;
import dataPoints.sparseCartesian.InvertedIndexDef2;
import dataPoints.sparseCartesian.InvertedIndexDef3Correct;
import dataPoints.sparseCartesian.JensenShannonDef1;
import dataPoints.sparseCartesian.JensenShannonDef2;
import dataPoints.sparseCartesian.JensenShannonDef3;
import dataPoints.sparseCartesian.Manhattan;
import dataPoints.sparseCartesian.SparseCartesianPoint;
import dataSets.fileReaders.SparseVectorFileReader;
import dataSets.generators.CartesianPointGenerator;
import dataSets.generators.SparseCartesianPointGenerator;

/**
 * 
 * this class contains all the code needed to produce the results documented in
 * the SISAP 2013 paper called
 * "Evaluation of Jensen-Shannon Distance over Sparse Data"
 * 
 * @author Richard Connor
 * 
 */
public class TestRunsForPaper {

	public static double t5 = 0.229;
	public static double t6 = 0.213;
	/**
	 * the dimensions chosen for generated examples in the paper
	 */
	public static int[] testedPopulatedDimensions = { 50, 125, 250, 500, 1000,
			2000 };
	public static String[] realDataSets = { "colors" };

	/**
	 * Creates data files to run against the inverted index files generated for
	 * the same data set
	 * 
	 * each datapoint in the file is three lines long: line 1: object id, no of
	 * non-zero dimensions line 2: dimension numbers of non-zero dimensions line
	 * 3: (normalised) values of non-zero dimensions
	 * 
	 * @param noOfPoints
	 *            the number of data objects to put in each file
	 * @param fileBase
	 *            the path to where the files are stored
	 * @throws FileNotFoundException
	 *             if a file can't be opened
	 */
	public static void createGeneratedDataFiles(int noOfPoints, String fileBase)
			throws FileNotFoundException {

		Map<Integer, List<SparseCartesianPoint>> dataSets = getSparseDataSets(noOfPoints);

		for (int i : dataSets.keySet()) {
			final List<SparseCartesianPoint> l = dataSets.get(i);

			final String fileName = fileBase + "dataFiles/sparse" + i
					+ "_50Data.csv";

			writeSparseDataFile(l, fileName);
		}

	}

	/**
	 * Writes a list of SparseCartesianPoints into the verbose data file format
	 * at the file path given
	 * 
	 * @param data
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public static void writeSparseDataFile(
			final List<SparseCartesianPoint> data, final String fileName)
			throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(fileName);

		int pntr = 0;
		for (SparseCartesianPoint p : data) {
			final int[] dims = p.getDims();
			final double[] vals = p.getValues();
			pw.println(pntr + ", " + dims.length);

			for (int j = 0; j < dims.length; j++) {
				pw.print(dims[j]);
				if (j < dims.length - 1) {
					pw.print(", ");
				}
			}
			pw.println();

			for (int j = 0; j < vals.length; j++) {
				pw.print(vals[j]);
				if (j < vals.length - 1) {
					pw.print(", ");
				}
			}
			pw.println();

			pntr++;
		}
		pw.close();
	}

	/**
	 * Create inverted index files for the sparse dimensions of the SISAP paper
	 * 
	 * @param noOfPoints
	 * @param fileBase
	 * @throws FileNotFoundException
	 */
	public static void createGeneratedInvertedIndexFiles(int noOfPoints,
			String fileBase) throws FileNotFoundException {

		Map<Integer, List<SparseCartesianPoint>> dataSets = getSparseDataSets(noOfPoints);

		for (int i : dataSets.keySet()) {
			final List<SparseCartesianPoint> l = dataSets.get(i);

			InvertedIndexDef2<SparseCartesianPoint> ii = new InvertedIndexDef2<SparseCartesianPoint>(
					l);
			ii.writeVerboseIndexFile(fileBase + "iiFiles/sparse" + i
					+ "_50.csv");
		}

	}

	/**
	 * generates a histogram along with associated data values for
	 * 50-dimensional Cartesian space under Jensen-Shannon
	 * 
	 * @param fileBase
	 *            folder where the file should be generated
	 * 
	 * @throws FileNotFoundException
	 *             if the output file can't be created and written to
	 */
	public static void get50dimThresholdValues(String fileBase)
			throws FileNotFoundException {

		DataSet<CartesianPoint> gen = new CartesianPointGenerator(50, false);
		MetricSpace<CartesianPoint> ms = new MetricSpace<CartesianPoint>(gen,
				new JensenShannonViaSed());
		MetricHistogram<CartesianPoint> mhist = new MetricHistogram<CartesianPoint>(
				ms, 10000, 1000, true, true, 1.0);
		PrintStream ps = new PrintStream(fileBase + "50DimDenseHist.csv");
		MetricHistogram.printHeaderRow(ps);
		mhist.printToStream(ps);
		ps.close();
	}

	/**
	 * run performance tests documented in SISAP 2013 submission
	 * 
	 * @param noOfPoints
	 * @param accurate
	 * @param pw
	 * @throws Exception
	 */
	public static void runFileTestsForSisapPaper(String pathRoot,
			boolean accurate, PrintStream pw) throws Exception {

		Map<Integer, List<SparseCartesianPoint>> dataSets = getSparseDataSetsFromFiles(pathRoot);

		runDataSetTests(accurate, pw, dataSets);

	}

	/**
	 * run all tests over data and ii files given by the root and fileName
	 * 
	 * @param pathRoot
	 * @param fileName
	 * @param t1
	 * @param t2
	 * 
	 * @param accurate
	 * @param pw
	 * @param reorder
	 * 
	 * @throws Exception
	 */
	public static void runFileSeriesTests(String pathRoot, String fileName,
			double t1, double t2, boolean accurate, PrintStream pw,
			final boolean reorder) throws Exception {
		File dataFile = new File(pathRoot + "/dataFiles/" + fileName
				+ "Data.csv");
		File iiFile = new File(pathRoot + "/iiFiles/" + fileName + ".csv");
		if (!dataFile.exists()) {
			throw new Exception("couldn't find file for " + fileName);
		}

		List<Metric<SparseCartesianPoint>> ms = getJSMetrics(t1, t2);
		final List<SparseCartesianPoint> data = new SparseVectorFileReader(
				dataFile);

		for (final Metric<SparseCartesianPoint> m : ms) {
			pw.print(m.getMetricName());

			Command c = new Command() {

				@Override
				public void execute() {
					for (SparseCartesianPoint p1 : data) {
						for (SparseCartesianPoint p2 : data) {
							double d = m.distance(p1, p2);
						}
					}

				}
			};
			double time = Timer.time(c, accurate);
			double microSecsPerM = 1000 * (time / (data.size() * data.size()));
			double printable = Math.round(microSecsPerM * 1000) / (double) 1000;
			pw.print("\t" + printable);

			pw.println();
		}

		for (int fileVersion = 1; fileVersion <= 3; fileVersion++) {
			double threshold = t1;
			final InvertedIndex<SparseCartesianPoint> invInd = getIIVersion(
					fileVersion, iiFile);
			if (fileVersion == 3) {
				threshold = t2;
			}
			final double thresh = threshold;

			pw.print("Inv Ind Def " + fileVersion + " (" + threshold + ")");

			Command c = new Command() {

				@Override
				public void execute() {
					for (SparseCartesianPoint p : data) {
						if (reorder) {
							p.reorder();
						}
						invInd.thresholdQuery(p, thresh);
					}

				}
			};
			double time = Timer.time(c, accurate);
			double microSecsPerM = 1000 * (time / (data.size() * data.size()));
			double printable = Math.round(microSecsPerM * 1000) / (double) 1000;
			pw.print("\t" + printable);

			pw.println();
		}

	}

	/**
	 * run performance tests documented in SISAP 2013 submission
	 * @param pathRoot 
	 * @param fileName 
	 * @param accurate 
	 * @param pw 
	
	 */
	public static void runGeneratedTestsForSisapPaper(String pathRoot,
			String fileName, boolean accurate, PrintStream pw) {

	}

	private static void runDataSetTests(boolean accurate, PrintStream pw,
			Map<Integer, List<SparseCartesianPoint>> dataSets) {
		double t1 = t5;
		double t2 = t6;

		List<Metric<SparseCartesianPoint>> ms = getJSMetrics(t1, t2);

		for (final Metric<SparseCartesianPoint> m : ms) {
			pw.print(m.getMetricName() + "\t&");
			for (int i : dataSets.keySet()) {
				final List<SparseCartesianPoint> l = dataSets.get(i);

				Command c = new Command() {

					@Override
					public void execute() {
						for (SparseCartesianPoint p : l) {
							for (SparseCartesianPoint q : l) {
								double d = m.distance(p, q);
							}
						}
					}
				};

				double t = Timer.time(c, accurate);
				double microSecsPerM = 1000 * (t / (l.size() * l.size()));
				double printable = Math.round(microSecsPerM * 1000)
						/ (double) 1000;
				pw.print(printable + "\t");
				if (i != 2000) {
					pw.print("&");
				}
			}
			pw.println("\\\\");
		}
	}

	private static List<Metric<SparseCartesianPoint>> getJSMetrics(double t1,
			double t2) {
		Metric<SparseCartesianPoint> m1 = new JensenShannonDef1<SparseCartesianPoint>();
		Metric<SparseCartesianPoint> m2 = new JensenShannonDef2<SparseCartesianPoint>();
		Metric<SparseCartesianPoint> m3 = new JensenShannonDef3<SparseCartesianPoint>(
				t1);
		Metric<SparseCartesianPoint> m4 = new JensenShannonDef3<SparseCartesianPoint>(
				t2);
		Metric<SparseCartesianPoint> man = new Manhattan();

		List<Metric<SparseCartesianPoint>> ms = new ArrayList<Metric<SparseCartesianPoint>>();
		ms.add(m1);
		ms.add(m2);
		ms.add(m3);
		ms.add(m4);
		ms.add(man);
		return ms;
	}

	public static void runInvertedFileTests(String pathRoot, int fileVersion,
			boolean accurate, PrintStream pw) throws Exception {

		pw.print("Inverted Index " + fileVersion + "\t&");
		for (int i : testedPopulatedDimensions) {

			File dataFile = new File(pathRoot + "/dataFiles/sparse" + i
					+ "_50Data.csv");
			File iiFile = new File(pathRoot + "/iiFiles/sparse" + i + "_50.csv");
			if (!dataFile.exists() || !iiFile.exists()) {
				throw new Exception("couldn't find file for " + i
						+ " populated dimensions");
			}

			List<SparseCartesianPoint> data = new SparseVectorFileReader(
					dataFile);

			double threshold = t5;
			InvertedIndex<SparseCartesianPoint> invInd = getIIVersion(
					fileVersion, iiFile);
			if (fileVersion == 3) {
				threshold = t6;
			}

			printInvIndTest(accurate, pw, data, invInd, threshold);

		}
		pw.println();

	}

	private static InvertedIndex<SparseCartesianPoint> getIIVersion(
			int fileVersion, File iiFile) throws IOException {
		InvertedIndex<SparseCartesianPoint> invInd;
		if (fileVersion == 1) {
			invInd = new InvertedIndexDef2<SparseCartesianPoint>(iiFile);
		} else {
			invInd = new InvertedIndexDef3Correct<SparseCartesianPoint>(iiFile);
		}
		return invInd;
	}

	public static void runInvertedIndexTestsForSisap(int noOfPoints,
			boolean accurate, PrintStream pw) {

		Map<Integer, List<SparseCartesianPoint>> dataSets = getSparseDataSets(noOfPoints);

		runInvertedIndexTest1ForSisap(dataSets, accurate, pw);
		runInvertedIndexTest2ForSisap(dataSets, accurate, pw);
		runInvertedIndexTest3ForSisap(dataSets, accurate, pw);
	}

	@SuppressWarnings("boxing")
	private static Map<Integer, List<SparseCartesianPoint>> getSparseDataSets(
			int noOfPoints) {
		Map<Integer, List<SparseCartesianPoint>> dataSets = new TreeMap<Integer, List<SparseCartesianPoint>>();

		for (int totDims : testedPopulatedDimensions) {
			DataSet<CartesianPoint> gen = new SparseCartesianPointGenerator(
					totDims, 50, false);
			List<SparseCartesianPoint> data = new ArrayList<SparseCartesianPoint>();
			for (int i = 0; i < noOfPoints; i++) {
				data.add(new SparseCartesianPoint(gen.randomValue()));
			}

			dataSets.put(totDims, data);
		}
		return dataSets;
	}

	@SuppressWarnings("boxing")
	private static Map<Integer, List<SparseCartesianPoint>> getSparseDataSetsFromFiles(
			String pathRoot) throws Exception {
		Map<Integer, List<SparseCartesianPoint>> dataSets = new TreeMap<Integer, List<SparseCartesianPoint>>();

		for (int i : testedPopulatedDimensions) {

			File dataFile = new File(pathRoot + "/dataFiles/sparse" + i
					+ "_50Data.csv");
			if (!dataFile.exists()) {
				throw new Exception("couldn't find file for " + i
						+ " populated dimensions");
			}

			List<SparseCartesianPoint> data = new SparseVectorFileReader(
					dataFile);
			dataSets.put(i, data);

		}

		return dataSets;
	}

	public static void getAllStats(String pathRoot) throws Exception {
		String[] testFiles = { "sparse50_50", "sparse125_50", "sparse250_50",
				"sparse500_50", "sparse1000_50", "sparse2000_50", "colors1K",
				"EnglishDic", "occupations", "MirFlickrEH", "MirFlickrHT" };

		for (String fileName : testFiles) {
			File iiFile = new File(pathRoot + "/iiFiles/" + fileName + ".csv");

			if (!iiFile.exists()) {
				throw new Exception("couldn't index find file for " + fileName);
			}

			InvertedIndex<SparseCartesianPoint> invInd = new InvertedIndexDef2<SparseCartesianPoint>(
					iiFile);

			System.out.println(fileName + " stats:");
			System.out.println(invInd.getStats());
		}

	}

	public static void doAllSisapPaperTests(String filePath,
			final boolean accurate, final PrintStream output)
			throws IOException, Exception {
		String[] testFiles = { "sparse50_50", "sparse125_50", "sparse250_50",
				"sparse500_50", "sparse1000_50", "sparse2000_50", "colors1K",
				"EnglishDic", "occupations", "MirFlickrEH", "MirFlickrHT" };

		double t5 = 0;
		double t6 = 0;

		for (String fileName : testFiles) {
			output.println("____________");
			output.println("analysing " + fileName);
			output.println("____________");
			MetricHistogram<?>.HistogramInfo h = TestRunsForPaper
					.getJShistogramInfo(filePath, fileName, System.out);
			if (t5 == 0 || !fileName.contains("sparse")) {
				t5 = h.t5;
				t6 = h.t6;
			}
			output.println("median\t" + h.median + "\t IDIM \t" + h.idim
					+ "\t t5 \t" + t5 + "\t t6 \t" + t6);

			boolean reorder = true;
			if (fileName.equals("occupations") || fileName.equals("EnglishDic")
					|| fileName.equals("MirFlickrHT")) {
				reorder = false;
			}

			TestRunsForPaper.runFileSeriesTests(filePath, fileName, t5, t6,
					accurate, System.out, reorder);
		}
	}

	private static void runInvertedIndexTest1ForSisap(
			Map<Integer, List<SparseCartesianPoint>> dataSets,
			boolean accurate, PrintStream pw) {

		for (int i : dataSets.keySet()) {
			final List<SparseCartesianPoint> l = dataSets.get(i);

			final InvertedIndexDef2<SparseCartesianPoint> ii = new InvertedIndexDef2<SparseCartesianPoint>(
					l);

			printInvIndTest(accurate, pw, l, ii, 0.5);

		}
		pw.println("\\\\");
	}

	private static void runInvertedIndexTest2ForSisap(
			Map<Integer, List<SparseCartesianPoint>> dataSets,
			boolean accurate, PrintStream pw) {

		for (int i : dataSets.keySet()) {
			final List<SparseCartesianPoint> l = dataSets.get(i);

			final InvertedIndex<SparseCartesianPoint> ii = new InvertedIndexDef3Correct<SparseCartesianPoint>(
					l);

			printInvIndTest(accurate, pw, l, ii, t5);

		}
		pw.println("\\\\");
	}

	private static void runInvertedIndexTest3ForSisap(
			Map<Integer, List<SparseCartesianPoint>> dataSets,
			boolean accurate, PrintStream pw) {

		for (int dataSetDimension : dataSets.keySet()) {
			@SuppressWarnings("boxing")
			final List<SparseCartesianPoint> testSet = dataSets
					.get(dataSetDimension);

			final InvertedIndex<SparseCartesianPoint> invertedIndex = new InvertedIndexDef3Correct<SparseCartesianPoint>(
					testSet);

			printInvIndTest(accurate, pw, testSet, invertedIndex, t6);

		}
		pw.println("\\\\");
	}

	private static void printInvIndTest(boolean accurate, PrintStream pw,
			final List<SparseCartesianPoint> testSet,
			final InvertedIndex<SparseCartesianPoint> invertedIndex,
			final double threshold) {

		Command c = new Command() {

			@Override
			public void execute() {
				for (SparseCartesianPoint p : testSet) {
					invertedIndex.thresholdQuery(p, threshold);
				}
			}
		};

		double t = Timer.time(c, accurate);
		double microSecsPerM = 1000 * (t / (testSet.size() * invertedIndex
				.size()));
		double printable = Math.round(microSecsPerM * 1000) / (double) 1000;
		pw.print(printable + "\t");
		pw.print("&");
	}

	public static MetricHistogram.HistogramInfo getJShistogramInfo(
			String filepath, String fileName, PrintStream out)
			throws IOException {

		File dataFile = new File(filepath + "/dataFiles/" + fileName
				+ "Data.csv");
		Metric<SparseCartesianPoint> js = new JensenShannonDef1();
		DataSet<SparseCartesianPoint> data = new SparseVectorFileReader(
				dataFile);
		MetricSpace<SparseCartesianPoint> ms = new MetricSpace<SparseCartesianPoint>(
				data, js);
		MetricHistogram<SparseCartesianPoint> mh = new MetricHistogram<SparseCartesianPoint>(
				ms, 1000, 1000, true, false, 1.0);

		return mh.getHistogramInfo();

	}
}
