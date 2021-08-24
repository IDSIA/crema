package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;


public class BayesUAIWriter extends NetUAIWriter<BayesianNetwork> {

	public BayesUAIWriter(BayesianNetwork target, String filename) {
		super(target, filename);
		TYPE = UAITypes.BAYES;
	}

	@Override
	protected void sanityChecks() {
		// TODO: there aren't right?
	}

	@Override
	protected void writeFactors() {
		append("");
		for (int v : target.getVariables()) {

			final BayesianFactor f = target.getFactor(v);
			final int vsize = f.getDomain().getCardinality(v);

			final int[] vars = ArraysUtil.append(
					new int[]{v},
					ArraysUtil.reverse(target.getParents(v))
			);

			// TODO: verify this
			final IndexIterator it = f.getDomain().getReorderedIterator(vars);

			final double[] probs = new double[f.getDomain().getCombinations()];

			for (int i = 0; i < probs.length; i++) {
				probs[i] = f.getValueAt(it.next());
			}

			append(probs.length);

			for (double[] p : ArraysUtil.reshape2d(probs, probs.length / vsize, vsize))
				append("", str(p));

			append("");
		}
	}

	@Override
	protected void writeTarget() {
		writeType();
		writeVariablesInfo();
		writeDomains();
		writeFactors();
	}

	@Override
	protected void writeDomains() {
		// write the number of factors
		append(target.getVariables().length);

		// add the factor domains with children at the end
		for (int v : target.getVariables()) {
			int[] parents = ArraysUtil.reverse(target.getParents(v));
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

	public static boolean isCompatible(Object target) {
		return target instanceof BayesianNetwork;
	}

}
