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
import ch.idsia.crema.models.causal.RandomChainNonMarkovian;
import ch.idsia.crema.utility.InvokerWithTimeout;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.hash.TIntIntHashMap;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeoutException;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static ch.idsia.crema.models.causal.RandomChainNonMarkovian.buildModel;


public class RunExperiments {


    static StructuralCausalModel model;

    static TIntIntHashMap evidence, intervention;
    static int target;
    static double eps;

    static String method;

    static int warmups = 3;
    static int measures = 10;

    static boolean verbose = false;

    static long TIMEOUT = 5*60;


    public static void main(String[] args) throws InterruptedException {
        try {
            ////////// Input arguments Parameters //////////

            String modelName = "ChainNonMarkovian";

            /** Number of endogenous variables in the chain (should be 3 or greater)*/
            int N = 5;
            /** Number of states in the exogenous variables */
            int exoVarSize = 6;

            target = N/2;

            int obsvar = N - 1;

            int dovar = 0;

            /** Inference method: CVE, CCVE, CCALP, CCALPeps  **/
            method = "CVE";

            eps = 0.0;

            long seed = 1234;


            // ChainNonMarkovian 6 5 1 -1 0 CCALP 1234 0 1
            if (args.length > 0) {
                modelName = args[0];
                N = Integer.parseInt(args[1]);
                exoVarSize = Integer.parseInt(args[2]);
                target = Integer.parseInt(args[3]);
                obsvar = Integer.parseInt(args[4]);
                dovar = Integer.parseInt(args[5]);
                method = args[6];
                seed = Long.parseLong(args[7]);
                if(args.length>8) {
                    warmups = Integer.parseInt(args[8]);
                    measures = Integer.parseInt(args[9]);
                }

            }

            if (method.equals("CCALPeps"))
                eps = 0.000001;

            System.out.println("\n" + modelName + "\n   N=" + N + " exovarsize=" + exoVarSize + " target=" + target + " obsvar=" + obsvar + " dovar=" + dovar + " method=" + method + " seed=" + seed);
            System.out.println("=================================================================");


            /////////////////////////////////
            RandomUtil.getRandom().setSeed(seed);


            /** Number of states in endogenous variables */
            int endoVarSize = 2;
            // Load the chain model

            if (modelName.equals("ChainMarkovian"))
                model = RandomChainMarkovian.buildModel(N, endoVarSize, exoVarSize);
            else if (modelName.equals("ChainNonMarkovian"))
                model = RandomChainNonMarkovian.buildModel(N, endoVarSize, exoVarSize);
            else
                throw new IllegalArgumentException("Non valid model name");


            int[] X = model.getEndogenousVars();

            evidence = new TIntIntHashMap();
            if (obsvar >= 0) evidence.put(obsvar, 0);

            intervention = new TIntIntHashMap();
            if (dovar >= 0) intervention.put(dovar, 0);

            System.out.println("Running experiments...");

            double res[] = run();
            for(int i=0; i<res.length; i++){
                if(i!=res.length-1)
                    System.out.print(res[i]+",");
                else
                    System.out.println(res[i]);
            }

        }catch (TimeoutException e){
            System.out.println(e);
            System.out.println("inf,inf,nan,nan,nan");
        }catch (Exception e){
            System.out.println(e);
            System.out.println("nan,nan,nan,nan,nan");
        }catch (Error e){
            System.out.println(e);
            System.out.println("nan,nan,nan,nan,nan");
        }


    }

    static double[] experiment() throws InterruptedException {
        Instant start = Instant.now();
        Instant queryStart = null;


        double intervalSize = 0.0;
        double lowerBound = 0;
        double upperBound = 0;

        if(method.equals("CVE")) {
            CausalInference inf1 = new CausalVE(model);
            queryStart = Instant.now();
            BayesianFactor result1 = (BayesianFactor) inf1.query(target, evidence, intervention);
            if(verbose) System.out.println(result1);
            lowerBound = result1.getData()[0];
            upperBound = lowerBound;



        }else if(method.equals("CCVE")) {
            CausalInference inf2 = new CredalCausalVE(model);
            queryStart = Instant.now();
            VertexFactor result2 = (VertexFactor) inf2.query(target, evidence, intervention);
            if (verbose) System.out.println(result2);

            lowerBound = Stream.of(result2.filter(target,0).getData()[0]).mapToDouble(v->v[0]).min().getAsDouble();
            upperBound = Stream.of(result2.filter(target,0).getData()[0]).mapToDouble(v->v[0]).max().getAsDouble();



        }else if (method.startsWith("CCALP")) {
            CausalInference inf3 = new CredalCausalAproxLP(model).setEpsilon(eps);
            queryStart = Instant.now();
            IntervalFactor result3 = (IntervalFactor) inf3.query(target, evidence, intervention);
            if(verbose) System.out.println(result3);

            lowerBound =  result3.getLower(0)[0];
            upperBound =  result3.getUpper(0)[0];


        }else {
            throw new IllegalArgumentException("Unknown inference method");
        }

        Instant finish = Instant.now();
        double timeElapsed = Duration.between(start, finish).toNanos()/Math.pow(10,6);
        double timeElapsedQuery = Duration.between(queryStart, finish).toNanos()/Math.pow(10,6);



        if(lowerBound>upperBound) {
            double aux = lowerBound;
            lowerBound = upperBound;
            upperBound = aux;
        }

        intervalSize = upperBound - lowerBound;

        if(intervalSize<0.0) throw new RuntimeException("negative interval size");
        return new double[]{timeElapsed, timeElapsedQuery, intervalSize, lowerBound, upperBound};
    }


    public static double[] run() throws InterruptedException, TimeoutException {

        double time[] = new double[measures];
        double time2[] = new double[measures];
        double size[] = new double[measures];
        double lbound[] = new double[measures];
        double ubound[] = new double[measures];

        ch.idsia.crema.utility.InvokerWithTimeout<double[]> invoker = new InvokerWithTimeout<>();

        // Warm-up
        for(int i=0; i<warmups; i++){
            double[] out = invoker.run(RunExperiments::experiment, TIMEOUT*2);
            System.out.println("Warm-up #"+i+" in "+out[0]+" ms.");
        }

        // Measures
        for(int i=0; i<measures; i++){

            double[] out = invoker.run(RunExperiments::experiment, TIMEOUT);
            System.out.println("Measurement #"+i+" in "+out[0]+" ms. size="+out[2]);
            time[i] = out[0];
            time2[i] = out[1];
            size[i] = out[2];
            lbound[i] = out[3];
            ubound[i] = out[4];
        }

        return new double[]{    DoubleStream.of(time).average().getAsDouble(),
                                DoubleStream.of(time2).average().getAsDouble(),
                                DoubleStream.of(size).average().getAsDouble(),
                                DoubleStream.of(lbound).average().getAsDouble(),
                                DoubleStream.of(ubound).average().getAsDouble(),
        };
    }


}
