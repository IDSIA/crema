package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.model.graphical.DAGModel;

import java.io.IOException;


public abstract class NetUAIWriter<T extends DAGModel<? extends OperableFactor<?>>> extends UAIWriter<T> {

	public NetUAIWriter(T target, String filename) {
		super(target, filename);
	}

	@Override
	protected void sanityChecks() {
		// Check model consistency
		if (!target.checkFactorsDAGConsistency())
			throw new IllegalArgumentException("Inconsistent model");
	}

	protected void writeVariablesInfo() {
		// Write the number of variables in the network
		append(target.getVariables().length);
		// Write the number of states of each variable
		append(target.getSizes(target.getVariables()));
	}

	protected void writeDomains() {
		// Write the number of factors
		append(target.getVariables().length);

		// Add the factor domains with children at the end
		for (int v : target.getVariables()) {
			int[] parents = target.getParents(v);
			if (parents.length == 0)
				append("1", str(v));
			else
				append(
						str(parents.length + 1),
						str(parents),
						str(v)
				);
		}
	}

	protected abstract void writeFactors() throws IOException;

}
