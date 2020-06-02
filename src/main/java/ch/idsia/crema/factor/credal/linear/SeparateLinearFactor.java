package ch.idsia.crema.factor.credal.linear;


import org.apache.commons.math3.optim.linear.LinearConstraintSet;

import ch.idsia.crema.factor.credal.SeparatelySpecified;

public interface SeparateLinearFactor<F extends SeparateLinearFactor<F>> extends SeparatelySpecified<F>, LinearFactor {
	public LinearConstraintSet getLinearProblem(int... states); 
	public LinearConstraintSet getLinearProblemAt(int offset);
	
	@Override
	public F copy();
}
