package pgm20.experiments;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.causality.CausalInference;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalAproxLP;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.models.causal.RandomChainMarkovian;
import gnu.trove.map.hash.TIntIntHashMap;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.DoubleStream;

public class ChainMarkovian {


    static StructuralCausalModel model;

    static TIntIntHashMap evidence, intervention;
    static int target;
    static double eps = 0.00001;

    static String method;

    static int warmups = 2;
    static int measures = 10;


    public static void main(String[] args) throws InterruptedException {
        try {
            ////////// Input arguments Parameters //////////

            /** Number of endogenous variables in the chain (should be 3 or greater)*/
            int N = 6;
            /** Number of states in the exogenous variables */
            int exoVarSize = 6;
            /** Inference method: CVE, CCVE, CCALP  **/
            method = "CVE";

            long seed = 1234;

            if(args.length>0){
                N = Integer.parseInt(args[0]);
                exoVarSize = Integer.parseInt(args[1]);
                method = args[2];
                seed = Long.parseLong(args[3]);
            }

            System.out.println("\nChainMarkovian: N="+N+" exovarsize="+exoVarSize+" method="+method+" seed="+seed);
            System.out.println("=================================================================");


            /////////////////////////////////

            /** Number of states in endogenous variables */
            int endoVarSize = 2;
            // Load the chain model
            model = RandomChainMarkovian.buildModel(N, endoVarSize, exoVarSize);
            // Query: P(X[N/2] | X[N-1]=0, do(X[0])=0)
            int[] X = model.getEndogenousVars();

            evidence = new TIntIntHashMap();
            evidence.put(X[N-1], 0);
            intervention = new TIntIntHashMap();
            intervention.put(X[0], 0);
            target = X[N/2];

            System.out.println("Running experiments...");

            double res[] = run();
            System.out.println(res[0] + "," + res[1]);

        }catch (Exception e){
            System.out.println(e);
            System.out.println("nan,nan");
        }catch (Error e){
            System.out.println(e);
            System.out.println("nan,nan");
        }


    }

    static double[] experiment(boolean verbose) throws InterruptedException {
        Instant start = Instant.now();

        double intervalSize = 0.0;

        if(method.equals("CVE")) {
            CausalInference inf1 = new CausalVE(model);
            BayesianFactor result1 = (BayesianFactor) inf1.query(target, evidence, intervention);
            if(verbose) System.out.println(result1);
        }else if(method.equals("CCVE")) {
            CausalInference inf2 = new CredalCausalVE(model);
            VertexFactor result2 = (VertexFactor) inf2.query(target, evidence, intervention);
            if (verbose) System.out.println(result2);
        }else if (method.equals("CCALP")) {
            CausalInference inf3 = new CredalCausalAproxLP(model).setEpsilon(eps);
            IntervalFactor result3 = (IntervalFactor) inf3.query(target, evidence, intervention);
            if(verbose) System.out.println(result3);
            intervalSize =  result3.getUpper(0)[0] - result3.getLower(0)[0];
        }else {
            throw new IllegalArgumentException("Unknown inference method");
        }

        Instant finish = Instant.now();
        double timeElapsed = Duration.between(start, finish).toNanos()/Math.pow(10,6);

        return new double[]{timeElapsed, intervalSize};
    }


    public static double[] run() throws InterruptedException {

        double time[] = new double[measures];
        double size[] = new double[measures];

        // Warm-up
        for(int i=0; i<warmups; i++){
            double[] out = experiment(false);
            System.out.println("Warm-up #"+i+" in "+out[0]+" ms.");
        }

        // Measures
        for(int i=0; i<measures; i++){

            double[] out = experiment(false);
            System.out.println("Measurement #"+i+" in "+out[0]+" ms. size="+out[1]);
            time[i] = out[0];
            size[i] = out[1];
        }

        return new double[]{ DoubleStream.of(time).average().getAsDouble(),  DoubleStream.of(size).average().getAsDouble()};
    }


}
