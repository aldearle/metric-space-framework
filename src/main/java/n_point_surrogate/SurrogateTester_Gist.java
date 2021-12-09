package n_point_surrogate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import util.Range;
import dataPoints.cartesian.CartesianPoint;
import dataSets.fileReaders.CartesianPointFileReader;
import dataSets.fileReaders.GistFileReader;

public class SurrogateTester_Gist {

	List<CartesianPoint> refPoints;

	public static String mfFilebase = "/Volumes/Data/mirflickr/";
	public static String mfGistFilebase = mfFilebase + "gist_pivot_dists_jsd/";

	public static void main(String[] a) throws Exception {
		File f = new File(mfFilebase);
		System.out.println(f.exists());
		List<Integer> g = gistPivotIds();
		System.out.println(g.size());

		// doSomethingWithPoints();
	}

	private static void doSomethingWithPoints() throws Exception {
		for (int i : new Range(0, 100)) {
			CartesianPointFileReader c = new CartesianPointFileReader(
					mfFilebase + i + ".dat", false);
			System.out.println("read " + c.size() + " points from file " + i);
			System.out.println("each is " + c.get(0).getPoint().length
					+ " dimensions");
		}
	}

	@SuppressWarnings("boxing")
	public static List<Integer> gistPivotIds() throws IOException {
		FileReader f = new FileReader(mfFilebase + "gist_pivots.txt");
		LineNumberReader flr = new LineNumberReader(f);
		String line = flr.readLine();
		List<Integer> res = new ArrayList<>();
		while (line != null) {
			int next = Integer.parseInt(line);
			res.add(next);

			GistFileReader gr = new GistFileReader("");

			line = flr.readLine();
		}

		f.close();
		return res;
	}
}
