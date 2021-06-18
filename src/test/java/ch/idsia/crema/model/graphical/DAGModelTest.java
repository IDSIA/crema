package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.12.2020 15:30
 */
public class DAGModelTest {

	@Test
	public void testCopy() {
		DAGModel<BayesianFactor> real = new DAGModel<>();
		BayesianFactor[] f = new BayesianFactor[5];

		// Winter?
		int A = real.addVariable(2);
		f[A] = BayesianFactorFactory.factory().domain(real.getDomain(A))
				.data(new double[]{.6, .4})
				.get();

		// Sprinkler?
		int B = real.addVariable(2);
		real.addParent(B, A);
		f[B] = BayesianFactorFactory.factory().domain(real.getDomain(A, B))
				.data(new int[]{B, A}, new double[]{.2, .8, .75, .25})
				.get();

		// Rain?
		int C = real.addVariable(2);
		real.addParent(C, A);
		f[C] = BayesianFactorFactory.factory().domain(real.getDomain(A, C))
				.data(new int[]{C, A}, new double[]{.8, .2, .1, .9})
				.get();

		// Wet Grass?
		int D = real.addVariable(2);
		real.addParent(D, B);
		real.addParent(D, C);
		f[D] = BayesianFactorFactory.factory().domain(real.getDomain(B, C, D))
				.data(new int[]{D, B, C}, new double[]{.95, .05, .9, .1, .8, .2, 0, 1})
				.get();

		// Slippery Road?
		int E = real.addVariable(2);
		real.addParent(E, C);
		f[E] = BayesianFactorFactory.factory().domain(real.getDomain(C, E))
				.data(new int[]{E, C}, new double[]{.7, .3, 0, 1})
				.get();

		real.setFactors(f);

		DAGModel<BayesianFactor> copy = real.copy();

		assertEquals(real.max, copy.max);
		assertEquals(real.getVariablesCount(), copy.getVariablesCount());

		for (int v : real.getVariables()) {
			assertArrayEquals(real.getVariableAndParents(v), copy.getVariableAndParents(v));
			assertEquals(real.getFactor(v), copy.getFactor(v));
		}

		// network correctness
		DirectedAcyclicGraph<Integer, DefaultEdge> realNetwork = real.getNetwork();
		DirectedAcyclicGraph<Integer, DefaultEdge> copyNetwork = copy.getNetwork();

		for (Integer v : realNetwork.vertexSet()) {
			assertTrue(copyNetwork.containsVertex(v));
		}

		for (DefaultEdge e : realNetwork.edgeSet()) {
			Integer s = realNetwork.getEdgeSource(e);
			Integer t = realNetwork.getEdgeTarget(e);
			assertTrue(copyNetwork.containsEdge(s, t));
		}
	}

}