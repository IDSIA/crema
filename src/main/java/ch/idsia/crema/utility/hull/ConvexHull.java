package ch.idsia.crema.utility.hull;

public interface ConvexHull {

    double[][] apply(double[][] vertices);

    // todo: also to use an enum as well.
    static ConvexHull method(String methodName){
        if(methodName=="lp")
            return new LPConvexHull();
        else if (methodName=="quick")
            return new QuickHull();
        else
            throw new IllegalArgumentException("Unknown convex hull method");
    }


}
