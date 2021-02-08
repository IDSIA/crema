package ch.idsia.crema.utility.hull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface ConvexHull {

    enum Method {
        LP_CONVEX_HULL,
        QUICK_HULL,
        REDUCED_HULL;

        static Map<Method, Supplier<ConvexHull>> constructors = new HashMap();

        static {
            constructors.put(LP_CONVEX_HULL, LPConvexHull::new);
            constructors.put(QUICK_HULL, QuickHull::new);
            constructors.put(REDUCED_HULL, ReducedHull::new);
        }
    }


    /**
     * Method that applies the given convex hull method to a list of vertices.
     * @param vertices: 2D array of doubles where the first dimension is the number of points and the second
     *                is the dimensionality of the points.
     * @return Array of vertices after applying the convex hull method.
     */
    double[][] apply(double[][] vertices);

    /**
     * Method that allows to instantiate any of the implemented methods for convex hull
     * @param method
     * @return
     */
    static ConvexHull as(Method method){
        return Method.constructors.get(method).get();
    }


}
