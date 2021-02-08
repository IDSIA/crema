package ch.idsia.crema.utility;

public class ProbabilityUtil {


    // Compute the symmetrized KL distance between v1 and v2
    public static double KLsymmetrized(double[] p, double[] q, boolean... zeroSafe){
        return KL(p,q,zeroSafe) + KL(q,p,zeroSafe);
    }

    // Compute the symmetrized KL distance between v1 and v2
    public static double KL(double[] p, double[] q, boolean... zeroSafe){

        if(zeroSafe.length>1) throw new IllegalArgumentException("Wrong number of arguments,");
        if(p.length != q.length) throw new IllegalArgumentException("Arrays of different sizes.");

        double distance = 0;
        int n = p.length;
        for(int i=0; i<n; i++){
            if(!zeroSafe[0] || (p[i]!=0 && q[i]!=0)) {
                distance += p[i] * (Math.log(p[i]) - Math.log(q[i]));
            }
        }
        return distance;
    }

    public static double infoLoss(double[] p1, double[] p2, double[] q, boolean... zeroSafe){
        return KL(p1, q, zeroSafe) + KL(p2, q, zeroSafe);
    }
}
