package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateLinearFactor;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class SeparateLinearToHalfspaceFactor implements Converter<SeparateLinearFactor, SeparateHalfspaceFactor> {

	/**
	 * When true the linear constraints obtained from the source factor are copied.
	 * Otherwise they are simply referenced. This is a compile time switch.
	 */
	private static final boolean copy = false;

	@Override
	public SeparateHalfspaceFactor apply(SeparateLinearFactor s, Integer var) {
		List<List<LinearConstraint>> data = new ArrayList<>(s.getSeparatingDomain().getCombinations());

		for (int offset = 0; offset < s.getSeparatingDomain().getCombinations(); ++offset) {
			LinearConstraintSet set = s.getLinearProblemAt(offset);
			if (copy) {
				List<LinearConstraint> new_constraints = new ArrayList<>(set.getConstraints().size());
				for (LinearConstraint constraint : set.getConstraints()) {
					RealVector coeff = constraint.getCoefficients().copy();
					new_constraints.add(new LinearConstraint(coeff, constraint.getRelationship(), constraint.getValue()));
				}
				// TODO: copy done but not saved
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
