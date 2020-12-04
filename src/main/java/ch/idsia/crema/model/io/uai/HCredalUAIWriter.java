package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.ConstraintsUtil;
import ch.idsia.crema.utility.IndexIterator;
import org.apache.commons.math3.optim.linear.LinearConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class HCredalUAIWriter extends NetUAIWriter<DAGModel<? extends Factor<?>>> {

	public HCredalUAIWriter(DAGModel<? extends Factor<?>> target, String filename) {
		super(target, filename);
		TYPE = UAITypes.HCREDAL;

	}

	@Override
	protected void sanityChecks() {
		super.sanityChecks();
		if (!isCompatible(target))
			throw new IllegalArgumentException("Target is not compatible with writer");
	}

	@Override
	protected void writeFactors() {
		for (int v : target.getVariables()) {
			SeparateHalfspaceFactor f = (SeparateHalfspaceFactor) target.getFactor(v);

			append("");
			// get a reordered iterator as UAI stores data with inverted variables compared to Crema
			Strides paDomain = target.getDomain(target.getParents(v)).reverseDomain();
			IndexIterator iter = paDomain.getReorderedIterator(target.getParents(v));

			Collection<LinearConstraint> K = new ArrayList<>();

			int paComb = paDomain.getCombinations();
			int vSize = target.getSize(v);
			int offset = 0;

			// transform constraints
			int j;
			while (iter.hasNext()) {
				j = iter.next();
				Collection<LinearConstraint> Kj = HCredalUAIWriter.processConstraints(f.getLinearProblemAt(j).getConstraints());
				Kj = ConstraintsUtil.expandCoeff(Kj, paComb * vSize, offset);
				K.addAll(Kj);
				offset += vSize;
			}

			// write coefficients
			append(paComb * vSize * K.size());
			for (LinearConstraint c : K)
				append(ArraysUtil.replace(c.getCoefficients().toArray(), -0.0, 0.0));

			// write values
			append(K.size());
			append(K.stream()
					.map(c -> ArraysUtil.replace(new double[]{c.getValue()}, -0.0, 0.0))
					.map(this::str)
					.collect(Collectors.joining(" "))
			);
		}

	}

	@Override
	protected void writeTarget() {
		writeType();
		writeVariablesInfo();
		writeDomains();
		writeFactors();
	}

	public static Collection<LinearConstraint> processConstraints(Collection<LinearConstraint> set) {
		return ConstraintsUtil.changeGEQtoLEQ(
				ConstraintsUtil.changeEQtoLEQ(
						ConstraintsUtil.removeNormalization(
								ConstraintsUtil.removeNonNegative(set))));
	}

	protected static boolean isCompatible(Object object) {
		if (!(object instanceof DAGModel))
			return false;

		for (int v : ((DAGModel<?>) object).getVariables())
			if (!(((DAGModel<?>) object).getFactor(v) instanceof SeparateHalfspaceFactor))
				return false;
		return true;
	}

}
