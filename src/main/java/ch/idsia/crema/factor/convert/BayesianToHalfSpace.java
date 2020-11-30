package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Converter;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import com.google.common.primitives.Doubles;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.List;

public class BayesianToHalfSpace implements Converter<BayesianFactor, SeparateHalfspaceFactor> {
	public static final BayesianToHalfSpace INSTANCE = new BayesianToHalfSpace();
	
	@Override
	public SeparateHalfspaceFactor apply(BayesianFactor cpt, Integer var) {

		Strides left = Strides.as(var, cpt.getDomain().getCardinality(var));
		Strides right = cpt.getDomain().remove(var);
		SeparateHalfspaceFactor factor = new SeparateHalfspaceFactor(left, right);
		int left_var_size = cpt.getDomain().getCardinality(var);
		List cpt_data = Doubles.asList(cpt.getData());

		for(int i=0; i<right.getCombinations(); i++){
			double[] v = Doubles.toArray(cpt_data.subList(i*left_var_size, (i+1)*left_var_size));
			for(int j=0; j<left_var_size; j++){
				// Value constraint
				double[] data = new double[left_var_size];
				data[j] = 1.0;
				factor.addConstraint(data, Relationship.EQ, v[j], i);

				// non-negative constraints
				factor.addConstraint(data, Relationship.GEQ, 0.0, i);
			}

			// normalization constraint
			double [] ones =  new double[left_var_size];
			for(int j=0; j<ones.length; j++)
				ones[j] = 1.;
			factor.addConstraint(ones, Relationship.EQ, 1.0, i);

		}

		return factor;
	}

	@Override
	public Class<SeparateHalfspaceFactor> getTargetClass() {
		return SeparateHalfspaceFactor.class;
	}

	@Override
	public Class<BayesianFactor> getSourceClass() {
		return BayesianFactor.class;
	}
}
