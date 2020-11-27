package ch.idsia.crema.factor.convert;

import java.util.ArrayList;

import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;

import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.SeparateLinearFactor;
import ch.idsia.crema.core.Converter;

public class SeparateLinearToHalfspaceFactor implements Converter<SeparateLinearFactor, SeparateHalfspaceFactor> {

	/**
	 * when true the linear constraints obtained from the source factor are copied.
	 * Otherwise they are simply referenced. This is a compile time switch.
	 */
	private static final boolean copy = false;
	
	@Override
	public SeparateHalfspaceFactor apply(SeparateLinearFactor s, Integer var) {
		ArrayList<ArrayList<LinearConstraint>> data = new ArrayList<>(s.getSeparatingDomain().getCombinations());
		
		for (int offset = 0; offset < s.getSeparatingDomain().getCombinations(); ++offset) {
			LinearConstraintSet set = s.getLinearProblemAt(offset);
			if (copy) {
				ArrayList<LinearConstraint> new_constraints = new ArrayList<>(set.getConstraints().size());
				for (LinearConstraint constraint : set.getConstraints()) {
					RealVector coeff = constraint.getCoefficients().copy();
					new_constraints.add(new LinearConstraint(coeff, constraint.getRelationship(), constraint.getValue()));
				}
			} else {
				data.add(new ArrayList<>(set.getConstraints()));
			}
		}
		return new SeparateHalfspaceFactor(s.getDataDomain(), s.getSeparatingDomain(), data);
	}

	@Override
	public Class<SeparateHalfspaceFactor> getTargetClass() {
		return SeparateHalfspaceFactor.class;
	}

	@Override
	public Class<SeparateLinearFactor> getSourceClass() {
		return SeparateLinearFactor.class;
	}


}
