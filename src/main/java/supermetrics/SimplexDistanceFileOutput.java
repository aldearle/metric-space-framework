package supermetrics;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import n_point_surrogate.SimplexExclusion;
import n_point_surrogate.SimplexND;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import coreConcepts.DataSet;
import coreConcepts.DataSetImpl;
import coreConcepts.Metric;
import dataPoints.cartesian.CartesianPoint;
import dataPoints.cartesian.CosineNormalised;
import dataPoints.cartesian.Euclidean;
import dataPoints.cartesian.JensenShannonViaSed;
import dataPoints.cartesian.TriDiscrim;

/**
 * @author Richard
 * 
 *         for a given data set and metric, a file is output with true distance
 *         and simplex apex distances at different dimensions for randomly
 *         selected pairs of points
 *
 */
public class SimplexDistanceFileOutput<T> {

	private List<T> data;
	private Metric<T> metric;
	private String filename;

	/**
	 * @param data
	 *            should be 10k data items for present output format
	 * @param metric
	 *            the metric...
	 * @param filename
	 *            the local filename where the output will be stored
	 */
	SimplexDistanceFileOutput(List<T> data, Metric<T> metric, String filename) {
		this.data = data;
		this.metric = metric;
		this.filename = filename;
	}

	public void writeDists(String outputDir, int[] dims) throws Exception {
		final String outputfile = outputDir + this.filename + ".csv";
		PrintWriter pw = new PrintWriter(outputfile);
		System.out.println("created " + outputfile);
		Random r = new Random(0);

		pw.print("actual");
		for (int i : dims) {
			pw.print("," + i);
		}
		pw.println();

		final int testRunSize = 5000;

		double[][] results = new double[testRunSize][dims.length + 1];
		List<T> dataPointsA = new ArrayList<>();
		List<T> dataPointsB = new ArrayList<>();
		for (int xx = 0; xx < testRunSize; xx++) {
			final T p1 = this.data.get(r.nextInt(this.data.size()));
			final T p2 = this.data.get(r.nextInt(this.data.size()));
			dataPointsA.add(p1);
			dataPointsB.add(p2);
			results[xx][0] = this.metric.distance(p1, p2);
		}

		int dimPtr = 1;

		for (int dimension : dims) {
			assert dims[dimPtr - 1] == dimension;

			Object[] refs = new Object[dimension];

			for (int jj = 0; jj < dimension; jj++) {
				T rand = this.data.remove(r.nextInt(this.data.size()));
				refs[jj] = rand;
			}

			T[] d = (T[]) refs;

			SimplexND<T> sim = new SimplexND<>(dimension, this.metric, d);

			for (int xx = 0; xx < testRunSize; xx++) {
				double[] a1 = sim.formSimplex(dataPointsA.get(xx));
				double[] a2 = sim.formSimplex(dataPointsB.get(xx));

				final double l2 = SimplexExclusion.l2(a1, a2);
				try {
					assert l2 != Double.NaN;
					assert !Double.isNaN(l2) : "nan found in SimplexDistanceFileOutput:writeDists";
				} catch (Throwable e) {
					SimplexGeneral.display("a1", a1);
					SimplexGeneral.display("a2", a2);
					assert false;
				}
				results[xx][dimPtr] = l2;
			}
			dimPtr++;
		}

		for (double[] row : results) {
			boolean first = true;
			for (double d : row) {
				if (first) {
					first = false;
				} else {
					pw.print(",");
				}
				assert !Double.isNaN(d) : "NaN found";
				pw.print(d);
			}
			pw.println();
		}
		pw.close();
	}

	public static void main(String[] a) throws Exception {
		SisapFile[] sfs = { SisapFile.colors, SisapFile.nasa };

		List<Metric<CartesianPoint>> ms = new ArrayList<>();
		ms.add(new Euclidean<>());
		ms.add(new TriDiscrim<>());
		ms.add(new JensenShannonViaSed<>());
		ms.add(new CosineNormalised<>());

		for (SisapFile sf : sfs) {
			TestLoad tl = new TestLoad(sf);
			for (Metric<CartesianPoint> metric : ms) {
				generateFile(tl, sf.toString(), metric);
			}
		}
	}

	private static void generateFile(TestLoad tl, String dataName,
			Metric<CartesianPoint> metric) throws Exception {
		List<CartesianPoint> dat = tl.getQueries(10000);

		SimplexDistanceFileOutput<CartesianPoint> s = new SimplexDistanceFileOutput<>(
				dat, metric, dataName + "_" + metric.getMetricName());

		int[] dims = { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		s.writeDists("/Volumes/Data/simplexes/output_temp/", dims);
		System.out.println("done");
	}
}
