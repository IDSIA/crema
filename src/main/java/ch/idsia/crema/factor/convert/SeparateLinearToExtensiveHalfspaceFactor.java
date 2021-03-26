package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.credal.linear.ExtensiveHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.SeparateLinearFactor;
import ch.idsia.crema.utility.IndexIterator;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


@SuppressWarnings("rawtypes")
public class SeparateLinearToExtensiveHalfspaceFactor implements Converter<SeparateLinearFactor, ExtensiveHalfspaceFactor> {

	public ExtensiveHalfspaceFactor apply(SeparateLinearFactor s) {
		return apply(s, -1);
	}

	@Override
	public ExtensiveHalfspaceFactor apply(SeparateLinearFactor s, Integer var) {
		Strides combined = s.getDataDomain().union(s.getSeparatingDomain());

		List<LinearConstraint> data = new ArrayList<>();

		IndexIterator group_iter = combined.getFiteredIndexIterator(s.getDataDomain().getVariables(), new int[s.getDataDomain().getSize()]);

		for (int offset = 0; offset < s.getSeparatingDomain().getCombinations(); ++offset) {
			int target_offset = group_iter.next();

			LinearConstraintSet set = s.getLinearProblemAt(offset);
			Collection<LinearConstraint> constraints = set.getConstraints();

			// now we need to change the columns according to the combined domain
			ArrayList<RealVector> params = new ArrayList<>();

			for (int i = 0; i < constraints.size(); ++i) {
				params.add(new OpenMapRealVector(combined.getCombinations()));
			}

			IndexIterator data_iter = combined.getFiteredIndexIterator(s.getSeparatingDomain().getVariables(), new int[s.getSeparatingDomain().getSize()]);
			for (int source_data_offset = 0; source_data_offset < s.getDataDomain().getCombinations(); ++source_data_offset) {
				int target_data_offset = data_iter.next();

				Iterator<LinearConstraint> constraints_iter = constraints.iterator();
				for (int constraint = 0; constraint < constraints.size(); ++constraint) {
					RealVector vector = params.get(constraint);
					RealVector source_vector = constraints_iter.next().getCoefficients();
					vector.setEntry(target_data_offset + target_offset, source_vector.getEntry(source_data_offset));
				}
			}

			Iterator<LinearConstraint> constraints_iter = constraints.iterator();
			for (int i = 0; i < constraints.size(); ++i) {
				LinearConstraint source = constraints_iter.next();
				LinearConstraint target = new LinearConstraint(params.get(i), source.getRelationship(), source.getValue());
				data.add(target);
			}
		}

		return new ExtensiveHalfspaceFactor(combined, data);
	}

	@Override
	public Class<ExtensiveHalfspaceFactor> getTargetClass() {
		return ExtensiveHalfspaceFactor.class;
	}

	@Override
	public Class<SeparateLinearFactor> getSourceClass() {
		return SeparateLinearFactor.class;
	}

	
}
