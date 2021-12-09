package test;

import java.util.List;

import testloads.CartesianThresholds;
import testloads.TestLoad;
import testloads.TestLoad.SisapFile;
import dataPoints.cartesian.CartesianPoint;

public class Scratch {

	public static void main(String[] args) throws Exception {

		TestLoad tl = new TestLoad(SisapFile.nasa);
		checkForZeroVals(tl);
		CartesianThresholds ct = new CartesianThresholds();
		
	}

	private static void checkForZeroVals(TestLoad tl) {
		List<CartesianPoint> dat = tl.getDataCopy();
		int ptr = 0;
		for (CartesianPoint d : dat) {
			double acc = 0;
			for (double db : d.getPoint()) {
				acc += db;
			}
			if (acc == 0.0) {
				System.out.println(ptr + " is all zeros");
			}
			ptr++;
		}
	}

}
