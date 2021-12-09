package dataPoints.cartesian;

import java.util.Map;
import java.util.TreeMap;

import coreConcepts.Metric;

/**
 * some useful methods for CartesianPoint types
 * 
 * @author Richard Connor
 * 
 */
public class CartesianPointMetrics<T extends CartesianPoint> extends
		TreeMap<String, Metric<T>> {

	public CartesianPointMetrics() {
		this.put("Sed", new SEDByComplexity<T>());
		this.put("Jsd", new JensenShannon<T>(true, false));
		this.put("Cos", new Cosine<T>());
		this.put("CosN", new CosineNormalised<T>());
		this.put("Euc", new Euclidean<T>());
		this.put("Man", new Manhattan<T>());
		this.put("Tri", new TriDiscrim<T>());
	}

	/**
	 * @return a set of useful metrics over CartesianPoint types
	 */
	public static <T extends CartesianPoint> Map<String, Metric<T>> getCartesianPointMetrics() {
		Map<String, Metric<T>> metrics = new TreeMap<>();
		metrics.put("sed", new SEDByComplexity<T>());
		metrics.put("jsd", new JensenShannon<T>(true, false));
		metrics.put("cos", new Cosine<T>());
		metrics.put("euc", new Euclidean<T>());
		metrics.put("man", new Manhattan<T>());
		metrics.put("tri", new TriDiscrim<T>());

		return metrics;
	}

}
