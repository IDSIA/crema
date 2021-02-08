package ch.idsia.crema.utility.hull;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static ch.idsia.crema.utility.ProbabilityUtil.infoLoss;

public class ReducedHull implements ConvexHull{

    private int numPoints = -1;
    ArrayList<double[]> vertices;
    HashMap<double[], Map<double[], Double>> dist;
    HashMap<double[], Map<double[], double[]>>  repl;


    private void init(double[][] vertices){

        // Get list structures: points, distances, midpoint
        this.dist = new HashMap<double[], Map<double[], Double>> ();
        this.repl = new HashMap<double[], Map<double[], double[]>> ();
        this.vertices = new ArrayList<>();

        for(int i=0; i<vertices.length; i++) {
            addInfoPoint(vertices[i]);
        }

    }

    private void removeNeighbour(){

        double minDist = Double.POSITIVE_INFINITY;
        double[] p1=null, p2=null;
        double[] q = null;

        // find the pair of point with the minimal loss
        for(int i=0; i<vertices.size()-1; i++){
            for(int j=i+1; j<vertices.size(); j++) {
                double d = dist.get(vertices.get(i)).get(vertices.get(j));
                if(minDist>d){
                    minDist = d;
                    p1 = vertices.get(i);
                    p2 = vertices.get(j);
                }
            }
        }

        // get the replacement
        q = repl.get(p1).get(p2);

        // remove the existing information
        removeInfoPoint(p1);
        removeInfoPoint(p2);

        // add new point and update distances
        addInfoPoint(q);
    }


    private void removeInfoPoint(double[] p){
        vertices.remove(p);
        repl.remove(p);
        dist.remove(p);
        for(double[] pi : vertices){
            repl.get(pi).remove(p);
            dist.get(pi).remove(p);
        }
    }


    private void addInfoPoint(double[] p){
        dist.put(p, new HashMap<double[], Double>());
        repl.put(p, new HashMap<double[], double[]>());

        for(double[] pi : vertices) {
            // Compute replacement point
            double q[] = IntStream.range(0,pi.length).mapToDouble(k -> 0.5*(p[k] + pi[k])).toArray();
            double d = infoLoss(p, pi, q, true);
            dist.get(p).put(pi, d);
            dist.get(pi).put(p, d);
            repl.get(p).put(pi, q);
            repl.get(pi).put(p, q);

        }
        vertices.add(p);
    }


    /**
     * Set the number of points
     * @param numPoints
     * @return
     */
    public ReducedHull setNumPoints(int numPoints) {
        this.numPoints = numPoints;
        return this;
    }

    /**
     * Method that applies the given convex hull method to a list of vertices.
     * @param vertices: 2D array of doubles where the first dimension is the number of points and the second
     *                is the dimensionality of the points.
     * @return Array of vertices after applying the convex hull method.
     */
    @Override
    public double[][] apply(double[][] vertices) {
        double[][] hull = ConvexHull.as(Method.LP_CONVEX_HULL).apply(vertices);
        int m = numPoints;
        if(m<0) m = hull.length-1;
        init(vertices);
        while(this.vertices.size()>m)
            removeNeighbour();
        return this.vertices.toArray(double[][]::new);
    }

}
