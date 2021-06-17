package ch.idsia.crema.utility.hull;

public interface ConvexHull {

	ConvexHull DEFAULT = new LPConvexHull();
	ConvexHull LP_CONVEX_HULL = new LPConvexHull();
	ConvexHull QUICK_HULL = new QuickHull();
	ConvexHull REDUCED_HULL = new ReducedHull();
	ConvexHull REDUCED_HULL_2 = new ReducedHull(2);
	ConvexHull REDUCED_HULL_3 = new ReducedHull(3);
	ConvexHull REDUCED_HULL_4 = new ReducedHull(4);
	ConvexHull REDUCED_HULL_5 = new ReducedHull(5);
	ConvexHull REDUCED_HULL_10 = new ReducedHull(10);
	ConvexHull REDUCED_HULL_50 = new ReducedHull(50);

	/**
	 * Method that applies the given convex hull method to a list of vertices.
	 *
	 * @param vertices: 2D array of doubles where the first dimension is the number of points and the second
	 *                  is the dimensionality of the points.
	 * @return Array of vertices after applying the convex hull method.
	 */
	double[][] apply(double[][] vertices);

}
