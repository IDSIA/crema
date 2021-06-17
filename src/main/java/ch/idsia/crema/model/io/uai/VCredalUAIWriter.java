package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.IndexIterator;
import org.apache.commons.math3.optim.linear.LinearConstraint;

import java.util.ArrayList;
import java.util.Collection;

public class VCredalUAIWriter extends NetUAIWriter<DAGModel<VertexFactor>> {

	public VCredalUAIWriter(DAGModel<VertexFactor> target, String filename) {
		super(target, filename);
		TYPE = UAITypes.VCREDAL;
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
			VertexFactor f = target.getFactor(v);

			append("");
			// get a reordered iterator as UAI stores data with inverted variables compared to Crema
			Strides paDomain = target.getDomain(target.getParents(v)).reverseDomain();
			IndexIterator iter = paDomain.getReorderedIterator(target.getParents(v));

			Collection<LinearConstraint> K = new ArrayList<>();

			int paComb = paDomain.getCombinations();
			int vSize = target.getSize(v);

			// transform constraints
			while (iter.hasNext()) {
				int j = iter.next();
				double[][] vertex = f.getVerticesAt(j);
				append(vertex.length * vSize);
				for (double[] doubles : vertex)
					append(doubles);
			}
		}
	}

	@Override
	protected void writeTarget() {
		writeType();
		writeVariablesInfo();
		writeDomains();
		writeFactors();
	}

	protected static boolean isCompatible(Object object) {
		if (!(object instanceof DAGModel))
			return false;

		for (int v : ((DAGModel<?>) object).getVariables())
			if (!(((DAGModel<?>) object).getFactor(v) instanceof VertexFactor))
				return false;
		return true;
	}

}
