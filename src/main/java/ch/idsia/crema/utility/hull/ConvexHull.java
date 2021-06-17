package ch.idsia.crema.utility.hull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// TODO: this interface can be replaced with the internal enum
public interface ConvexHull {

	enum Method {
		DEFAULT,
		LP_CONVEX_HULL,
		QUICK_HULL,
		REDUCED_HULL,
		REDUCED_HULL_2,
		REDUCED_HULL_3,
		REDUCED_HULL_4,
		REDUCED_HULL_5,
		REDUCED_HULL_10,
		REDUCED_HULL_50;

		static Map<Method, Supplier<ConvexHull>> constructors = new HashMap<>();

		static {
			constructors.put(DEFAULT, LPConvexHull::new);
			constructors.put(LP_CONVEX_HULL, LPConvexHull::new);
			constructors.put(QUICK_HULL, QuickHull::new);
			constructors.put(REDUCED_HULL, ReducedHull::new);
			constructors.put(REDUCED_HULL_2, () -> new ReducedHull(2));
			constructors.put(REDUCED_HULL_3, () -> new ReducedHull(3));
			constructors.put(REDUCED_HULL_4, () -> new ReducedHull(4));
			constructors.put(REDUCED_HULL_5, () -> new ReducedHull(5));
			constructors.put(REDUCED_HULL_10, () -> new ReducedHull(10));
			constructors.put(REDUCED_HULL_50, () -> new ReducedHull(50));
		}
	}

	/**
	 * Method that applies the given convex hull method to a list of vertices.
	 *
	 * @param vertices: 2D array of doubles where the first dimension is the number of points and the second
	 *                  is the dimensionality of the points.
	 * @return Array of vertices after applying the convex hull method.
	 */
	double[][] apply(double[][] vertices);

	/**
	 * Method that allows to instantiate any of the implemented methods for convex hull
	 *
	 * @param method
	 * @return
	 */
	static ConvexHull as(Method method) {
		return Method.constructors.get(method).get();
	}

}
