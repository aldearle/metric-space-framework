package supermetrics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import n_point_surrogate.SimplexND;
import searchStructures.GHTree;
import searchStructures.SearchIndex;
import coreConcepts.CountedMetric;
import coreConcepts.Metric;
import coreConcepts.NamedObject;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannonViaSed;
import dataPoints.cartesian.NamedCartesianPoint;
//import dataPoints.floatArray.Euclidean;
import dataSets.fileReaders.GistFileReader;
import dataSets.fileReaders.NamedFloatFileReader;

public class MirFlickrGistExperiment {

	private static String filebase = "/Volumes/Data/mirflickr/";

	public static void main(String[] a) throws Exception {
		// MirFlickrGistExperiment e = new MirFlickrGistExperiment();
		// float[] image0 = getImage(0);
		// float[] image1 = getImage(585585);
		// System.out.println((new Euclidean()).distance(image0, image1));

		// writePivotDistancesCartesian(new JensenShannonViaSed());
		// writePivotDistancesCartesian(new CosineNormalised<>());
		// writeGistApexes(20);

		try {
			assert false : "assertions on";
			System.out.println("assertions off");
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}

		// writeDimFiles();

		// long t0 = System.currentTimeMillis();
		// timeGistDistance(100 * 1000);
		// System.out.println("that took " + (System.currentTimeMillis() - t0));

		// printNDDists();
		queryNDClusters(20, 0.03);
	}

	private static void writeDimFiles() throws Exception {

		List<CartesianPoint> data = new ArrayList<>();
		Random r = new Random();
		for (int i = 0; i < 10000; i++) {
			CartesianPoint im1 = CartesianPoint.toCartesianPoint(getImage(r
					.nextInt(1000 * 1000)));
			data.add(im1);
		}
		Metric<CartesianPoint> jsd = new JensenShannonViaSed<>();
		SimplexDistanceFileOutput<CartesianPoint> so = new SimplexDistanceFileOutput<>(
				data, jsd, "gist_jsd");
		int[] dims = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20 };
		so.writeDists("/Volumes/Data/simplexes/output_temp/", dims);
	}

	/**
	 * create a new set of files with distances from each point to each pivot
	 * point
	 * 
	 * @throws IOException
	 */
	private static void writePivotDistancesEuclidean() throws IOException {
		List<float[]> pivots = getGistPivots();
		dataPoints.floatArray.Euclidean euc = new dataPoints.floatArray.Euclidean();

		for (int i = 0; i < 100; i++) {
			PrintWriter f = new PrintWriter(filebase + "gist_pivot_dists/" + i
					+ ".dat");

			for (int j = 0; j < 10000; j++) {
				final int imageId = i * 10000 + j;
				f.print(imageId + " ");
				float[] gist = getImage(imageId);
				for (float[] piv : pivots) {
					f.print(euc.distance(gist, piv));
					f.print(" ");
				}
				f.println();
			}
			f.close();

		}
	}

	/**
	 * create a new set of files with distances from each point to each pivot
	 * point
	 * 
	 * @throws IOException
	 */
	private static void writePivotDistancesCartesian(Metric<CartesianPoint> m)
			throws IOException {
		List<CartesianPoint> pivots = getGistPivotsCartesian();

		for (int i = 0; i < 100; i++) {
			PrintWriter f = new PrintWriter(filebase + "gist_pivot_dists_"
					+ m.getMetricName() + "/" + i + ".dat");

			for (int j = 0; j < 10000; j++) {
				final int imageId = i * 10000 + j;
				f.print(imageId + " ");
				CartesianPoint gist = CartesianPoint
						.toCartesianPoint(getImage(imageId));
				for (CartesianPoint piv : pivots) {
					f.print(m.distance(gist, piv));
					f.print(" ");
				}
				f.println();
			}
			f.close();

		}
	}

	private static void timeGistDistance(int number) throws IOException {
		Metric<CartesianPoint> jsd = new JensenShannonViaSed<>();
		Random r = new Random();
		for (int i = 0; i < number; i++) {
			CartesianPoint im1 = CartesianPoint.toCartesianPoint(getImage(r
					.nextInt(1000 * 1000)));
			CartesianPoint im2 = CartesianPoint.toCartesianPoint(getImage(r
					.nextInt(1000 * 1000)));
			double d = jsd.distance(im1, im2);
		}
	}

	private static float[] getImage(int i) throws IOException {
		GistFileReader g = new GistFileReader(idToFilename(i));
		return g.getGistValues();
	}

	private static String idToFilename(int id) {
		return filebase + "features_gist/" + (id / 10000) + "/" + id + ".dat";
	}

	@SuppressWarnings("boxing")
	private static List<float[]> getGistPivots() throws IOException {
		List<float[]> res = new ArrayList<>();

		List<Integer> pivs = getPivotIds(1000);

		for (int pivotId : pivs) {
			res.add(getImage(pivotId));
		}
		return res;
	}

	private static List<Integer> getPivotIds(int numberRequired)
			throws FileNotFoundException, IOException {
		List<Integer> pivs = new ArrayList<>();
		FileReader f = new FileReader(filebase + "gist_pivots.txt");
		LineNumberReader flr = new LineNumberReader(f);
		String next = flr.readLine();

		while (next != null && pivs.size() < numberRequired) {
			final int pivotId = Integer.parseInt(next);
			pivs.add(pivotId);
			next = flr.readLine();
		}
		flr.close();
		assert pivs.size() == numberRequired : "wrong number of gist pivots read ("
				+ pivs.size() + ")";
		return pivs;
	}

	@SuppressWarnings("boxing")
	private static List<CartesianPoint> getGistPivotsCartesian()
			throws IOException {
		List<CartesianPoint> res = new ArrayList<>();
		FileReader f = new FileReader(filebase + "gist_pivots.txt");
		LineNumberReader flr = new LineNumberReader(f);
		String next = flr.readLine();
		while (next != null) {
			final float[] nextFloatArray = getImage(Integer.parseInt(next));
			res.add(CartesianPoint.toCartesianPoint(nextFloatArray));
			next = flr.readLine();
		}
		flr.close();
		assert res.size() == 1000 : "wrong number of gist pivots read ("
				+ res.size() + ")";
		return res;
	}

	/**
	 * @param metricType
	 *            always uses Jensen-Shannon distance despite this parameter
	 * @param simplexDimension
	 *            the dimension of the simplex to generate
	 * @throws Exception
	 */
	@SuppressWarnings("boxing")
	private static void writeGistApexes(int simplexDimension) throws Exception {

		List<CartesianPoint> pivs = getGistPivotsCartesian();
		List<CartesianPoint> refs = new ArrayList<>();
		for (int x = 0; x < simplexDimension; x++) {
			refs.add(pivs.get(x));
		}

		List<Integer> firstPivs = getPivotIds(simplexDimension);

		SimplexND<CartesianPoint> s = new SimplexND<>(simplexDimension,
				new JensenShannonViaSed<>(), refs);

		for (int i = 0; i < 100; i++) {
			PrintWriter pw = new PrintWriter(filebase + "gist_apexes_jsd_"
					+ simplexDimension + "df/" + i + ".dat");
			for (int j = 0; j < 10 * 1000; j++) {
				int imageId = i * 10 * 1000 + j;

				if (!firstPivs.contains(imageId)) {
					float[] g = getImage(imageId);
					CartesianPoint p = CartesianPoint.toCartesianPoint(g);

					double[] ap = s.formSimplex(p);

					pw.write(imageId + " ");
					for (double d : ap) {
						pw.write((float) d + " ");
					}
					pw.println();
				}
			}

			pw.close();
		}
	}

	private static List<List<Integer>> getNDClusters() throws IOException {
		List<List<Integer>> res = new ArrayList<>();
		FileReader fr = new FileReader(filebase + "IND_clusters.txt");
		LineNumberReader lr = new LineNumberReader(fr);
		String next = lr.readLine();
		while (next != null) {
			List<Integer> sublist = new ArrayList<>();
			String[] ints = next.split("\\s");
			for (String s : ints) {
				sublist.add(Integer.parseInt(s));
			}
			res.add(sublist);
			next = lr.readLine();
		}

		return res;
	}

	private static void queryNDClusters(int dimension, double threshold)
			throws Exception {
		List<NamedObject<float[]>> allGistSurs = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			NamedFloatFileReader fr = new NamedFloatFileReader(filebase
					+ "gist_apexes_" + "jsd" + "_" + dimension + "df/" + i
					+ ".dat", dimension);
			allGistSurs.addAll(fr);
		}

		Metric<NamedObject<float[]>> m = new Metric<NamedObject<float[]>>() {

			final Metric<float[]> m = new dataPoints.floatArray.Euclidean();

			@Override
			public double distance(NamedObject<float[]> x,
					NamedObject<float[]> y) {
				return m.distance(x.object, y.object);
			}

			@Override
			public String getMetricName() {
				return m.getMetricName();
			}
		};

		CountedMetric<NamedObject<float[]>> cm = new CountedMetric<NamedObject<float[]>>(
				m) {

		};

		GHTree<NamedObject<float[]>> ght = new GHTree<>(allGistSurs, cm, true);
		cm.reset();

		ght.setCosExclusionEnabled(true);
		ght.setCrExclusionEnabled(true);
		ght.setVorExclusionEnabled(true);

		long t0 = System.currentTimeMillis();
		queryNDclusters(allGistSurs, cm, ght, threshold);
		System.out.println("that took " + (System.currentTimeMillis() - t0));
	}

	private static void querySmokingGirl(List<NamedCartesianPoint> allGistSurs,
			CountedMetric<NamedCartesianPoint> cm,
			GHTree<NamedCartesianPoint> ght) throws IOException {
		List<NamedCartesianPoint> res = ght.thresholdSearch(allGistSurs.get(0),
				0.002);

		CartesianPoint smGirlGist = CartesianPoint
				.toCartesianPoint(getImage(0));
		CartesianPoint smGirlSur = allGistSurs.get(0);
		Metric<CartesianPoint> jsd = new JensenShannonViaSed<>();
		Metric<CartesianPoint> euc = new Euclidean<>();
		for (NamedCartesianPoint p : res) {
			System.out.print("id: " + p.getName() + "; ");
			final int imageId = Integer.parseInt(p.getName());
			CartesianPoint gist = CartesianPoint
					.toCartesianPoint(getImage(imageId));
			System.out.print("orig dist " + jsd.distance(smGirlGist, gist)
					+ ";");
			System.out.println("surr dist " + euc.distance(smGirlSur, p));
		}
		System.out.println("results returned: " + res.size());
		System.out.println("distances calculated: " + cm.reset());
	}

	@SuppressWarnings("boxing")
	private static <T> void queryNDclusters(List<NamedObject<T>> allGistSurs,
			CountedMetric<NamedObject<T>> cm, SearchIndex<NamedObject<T>> ght,
			double threshold) throws IOException {

		List<List<Integer>> ndClusts = getNDClusters();
		for (List<Integer> ndId : ndClusts) {
			int thisId = ndId.get(0);

			NamedObject<T> p = getSurrogateById(allGistSurs, thisId);
			List<NamedObject<T>> res = ght.thresholdSearch(p, threshold);

			System.out.print(thisId);
			System.out.print("\t" + res.size());
			System.out.println("\t" + cm.reset());
		}
	}

	private static <T> NamedObject<T> getSurrogateById(
			List<NamedObject<T>> surs, int id) {
		String idName = Integer.toString(id);
		int currentPos = Math.max(id, surs.size() - 1);
		NamedObject<T> res = surs.get(currentPos--);
		while (!res.getName().equals(idName)) {
			res = surs.get(currentPos--);
		}
		return res;
	}

	private static void printNDDists() throws IOException {
		List<List<Integer>> ndcs = getNDClusters();
		Metric<CartesianPoint> jsd = new JensenShannonViaSed<>();
		for (List<Integer> l : ndcs) {
			CartesianPoint im1 = CartesianPoint.toCartesianPoint(getImage(l
					.get(0)));
			for (int i : l) {
				CartesianPoint im2 = CartesianPoint
						.toCartesianPoint(getImage(i));
				System.out.print(jsd.distance(im1, im2) + "\t");
			}
			System.out.println();
		}
	}
}
