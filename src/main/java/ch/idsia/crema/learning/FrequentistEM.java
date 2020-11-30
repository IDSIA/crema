package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.JoinInference;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.IntStream;

public class FrequentistEM extends DiscreteEM<FrequentistEM> {

    private double regularization = 0.00001;

    private TIntObjectMap<BayesianFactor> counts;

    public FrequentistEM(GraphicalModel<BayesianFactor> model,
                                   JoinInference<BayesianFactor, BayesianFactor> inferenceEngine) {
        this.inferenceEngine = inferenceEngine;
        this.priorModel = model;
    }

    public FrequentistEM(GraphicalModel<BayesianFactor> model, int[] elimSeq){
        this.inferenceEngine = getDefaultInference(model, elimSeq);;
        this.priorModel = model;
    }

    public FrequentistEM(GraphicalModel<BayesianFactor> model) {
        this(model, (new MinFillOrdering()).apply(model));
    }


    protected void stepPrivate(Collection stepArgs) throws InterruptedException {
        // E-stage
        TIntObjectMap<BayesianFactor> counts = expectation((TIntIntMap[]) stepArgs.toArray(TIntIntMap[]::new));
        // M-stage
        maximization(counts);

    }

    protected TIntObjectMap<BayesianFactor> expectation(TIntIntMap[] observations) throws InterruptedException {

        TIntObjectMap<BayesianFactor> counts = new TIntObjectHashMap<>();
        for (int variable : posteriorModel.getVariables()) {
            counts.put(variable, new BayesianFactor(posteriorModel.getFactor(variable).getDomain(), false));
        }

        for (TIntIntMap observation : observations) {

            for (int var : trainableVars) {

                int[] relevantVars = ArraysUtil.addToSortedArray(posteriorModel.getParents(var), var);
                int[] hidden =  IntStream.of(relevantVars).filter(x -> !observation.containsKey(x)).toArray();
                int[] obsVars = IntStream.of(relevantVars).filter(x -> observation.containsKey(x)).toArray();


                if(hidden.length>0){
                    // Case with missing data
                    BayesianFactor phidden_obs = inferenceEngine.apply(posteriorModel, hidden, observation);
                    if(obsVars.length>0)
                        phidden_obs = phidden_obs.combine(
                                BayesianFactor.getJoinDeterministic(posteriorModel.getDomain(obsVars), observation));

                    counts.put(var, counts.get(var).addition(phidden_obs));
                    if(Double.isNaN(counts.get(var).getData()[0]))
                        System.out.println();
                }else{
                    //fully-observable case
                    for(int index : counts.get(var).getDomain().getCompatibleIndexes(observation)){
                        double x = counts.get(var).getValueAt(index) + 1;
                        counts.get(var).setValueAt(x, index);
                    }
                }
            }
        }

        return counts;
    }

    private void maximization(TIntObjectMap<BayesianFactor> counts){

        updated = false;
        for (int var : trainableVars) {
            BayesianFactor countVar = counts.get(var);

            if(regularization>0.0) {

                BayesianFactor reg = posteriorModel.getFactor(var).scalarMultiply(regularization);
                countVar = countVar.addition(reg);

            }

            BayesianFactor f = countVar.divide(countVar.marginalize(var));

            if(f.KLdivergence(posteriorModel.getFactor(var)) > klthreshold) {
                posteriorModel.setFactor(var, f);
                updated = true;
            }


        }
    }


    public FrequentistEM setRegularization(double regularization) {
        this.regularization = regularization;
        return this;
    }

    public double getRegularization() {
        return regularization;
    }



    public static void main(String[] args) throws InterruptedException {


        // https://www.cse.ust.hk/bnbook/pdf/l07.h.pdf
        BayesianNetwork model = new BayesianNetwork();


        for(int i=0;i<3;i++)
            model.addVariable(2);

        int[] X = model.getVariables();


        model.addParent(X[1],X[0]);
        model.addParent(X[2],X[1]);

        model.setFactor(X[0], new BayesianFactor(model.getDomain(X[0]), new double[]{0.5,0.5}));
        model.setFactor(X[1], new BayesianFactor(model.getDomain(X[1],X[0]), new double[]{2./3, 1./3, 1./3, 2./3}));
        model.setFactor(X[2], new BayesianFactor(model.getDomain(X[2],X[1]), new double[]{2./3, 1./3, 1./3, 2./3}));



        int[][] dataX = {
                {0, 0, 0},
                {1, 1, 1},
                {0, -1, 0},
                {1, -1, 1}
        };

        TIntIntMap[] observations = new TIntIntMap[dataX.length];
        for(int i=0; i<observations.length; i++) {
            observations[i] = new TIntIntHashMap();
            for(int j=0; j<dataX[i].length; j++) {
                if(dataX[i][j]>=0)
                    observations[i].put(X[j], dataX[i][j]);
            }
            System.out.println(observations[i]);
        }


        //RandomUtil.setRandomSeed(222);
        //model = (BayesianNetwork) BayesianFactor.randomModel(model, 4, false);


        FrequentistEM inf =
                new FrequentistEM(model)
                        .setRegularization(0.0)
                        .setInline(false)
                        .setVerbose(true);

        inf.run(Arrays.asList(observations),100);


        System.out.println("Posterior:");

        System.out.println(inf.getPosterior().getFactor(X[0])); //[0.5, 0.5]
        System.out.println("--");
        System.out.println(inf.getPosterior().getFactor(X[1]).filter(X[0],0)); //[0.9, 0.1]
        System.out.println(inf.getPosterior().getFactor(X[1]).filter(X[0],1)); // [0.1, 0.9]
        System.out.println("--");
        System.out.println(inf.getPosterior().getFactor(X[2]).filter(X[1],0)); // [0.9, 0.1]
        System.out.println(inf.getPosterior().getFactor(X[2]).filter(X[1],1)); // [0.1, 0.9]


    }

}
