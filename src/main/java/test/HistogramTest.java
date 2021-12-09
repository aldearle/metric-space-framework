package test;

import histogram.Histogram;

public class HistogramTest {

	public static void main(String[] args) {
		try {
			try {
				assert false : "assertions not enabled!";
			} catch (AssertionError e) {
				// do nothing
			}
			Histogram h = new Histogram(1000, 5, 6);
			h.addValue(5.1);
			System.out.println(h.getValuesInHistogram());
			System.out.println(h.getValuesOverUpb());
			
			

			assert false : "no error found";

		} catch (AssertionError e) {
			System.out.println("assertion error: " + e.getMessage());
		}
	}

}
