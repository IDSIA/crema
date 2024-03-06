package ch.idsia.crema.learning;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.model.graphical.GraphicalModel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author:  Rafael Cabañas and Claudio "Dna" Bonesana
 * Project: CreMA
 */
public abstract class ExpectationMaximization<F extends OperableFactor<F>> {

	protected InferenceJoined<GraphicalModel<F>, F> inferenceEngine;

	protected GraphicalModel<F> priorModel;

	protected GraphicalModel<F> posteriorModel;

	protected boolean inline = false;

	protected boolean verbose = false;

	protected int[] trainableVars = null;

	protected boolean recordIntermediate = false;

	protected List<GraphicalModel<F>> intermediateModels;

	protected boolean stopAtConvergence = true;

	protected boolean updated;

	protected int performedIterations = 0;

	protected double klthreshold = 0.0;

	protected abstract void stepPrivate(Collection<Int2IntMap> stepArgs) throws InterruptedException;

	public void step(Collection<Int2IntMap> stepArgs) throws InterruptedException {
		stepPrivate(stepArgs);
		performedIterations++;
		if (recordIntermediate)
			addIntermediateModels(posteriorModel);
	}

	public void run(Collection<Int2IntMap> stepArgs, int iterations) throws InterruptedException {
		init();
		for (int i = 1; i <= iterations; i++) {
			if (verbose) {
				if (i % 10 == 0)
					System.out.print("\n" + i + " iterations ");
				else
					System.out.print(".");
			}
			step(stepArgs);
			if (stopAtConvergence && !updated)
				break;
		}
	}

	private void init() {
		if (!inline)
			this.posteriorModel = priorModel.copy();
		else
			this.posteriorModel = priorModel;

		if (trainableVars == null)
			trainableVars = posteriorModel.getVariables();

		if (recordIntermediate) {
			intermediateModels = new ArrayList<>();
			addIntermediateModels(priorModel);
		}
	}

	public GraphicalModel<F> getPosterior() {
		return posteriorModel;
	}

	public ExpectationMaximization<F> setInline(boolean inline) {
		this.inline = inline;
		return this;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public ExpectationMaximization<F> setVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public ExpectationMaximization<F> setTrainableVars(int[] trainableVars) {
		this.trainableVars = trainableVars;
		return this;
	}

	public int[] getTrainableVars() {
		return trainableVars;
	}

	public ExpectationMaximization<F> setRecordIntermediate(boolean recordIntermediate) {
		this.recordIntermediate = recordIntermediate;
		return this;
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

	public ExpectationMaximization<F> setStopAtConvergence(boolean stopAtConvergence) {
		this.stopAtConvergence = stopAtConvergence;
		return this;
	}

	public ExpectationMaximization<F> setKlthreshold(double klthreshold) {
		this.klthreshold = klthreshold;
		return this;
	}

	public int getPerformedIterations() {
		return performedIterations;
	}

	public double getKlthreshold() {
		return klthreshold;
	}

	public Inference<GraphicalModel<F>, F> getInferenceEngine() {
		return inferenceEngine;
	}

	public ExpectationMaximization<F> setInferenceEngine(InferenceJoined<GraphicalModel<F>, F> inferenceEngine) {
		this.inferenceEngine = inferenceEngine;
		return this;
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
