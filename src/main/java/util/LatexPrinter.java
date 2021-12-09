package util;

import java.util.ArrayList;
import java.util.List;

import dataPoints.cartesian.CartesianPoint;

/**
 * @author Richard Connor
 *
 */
public class LatexPrinter {

	/**
	 * @param p 
	 * @return outputs a string suitable for inclusion as a line in a latex
	 *         tabular environment
	 */
	public static String pointToLatexString(CartesianPoint p) {
		StringBuffer b = new StringBuffer("point");

		for (double d : p.getPoint()) {
			float f = (Math.round(d * 1000)) / (float) 1000;
			b.append("&" + f);
		}
		return b.toString();
	}

	/**
	 * @param p1
	 * @param p2
	 * @param suffix1
	 * @param suffix2
	 * @return a LaTeX string for putting these points into a particular tabular style
	 */
	@SuppressWarnings("boxing")
	public static String pointPairToLatexString(CartesianPoint p1,
			CartesianPoint p2, String suffix1, String suffix2) {

		List<Double> p1List = new ArrayList<Double>();
		List<Double> p2List = new ArrayList<Double>();

		int counter = 0;
		for (double d1 : p1.getPoint()) {
			double d2 = p2.getPoint()[counter++];

			if (d1 != 0 || d2 != 0) {
				p1List.add(d1);
				p2List.add(d2);
			}
		}

		StringBuffer b = new StringBuffer("\nPoint1");

		for (double d : p1List) {
			b.append("&" + d);
		}

		b.append("&" + suffix1 + "\\\\");
		b.append("\n\\cline{2-" + (p1List.size() + 1) + "}\nPoint2");

		for (double d : p2List) {
			b.append("&" + d);
		}
		b.append("&" + suffix2 + "\\\\");
		b.append("\n\n\\hline\\hline\n\n");

		return b.toString();
	}

}
