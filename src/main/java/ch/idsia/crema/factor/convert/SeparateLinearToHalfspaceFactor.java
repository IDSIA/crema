package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceDefaultFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateLinearFactor;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class SeparateLinearToHalfspaceFactor implements Converter<SeparateLinearFactor, SeparateHalfspaceFactor> {

	@Override
	public SeparateHalfspaceFactor apply(SeparateLinearFactor s, Integer var) {
		TIntObjectMap<List<LinearConstraint>> data = new TIntObjectHashMap<>(s.getSeparatingDomain().getCombinations());

		for (int offset = 0; offset < s.getSeparatingDomain().getCombinations(); ++offset) {
			LinearConstraintSet set = s.getLinearProblemAt(offset);
			List<LinearConstraint> new_constraints = new ArrayList<>(set.getConstraints().size());
			for (LinearConstraint constraint : set.getConstraints()) {
				RealVector coeff = constraint.getCoefficients().copy();
				new_constraints.add(new LinearConstraint(coeff, constraint.getRelationship(), constraint.getValue()));
			}
			data.put(offset, new_constraints);
		}
		return new SeparateHalfspaceDefaultFactor(s.getDataDomain(), s.getSeparatingDomain(), data);
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
