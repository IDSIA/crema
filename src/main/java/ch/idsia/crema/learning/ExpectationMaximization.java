package ch.idsia.crema.learning;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.JoinInference;
import ch.idsia.crema.model.graphical.GraphicalModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author:  Rafael Cabañas and Claudio "Dna" Bonesana
 * Project: CreMA
 */
public abstract class ExpectationMaximization<F extends Factor, M extends ExpectationMaximization> {

    protected JoinInference<F, F> inferenceEngine;

    protected  GraphicalModel<F> priorModel;

    protected GraphicalModel<F> posteriorModel;

    protected boolean inline = false;

    protected boolean verbose = false;

    protected int[] trainableVars = null;

    protected boolean recordIntermediate = false;

    protected List<GraphicalModel<F>> intermediateModels;

    protected boolean stopAtConvergence =  true;

    protected boolean updated;

    protected int performedIterations = 0;

    protected double klthreshold = 0.0;

    protected abstract void stepPrivate(Collection stepArgs) throws InterruptedException;

    public void step(Collection stepArgs) throws InterruptedException {
        stepPrivate(stepArgs);
        performedIterations++;
        if(recordIntermediate)
            addIntermediateModels(posteriorModel);

    }


    public void run(Collection stepArgs, int iterations) throws InterruptedException {
        init();
        for(int i=1; i<=iterations; i++) {
            if(verbose){
                if(i % 10 == 0)
                    System.out.print("\n"+i+" iterations ");
                else
                    System.out.print(".");
            }
            step(stepArgs);
            if(stopAtConvergence && !updated)
                break;

        }
    }

    private void init(){
        if(!inline)
            this.posteriorModel = priorModel.copy();
        else
            this.posteriorModel = priorModel;

        if(trainableVars == null)
            trainableVars = posteriorModel.getVariables();

        if(recordIntermediate) {
            intermediateModels = new ArrayList<GraphicalModel<F>>();
            addIntermediateModels(priorModel);
        }

    }

    public GraphicalModel<F> getPosterior() {
        return posteriorModel;
    }



    public M setInline(boolean inline) {
        this.inline = inline;
        return (M) this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public M setVerbose(boolean verbose) {
        this.verbose = verbose;
        return (M) this;
    }

    public M setTrainableVars(int[] trainableVars) {
        this.trainableVars = trainableVars;
        return (M) this;
    }

    public int[] getTrainableVars() {
        return trainableVars;
    }


    public M setRecordIntermediate(boolean recordIntermediate) {
        this.recordIntermediate = recordIntermediate;
        return (M) this;
    }

    public boolean isRecordIntermediate() {
        return recordIntermediate;
    }

    public List<GraphicalModel<F>> getIntermediateModels() {
        return intermediateModels;
    }

    public boolean isStopAtConvergence() {
        return stopAtConvergence;
    }

    public M  setStopAtConvergence(boolean stopAtConvergence) {
        this.stopAtConvergence = stopAtConvergence;
        return (M) this;
    }

    public M setKlthreshold(double klthreshold) {
        this.klthreshold = klthreshold;
        return (M) this;
    }

    public int getPerformedIterations() {
        return performedIterations;
    }


    public double getKlthreshold() {
        return klthreshold;
    }

    public JoinInference<F, F> getInferenceEngine() {
        return inferenceEngine;
    }

    public M setInferenceEngine(JoinInference<F, F> inferenceEngine) {
        this.inferenceEngine = inferenceEngine;
        return (M) this;
    }

    protected void addIntermediateModels(GraphicalModel<F> model) {
        this.intermediateModels.add(model.copy());
    }

    protected void setPerformedIterations(int performedIterations) {
        this.performedIterations = performedIterations;
    }

    protected void setUpdated(boolean updated) {
        this.updated = updated;
    }

}
