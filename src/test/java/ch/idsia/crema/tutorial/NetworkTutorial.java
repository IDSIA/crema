package ch.idsia.crema.tutorial;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianDefaultFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class NetworkTutorial {

	@Test
	public void createSparseNetwork() {
		// [creating-sparse-model]
		DAGModel<BayesianFactor> model = new DAGModel<>();

		model.addVariable(2); // C
		model.addVariable(3); // A
		model.addVariable(2); // B
		// [creating-sparse-model]

		// [creating-sparse-model-arcs]
		// add variable 1 and 2 as parents of variable 0
		model.addParents(0, 1, 2);
		// [creating-sparse-model-arcs]
		assertArrayEquals(new int[]{0}, model.getChildren(1));
		assertArrayEquals(new int[]{0}, model.getChildren(2));

		// [creating-sparse-model-remove]
		model.removeVariable(1);
		// [creating-sparse-model-remove]
		assertArrayEquals(new int[]{2}, model.getParents(0));
	}

	@Test
	public void createSparseDAG() {
		// [creating-sparse-dag-model]
		DAGModel<GenericFactor> model = new DAGModel<>();

		model.addVariable(2); // C
		model.addVariable(3); // A
		model.addVariable(2); // B
		// [creating-sparse-dag-model]


		// [creating-sparse-model-arcs]
		// add variable 1 and 2 as parents of variable 0
		model.addParents(0, 1, 2);
		// [creating-sparse-model-arcs]
		assertArrayEquals(new int[]{0}, model.getChildren(1));
		assertArrayEquals(new int[]{0}, model.getChildren(2));


		// [creating-sparse-model-remove]
		model.removeVariable(1);
		// [creating-sparse-model-remove]
		assertArrayEquals(new int[]{2}, model.getParents(0));
	}


	@Test
	public void createSymbolicNetwork() {
		DAGModel<SymbolicFactor> model = new DAGModel<>();
		int A = model.addVariable(2);
		int B = model.addVariable(2);
		int C = model.addVariable(2);

		model.addParent(A, C);
		model.addParent(B, C);

		BayesianFactor fac = new BayesianDefaultFactor(model.getDomain(A), new double[0]);
		BayesianFactor fbc = new BayesianDefaultFactor(model.getDomain(B), new double[0]);
		BayesianFactor fc = new BayesianDefaultFactor(model.getDomain(C), new double[0]);

		// populate factors here

		PriorFactor pac = new PriorFactor(fac);
		PriorFactor pbc = new PriorFactor(fbc);
		PriorFactor pc = new PriorFactor(fc);

		model.setFactor(A, pac);
		model.setFactor(B, pbc);
		model.setFactor(C, pc);
	}
}
