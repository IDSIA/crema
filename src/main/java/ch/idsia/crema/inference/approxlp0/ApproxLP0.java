package ch.idsia.crema.inference.approxlp0;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.ExtensiveLinearToRandomBayesian;
import ch.idsia.crema.factor.convert.HalfspaceToRandomBayesianFactor;
import ch.idsia.crema.factor.convert.SeparateLinearToRandomBayesian;
import ch.idsia.crema.factor.convert.VertexToRandomBayesian;
import ch.idsia.crema.factor.credal.linear.extensive.ExtensiveLinearFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalDefaultFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateLinearFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.sampling.LikelihoodWeightingSampling;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    17.08.2021 10:56
 * <p>
 * This basic version
 * <p>
 * Primitive version of the ApproxLP algorithm. This algorithm generates a certain number of Bayesian networks from the
 * input credal network and then run a bayesian inference engine on them to produce the output.
 */
public class ApproxLP0<F extends GenericFactor> implements Inference<GraphicalModel<F>, IntervalFactor> {

	/**
	 * Flag, if true applies pre-processing before each inference.
	 */
	private boolean preprocess = true;
	/**
	 * Number of Bayesian Network to generate.
	 */
	private int n = 100;
	/**
	 * Bayesian inference engine to use.
	 */
	private Inference<GraphicalModel<BayesianFactor>, BayesianFactor> inference = new LikelihoodWeightingSampling();

	/**
	 * Generated networks of the last inference done.
	 */
	private List<GraphicalModel<BayesianFactor>> networks = new ArrayList<>();
	/**
	 * Generated outputs of the last inference done.
	 */
	private List<BayesianFactor> outputs = new ArrayList<>();

	public ApproxLP0() {
	}

	/**
	 * @param n the number of network to generate (default: 100)
	 */
	public ApproxLP0(int n) {
		this.n = n;
	}

	/**
	 * @param preprocess if true, applies {@link RemoveBarren} to the input model before use it (default: true)
	 */
	public ApproxLP0(boolean preprocess) {
		this.preprocess = preprocess;
	}

	/**
	 * @param n          the number of network to generate (default: 100)
	 * @param preprocess if true, applies {@link RemoveBarren} to the input model before use it (default: true)
	 */
	public ApproxLP0(int n, boolean preprocess) {
		this.n = n;
		this.preprocess = preprocess;
	}

	/**
	 * @param n         the number of network to generate (default: 100)
	 * @param inference the {@link Inference} method to use
	 *                  (default: {@link LikelihoodWeightingSampling} with default parameters)
	 */
	public ApproxLP0(int n, Inference<GraphicalModel<BayesianFactor>, BayesianFactor> inference) {
		this.n = n;
		this.inference = inference;
	}

	/**
	 * @param n          the number of network to generate (default: 100)
	 * @param inference  the {@link Inference} method to use
	 *                   (default: {@link LikelihoodWeightingSampling} with default parameters)
	 * @param preprocess if true, applies {@link RemoveBarren} to the input model before use it (default: true)
	 */
	public ApproxLP0(int n, Inference<GraphicalModel<BayesianFactor>, BayesianFactor> inference, boolean preprocess) {
		this.n = n;
		this.inference = inference;
		this.preprocess = preprocess;
	}

	/**
	 * @param preprocess if true, applies {@link RemoveBarren} to the input model before use it (default: true)
	 * @return the same object
	 */
	public ApproxLP0<F> setPreprocess(boolean preprocess) {
		this.preprocess = preprocess;
		return this;
	}

	/**
	 * @param inference the {@link Inference} method to use
	 *                  (default: {@link LikelihoodWeightingSampling} with default parameters)
	 * @return the same object
	 */
	public ApproxLP0<F> setInference(Inference<GraphicalModel<BayesianFactor>, BayesianFactor> inference) {
		this.inference = inference;
		return this;
	}

	/**
	 * @param n the number of network to generate (default: 100)
	 * @return the same object
	 */
	public ApproxLP0<F> setN(int n) {
		this.n = n;
		return this;
	}

	/**
	 * @return a list of all the generated Bayesian networks for the last query done.
	 */
	public List<GraphicalModel<BayesianFactor>> getNetworks() {
		return networks;
	}

	/**
	 * @return a list of all the sampled factors for the last query done.
	 */
	public List<BayesianFactor> getOutputs() {
		return outputs;
	}

	/**
	 * This method generates a certain amount of networks, specified by the {@code n} parameter, from the given
	 * {@code model} and then use the chosen {@code inference} engine.
	 *
	 * @param originalModel the model to use for inference
	 * @param evidence      the observed variable as a map of variable-states
	 * @param query         the variable that will be queried
	 * @return an {@link IntervalFactor} of the query node
	 */
	@Override
	public IntervalFactor query(GraphicalModel<F> originalModel, TIntIntMap evidence, int query) {
		final GraphicalModel<F> model;
		if (preprocess) {
			RemoveBarren<F> remove = new RemoveBarren<>();
			model = remove.execute(originalModel, evidence, query);
		} else {
			model = originalModel;
		}

		networks = IntStream.range(0, n)
				.mapToObj(i -> randomBayesianNetwork(model))
				.collect(Collectors.toList());

		outputs = networks.stream()
				.map(net -> inference.query(net, evidence, query))
				.collect(Collectors.toList());

		int states = model.getSize(query);

		double[] lowers = new double[states];
		double[] uppers = new double[states];

		Arrays.fill(lowers, 1.0);
		Arrays.fill(uppers, 0.0);

		for (int state = 0; state < states; state++) {
			for (BayesianFactor output : outputs) {
				final double value = output.getValue(state);
				lowers[state] = Math.min(value, lowers[state]);
				uppers[state] = Math.max(value, uppers[state]);
			}
		}

		return new IntervalDefaultFactor(
				model.getDomain(query),
				model.getDomain(),
				new double[][]{lowers},
				new double[][]{uppers}
		)
				.updateReachability();
	}

	/**
	 * @param model the model to use for inference
	 * @return a {@link DAGModel<BayesianFactor>} object with the same network of the input model but {@link BayesianFactor}s
	 */
	private GraphicalModel<BayesianFactor> randomBayesianNetwork(GraphicalModel<F> model) {
		final GraphicalModel<BayesianFactor> network = new DAGModel<>();

		// add nodes
		for (int var : model.getVariables()) {
			network.addVariable(var, model.getSize(var));
		}

		// add parents
		for (int var : model.getVariables()) {
			network.addParents(var, model.getParents(var));
		}

		// add factors
		for (int var : model.getVariables()) {
			if (model.getFactorsMap().containsKey(var)) {
				BayesianFactor r = randomBayesianFactor(model.getFactor(var), var);
				network.setFactor(var, r);
			}
		}

		return network;
	}

	/**
	 * @param factor input credal factor
	 * @return a {@link BayesianFactor}, randomly sampled from the given credal factor, or {@code null} if another not
	 * supported {@link GenericFactor}
	 */
	private BayesianFactor randomBayesianFactor(GenericFactor factor, int var) {
		if (factor instanceof ExtensiveLinearFactor) {
			return new ExtensiveLinearToRandomBayesian().apply((ExtensiveLinearFactor<?>) factor);
		} else if (factor instanceof SeparateHalfspaceFactor) {
			return new HalfspaceToRandomBayesianFactor().apply((SeparateHalfspaceFactor) factor, var);
		} else if (factor instanceof SeparateLinearFactor) {
			return new SeparateLinearToRandomBayesian().apply((SeparateLinearFactor<?>) factor, var);
		} else if (factor instanceof VertexFactor) {
			return new VertexToRandomBayesian().apply((VertexFactor) factor, var);
		} else if (factor instanceof BayesianFactor) {
			return (BayesianFactor) factor;
		}
		return null;
	}

}
