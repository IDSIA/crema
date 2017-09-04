package ch.idsia.crema.factor.credal.linear;


import ch.idsia.crema.factor.credal.SeparatelySpecified;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;

public interface SeparateLinearFactor<F extends SeparateLinearFactor<F>> extends SeparatelySpecified<F>, LinearFactor {
	public LinearConstraintSet getLinearProblem(int... states); 
	public LinearConstraintSet getLinearProblemAt(int offset);
	
	@Override
	public F copy();
}
