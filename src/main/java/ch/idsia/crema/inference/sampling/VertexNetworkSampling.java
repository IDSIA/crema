package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import ch.idsia.crema.model.graphical.GraphicalModel;

import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.11.2020 16:55
 * <p>
 * This class generates an array of {@link BayesianNetwork} compatible with the provided CredalNetwork.
 * </p>
 */
// TODO: this should be made factor type independent
public class VertexNetworkSampling {

	/**
	 * @param model network model to sample
	 * @return
	 */
	public BayesianNetwork sample(GraphicalModel<VertexFactor> model) {
		BayesianNetwork bnet = new BayesianNetwork();

		bnet.addVariables(model.getVariables());
		for (int v : model.getVariables()) {
			bnet.addParents(v, model.getParents(v));
			bnet.setFactor(v, model.getFactor(v).sampleVertex());
		}
		return bnet;
	}

	/**
	 * @param model network model to sample
	 * @param N     number of samples
	 * @return
	 */
	public BayesianNetwork[] sample(GraphicalModel<VertexFactor> model, int N) {
		return IntStream.range(0, N).mapToObj(i -> this.sample(model)).toArray(BayesianNetwork[]::new);
	}

}
