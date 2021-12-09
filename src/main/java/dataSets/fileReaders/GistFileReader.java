package dataSets.fileReaders;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("javadoc")
public class GistFileReader {

	public static final int N_SCALES = 6;
	public static final int N_ORIENTATIONS = 5;
	public static final int N_WINDOWS = 4;
	public static final int BYTES_PER_FLOAT = 4;

	public static final int GIST_SIZE = N_SCALES * N_ORIENTATIONS * N_WINDOWS
			* N_WINDOWS;

	public static final int GIST_SIZE_IN_BYTES = GIST_SIZE * BYTES_PER_FLOAT;

	protected static final int BUFFER_SIZE = 2048;

	private float[] gist_values;
	private double[] gist_doubles;
	private boolean doubleVals;

	public GistFileReader(String filename) throws IOException {
		this.doubleVals = false;
		FileInputStream in = new FileInputStream(filename);
		readData(in, false);
		in.close();
	} // GistFileReader

	/**
	 * if this constructor is used, the getGistDoubles method will work
	 * 
	 * @param filename
	 * @param normalised
	 *            set the double-precision values to normalised values which sum
	 *            to 1.0
	 * @throws IOException
	 */
	public GistFileReader(String filename, boolean normalised)
			throws IOException {
		this.doubleVals = true;
		FileInputStream in = new FileInputStream(filename);
		readData(in, normalised);
		in.close();
	} // GistFileReader

	public float[] getGistValues() {
		return this.gist_values;
	}

	/**
	 * @return null, unless normalised is set as true on construction
	 */
	public double[] getGistDoubles() {
		return this.gist_doubles;
	}

	private void readData(FileInputStream in, boolean normalised)
			throws IOException {
		byte[] bytes = new byte[BUFFER_SIZE];

		int nBytes = in.read(bytes);

		if (nBytes != GIST_SIZE_IN_BYTES) {
			throw new IOException("Wrong number of bytes");
		}

		parseBytes(bytes, normalised);

	}

	protected void parseBytes(byte[] bytes, boolean normalised) {
		double total = 0;
		this.gist_values = new float[GIST_SIZE];
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		int gist_idx = 0;

		for (int i = 0; i < GIST_SIZE_IN_BYTES; i += 4) {
			final float val = buf.getFloat(i);
			total += val;
			this.gist_values[gist_idx] = val;
			gist_idx++;
		}

		if (doubleVals) {
			this.gist_doubles = new double[GIST_SIZE];
			for (int i = 0; i < this.gist_values.length; i++) {
				if (normalised) {
					this.gist_doubles[i] = this.gist_values[i] / total;
				} else {
					this.gist_doubles[i] = this.gist_values[i];
				}
			}
		}

	}

	public static void main(String[] args) throws IOException {
		GistFileReader gfr = new GistFileReader(
				"/Volumes/Data partition/MF1M/data/features_gist/0/0.dat", true);
		float[] vals = gfr.getGistValues();
		for (float v : vals) {
			System.out.print(v + "\t");
		}
		System.out.println();
		double[] norms = gfr.getGistDoubles();
		for (double v : norms) {
			System.out.print(v + "\t");
		}
		System.out.println();
	}
}
