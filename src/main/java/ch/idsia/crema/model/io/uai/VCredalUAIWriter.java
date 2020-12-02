package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.IndexIterator;
import org.apache.commons.math3.optim.linear.LinearConstraint;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class VCredalUAIWriter extends NetUAIWriter<DAGModel> {

	public VCredalUAIWriter(DAGModel target, String file) throws IOException {
		this.target = target;
		TYPE = UAITypes.VCREDAL;
		this.writer = initWriter(file);

	}

	public VCredalUAIWriter(DAGModel target, BufferedWriter writer) {
		this.target = target;
		TYPE = UAITypes.VCREDAL;
		this.writer = writer;
	}

	@Override
	protected void sanityChecks() {
		super.sanityChecks();
		if (!isCompatible(target))
			throw new IllegalArgumentException("Target is not compatible with writer");
	}

	@Override
	protected void writeFactors() throws IOException {
		for (int v : target.getVariables()) {

			VertexFactor f = (VertexFactor) target.getFactor(v);

			tofileln("");
			// get a reordered iterator as UAI stores data with inverted variables compared to Crema
			Strides paDomain = target.getDomain(target.getParents(v)).reverseDomain();
			IndexIterator iter = paDomain.getReorderedIterator(target.getParents(v));

			Collection<LinearConstraint> K = new ArrayList<>();

			int paComb = paDomain.getCombinations();
			int vSize = target.getSize(v);

			// Transform constraints
			while (iter.hasNext()) {
				int j = iter.next();
				double[][] vertex = f.getVerticesAt(j);
				tofileln(vertex.length * vSize);
				for (int k = 0; k < vertex.length; k++)
					tofileln(vertex[k]);
			}
		}
	}

	@Override
	protected void writeTarget() throws IOException {
		writeType();
		writeVariablesInfo();
		writeDomains();
		writeFactors();
	}

	protected static boolean isCompatible(Object object) {

		if (!(object instanceof DAGModel))
			return false;

		for (int v : ((DAGModel) object).getVariables())
			if (!(((DAGModel) object).getFactor(v) instanceof VertexFactor))
				return false;
		return true;
	}

}
