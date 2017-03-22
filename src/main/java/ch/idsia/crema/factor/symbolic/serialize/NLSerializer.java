package ch.idsia.crema.factor.symbolic.serialize;

import java.util.HashMap;

import org.apache.commons.math3.optim.linear.LinearConstraint;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.SeparateLinearFactor;
import ch.idsia.crema.factor.symbolic.CombinedFactor;
import ch.idsia.crema.factor.symbolic.FilteredFactor;
import ch.idsia.crema.factor.symbolic.MarginalizedFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.Serializer;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

public class NLSerializer implements Serializer {

	@Override
	public String serialize(SymbolicFactor factor) {
		StringBuilder builder = new StringBuilder();

		serializeAny(factor, builder);

		return builder.toString();
	}

	private void serializeAny(SymbolicFactor factor, StringBuilder builder) {
		if (factor instanceof FilteredFactor) {
			serialize((FilteredFactor) factor, builder);
		} else if (factor instanceof CombinedFactor) {
			serialize((CombinedFactor) factor, builder);
		} else if (factor instanceof MarginalizedFactor) {
			serialize((MarginalizedFactor) factor, builder);
		} else if (factor instanceof PriorFactor) {
			serialize((PriorFactor) factor, builder);
		}
	}

	protected void serialize(MarginalizedFactor factor, StringBuilder builder) {
		SymbolicFactor source = factor.getSource();

		// IndexIterator iterator =
		// source.getDomain().getFiteredIndexIterator(factor.getVariable(), 0);
		IndexIterator iterator = source.getDomain().getIterator(factor.getVariable());

		int stride = source.getDomain().getStride(factor.getVariable());
		int size = source.getDomain().getCardinality(factor.getVariable());

		for (int target_index = 0; target_index < factor.getDomain().getCombinations(); ++target_index) {
			builder.append(getName(factor, target_index));
			builder.append("=");

			int source_index = iterator.next();

			// XXX fixme (StringJoiner requires Java8, do we agree?)
			// StringJoiner sj = new StringJoiner("+");
			for (int state = 0; state < size; ++state) {
				// sj.add(getName(source, source_index + state * stride));
			}
			// builder.append(sj);
			builder.append("\n");
		}
		serializeAny(source, builder);
	}

	protected void serialize(CombinedFactor factor, StringBuilder builder) {
		SymbolicFactor[] sources = factor.getFactors();
		IndexIterator[] iterators = new IndexIterator[sources.length];

		for (int source = 0; source < sources.length; ++source) {
			iterators[source] = sources[source].getDomain().getSupersetIndexIterator(factor.getDomain());
		}

		for (int target_index = 0; target_index < factor.getDomain().getCombinations(); ++target_index) {
			builder.append(getName(factor, target_index));

			builder.append(" = ");

			// XXX same here SJ requires Java8
			// StringJoiner sj = new StringJoiner(" * ");
			// for (int source = 0; source < sources.length; ++source) {
			// sj.add(getName(sources[source], iterators[source].next()));
			// }
			// builder.append(sj);
			builder.append("\n");
		}

		for (SymbolicFactor source : sources) {
			serializeAny(source, builder);
		}
	}

	protected void serialize(FilteredFactor filtered, StringBuilder builder) {
		int offset = filtered.getSource().getDomain().getPartialOffset(new int[] { filtered.getVariable() },
				new int[] { filtered.getState() });

//IndexIterator source_iterator = filtered.getSource().getDomain().getFiteredIndexIterator(filtered.getVariable(), filtered.getState());
		IndexIterator source_iterator = filtered.getSource().getDomain().getIterator(filtered.getVariable()).offset(offset);

		for (int target_index = 0; target_index < filtered.getDomain().getCombinations(); ++target_index) {
			int source_index = source_iterator.next();
			builder.append(getName(filtered, target_index));
			builder.append("=");
			builder.append(getName(filtered.getSource(), source_index));
			builder.append('\n');
		}

		serializeAny(filtered.getSource(), builder);
	}

	protected void serialize(PriorFactor factor, StringBuilder builder) {
		GenericFactor lf1 = factor.getFactor();

		SeparateLinearFactor lf = null;
		if (lf1 instanceof SeparateLinearFactor) {
			lf = (SeparateLinearFactor) lf1;
		}

		int[] unsorted_vars = ArraysUtil.append(lf.getDataDomain().getVariables(),
				lf.getSeparatingDomain().getVariables());
		IndexIterator target_iterator = factor.getDomain().getReorderedIterator(unsorted_vars);

		for (int source_index = 0; source_index < lf.getSeparatingDomain().getCombinations(); ++source_index) {
			String[] column_target_indices = new String[lf.getDataDomain().getCombinations()];

			// collect the indices
			for (int column = 0; column < column_target_indices.length; ++column) {
				column_target_indices[column] = getName(factor, target_iterator.next());
			}

			for (LinearConstraint constraint : lf.getLinearProblemAt(source_index).getConstraints()) {
				boolean first = true;
				for (int column = 0; column < column_target_indices.length; ++column) {
					double coeff = constraint.getCoefficients().getEntry(column);
					if (coeff != 0) {
						if (!first && coeff > 0)
							builder.append(" + ");

						if (coeff == -1)
							builder.append(" - ");
						else if (coeff != 1)
							builder.append(coeff).append(" * ");
						builder.append(column_target_indices[column]);
						first = false;
					}
				}
				builder.append(' ').append(constraint.getRelationship()).append(' ');
				builder.append(constraint.getValue());
				builder.append("\n");
			}
		}

	}

	public synchronized String getName(SymbolicFactor factor, int configuration) {
		int id = 0;
		if (ids.containsKey(factor)) {
			id = ids.get(factor);
		} else {
			id = ids.size();
			ids.put(factor, id);
		}
		return "V" + id + "S" + configuration;
	}

	private HashMap<SymbolicFactor, Integer> ids = new HashMap<>();
}
