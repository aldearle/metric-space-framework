package dataPoints.compactEnsemble;

import semanticDataTypes.StringShingle;
import coreConcepts.Metric;

/**
 * Returns the Levenshtein distance of the strings
 * 
 * @author Richard Connor (actually I think Robert Moss wrote the distance
 *         function, after Wikipedia!)
 * 
 * @param <T>
 */
public class Levenshtein<T extends StringShingle> implements Metric<T> {

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	@Override
	public double distance(T x, T y) {
		String str1 = x.toString();
		String str2 = y.toString();

		int[][] distanceAcc = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++) {
			distanceAcc[i][0] = i;
		}
		for (int j = 0; j <= str2.length(); j++) {
			distanceAcc[0][j] = j;
		}

		for (int i = 1; i <= str1.length(); i++) {
			for (int j = 1; j <= str2.length(); j++) {
				distanceAcc[i][j] = minimum(
						distanceAcc[i - 1][j] + 1,
						distanceAcc[i][j - 1] + 1,
						distanceAcc[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));
			}
		}

		return (distanceAcc[str1.length()][str2.length()]);
	}

	@Override
	public String getMetricName() {
		return "Levenshtein";
	}

}
