package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    20.04.2021 11:19
 */
public class ExtensiveVertexFactorFactory {

	private final List<double[]> vertices = new ArrayList<>();
	private final List<BayesianDefaultFactor> factors = new ArrayList<>();
	private final List<BayesianLogFactor> logFactors = new ArrayList<>();

	private boolean log = false;

	private Strides domain = Strides.empty();

	private ExtensiveVertexFactorFactory() {
	}

	public static ExtensiveVertexFactorFactory factory() {
		return new ExtensiveVertexFactorFactory();
	}

	public ExtensiveVertexFactorFactory log(boolean isLog) {
		log = isLog;
		return this;
	}

	public ExtensiveVertexFactorFactory log() {
		return log(true);
	}

	public ExtensiveVertexFactorFactory domain(Strides domain) {
		this.domain = domain;
		return this;
	}

	public ExtensiveVertexFactorFactory addVertex(double[] vertex) {
		this.vertices.add(vertex);
		return this;
	}

	public ExtensiveVertexFactorFactory addVertices(List<double[]> vertices) {
		this.vertices.addAll(vertices);
		return this;
	}

	public ExtensiveVertexFactorFactory addBayesVertex(BayesianFactor factor) {
		if (factor instanceof BayesianLogFactor)
			addLogVertex((BayesianLogFactor) factor);
		else
			addBayesVertex((BayesianDefaultFactor) factor);
		return this;
	}

	public ExtensiveVertexFactorFactory addBayesVertex(BayesianDefaultFactor factor) {
		factors.add(factor);
		return this;
	}

	public ExtensiveVertexFactorFactory addBayesVertices(List<BayesianDefaultFactor> factors) {
		this.factors.addAll(factors);
		return this;
	}

	public ExtensiveVertexFactorFactory addLogVertex(BayesianLogFactor factor) {
		this.logFactors.add(factor);
		return this;
	}

	public ExtensiveVertexFactorFactory addLogVertices(List<BayesianLogFactor> factors) {
		this.logFactors.addAll(factors);
		return this;
	}

	public ExtensiveVertexFactor build() {
		final List<double[]> data = new ArrayList<>();

		if (log) {
			vertices.forEach(ArraysUtil::log);
			factors.forEach(f -> ArraysUtil.log(f.getData()));
			logFactors.forEach(f -> data.add(f.getData()));

			return new ExtensiveVertexLogFactor(domain, data, true);
		} else {
			data.addAll(vertices);
			factors.forEach(f -> data.add(f.getData()));
			logFactors.forEach(f -> ArraysUtil.exp(f.getData()));

			return new ExtensiveVertexDefaultFactor(domain, data);
		}
	}

}
