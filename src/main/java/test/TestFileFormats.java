package test;

import java.io.File;

import dataSets.fileReaders.RealArrayFileReader;

/**
 * @author Richard Connor
 *
 */
public class TestFileFormats {

	/**
	 * @param args
	 *            requires a single argument, the directory path of the files
	 */
	public static void main(String[] args) {
		try {
			
			boolean assertionsEnabled = false;
			try {
				assert assertionsEnabled : "assertions are not enabled";
				throw new Exception("assertions must be enabled to run tests");
			} catch (AssertionError e) {
				// does nothing, if assertions are enabled this just stops anything happening..
			}
			

			if (args.length == 1) {
				String dirName = args[0];
				File f = new File(dirName);
				if (f.exists() && f.isDirectory()) {
					testRawObjectData(f.getAbsolutePath());

				} else {
					System.out.println("<" + dirName + "> is not a directory");
				}
			} else {
				System.out
						.println("Please supply source directory as parameter");
				for (String s : args) {
					System.out.println("<" + s + ">");
				}
			}
		} catch (AssertionError ae) {
			System.out.println("assertion failed: " + ae.getMessage());
		} catch (Throwable t) {
			System.out.println("failed: " + t.getMessage());
		}
	}

	private static void testRawObjectData(String s) throws Exception {

		RealArrayFileReader objData = new RealArrayFileReader(new File(s
				+ "/java_all.obj"));
		RealArrayFileReader original = new RealArrayFileReader(10000, 150, s
				+ "/eh_descriptors", "eh", ".txt", 1, 1, false);

		for (int i = 0; i < 100; i++) {
			double[] x = objData.getRawData()[i];
			double[] y = original.getRawData()[i];
			assert x.length == y.length : "arrays are different lengths";
			for (int j = 0; j < x.length; j++) {
				assert x[j] == y[j] : "different values, row " + i
						+ ", column " + j;
			}
		}
		assert false : "no failure, finished successfully";

	}

}
