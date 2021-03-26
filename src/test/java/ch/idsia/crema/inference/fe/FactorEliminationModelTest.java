package ch.idsia.crema.inference.fe;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 09:57
 */
public class FactorEliminationModelTest {

	private DAGModel<BayesianFactor> model;

	private int A, B, C;

	private final double eps = 0.000001;

	/**
	 * Example in Figure7.1 p. 154
	 */
	@BeforeEach
	public void setUp() {
		model = new DAGModel<>();
		BayesianFactor[] f = new BayesianFactor[3];

		A = model.addVariable(2);
		f[A] = new BayesianFactor(model.getDomain(A), new double[]{.6, .4}, false);

		B = model.addVariable(2);
		model.addParent(B, A);
		f[B] = new BayesianFactor(model.getDomain(A, B), false);
		f[B].setData(new int[]{B, A}, new double[]{.9, .1, .2, .8});

		C = model.addVariable(2);
		model.addParent(C, B);
		f[C] = new BayesianFactor(model.getDomain(B, C), false);
		f[C].setData(new int[]{C, B}, new double[]{.3, .7, .5, .5});

		model.setFactors(f);
	}

	@Test
	public void FE1() {
		FactorEliminationModel fe = new FactorEliminationModel();
		BayesianFactor q = fe.query(model, C);

		assertTrue(q.getData()[0] <= .376 + eps);
		assertTrue(q.getData()[1] <= .624 + eps);
	}
}