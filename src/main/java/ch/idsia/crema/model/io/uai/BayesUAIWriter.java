package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.utility.ArraysUtil;
import com.google.common.primitives.Ints;


public class BayesUAIWriter extends NetUAIWriter<BayesianNetwork> {

	public BayesUAIWriter(BayesianNetwork target, String filename) {
		super(target, filename);
		TYPE = UAITypes.BAYES;
	}

	@Override
	protected void sanityChecks() {
		// TODO
	}

	@Override
	protected void writeFactors() {
		append("");
		for (int v : target.getVariables()) {

			BayesianFactor f = target.getFactor(v);
			int vsize = f.getDomain().getCardinality(v);

			f = f.reorderDomain(Ints.concat(new int[]{v},
					ArraysUtil.reverse(target.getParents(v))
			));

			double[] probs = f.getData();
			append(probs.length);

			for (double[] p : ArraysUtil.reshape2d(probs, probs.length / vsize, vsize))
				append("", str(p));

			// append(probs);

			append("");

/*
            if(f != null){
                if(target.isEndogenous(v)) {
                   int[] assig = target.getFactor(v).getAssignments(target.getParents(v));
                   tofile(assig.length+"\t");
                   tofileln(assig);
                }else{
                    double[] probs = target.getFactor(v).getData();
                    tofile(probs.length+"\t");
                    tofileln(probs);

                }
            }else{
                tofileln(0);
            }
 */
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
