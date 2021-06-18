package ch.idsia.crema.model;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.ExtensiveVertexDefaultAlgebra;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GraphicalModelTest {

	@Test
	public void testAddStateNullChange() {
		GraphicalModel<BayesianFactor> model = new DAGModel<>();
		model.addVariable(2);
		model.addVariable(2);
		model.addParent(1, 0);

		Strides strides = model.getDomain(0, 1);
		BayesianFactor f = BayesianFactorFactory.factory().domain(strides).data().get();
		model.setFactor(1, f);

		// we have the null change manager installed by default
		model.addState(0);
		assertNull(model.getFactor(1));
	}

	@Disabled
	@Test
	public void testAlessandro() {
		DAGModel<VertexFactor> model = new DAGModel<>();
		int v1 = model.addVariable(3);
		int v2 = model.addVariable(3);
		int v3 = model.addVariable(3);

		Strides domain = model.getDomain(v2);
		VertexFactor f2 = VertexFactorFactory.factory().domain(domain, model.getDomain())
				.addVertex(new double[]{0.2, 0.3, 0.5})
				.addVertex(new double[]{0.2, 0.1, 0.7})
				.addVertex(new double[]{0.3, 0.3, 0.4})
				.get();

		domain = model.getDomain(v3);
		VertexFactor f3 = VertexFactorFactory.factory().domain(domain, model.getDomain())
				.addVertex(new double[]{0.2, 0.3, 0.5})
				.addVertex(new double[]{0.2, 0.1, 0.7})
				.addVertex(new double[]{0.3, 0.3, 0.4})
				.get();

		VertexFactor f1 = VertexFactorFactory.factory().domain(model.getDomain(v1), model.getDomain(v2, v3))
				.addVertex(new double[]{0.2, 0.3, 0.5}, 0, 0)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 0, 0)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 0, 0)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 1, 0)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 1, 0)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 1, 0)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 0, 1)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 0, 1)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 1, 1)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 1, 1)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 1, 2)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 1, 2)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 0, 2)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 0, 2)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 2, 1)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 2, 1)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 2, 0)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 2, 0)
				.get();

		model.setFactor(v1, f1);
		model.setFactor(v2, f2);
		model.setFactor(v3, f3);

		ExtensiveVertexDefaultAlgebra da = new ExtensiveVertexDefaultAlgebra();

		// we have the null change manager installed by default
		assertNull(model.getFactor(1));
	}

	@Test
	public void testRemoveState() {
		GraphicalModel<BayesianFactor> model = new DAGModel<>();

		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);

		model.addParent(2, 0); // A3 <-- A1
		model.addParent(2, 1); // A3 <-- A2
		model.addParent(3, 1);
		model.addParent(0, 1);

		Strides d = model.getDomain(1, 3);

		BayesianFactor factor = BayesianFactorFactory.factory().domain(d).data().log();
		model.setFactor(3, factor);
		model.removeVariable(0);

		// in this model factors are updated inline!!!
		assertArrayEquals(new int[]{1, 3}, factor.getDomain().getVariables());

		// since 0 was deleted the variable of the factor is 2
		assertArrayEquals(new int[]{1, 3}, model.getFactor(3).getDomain().getVariables());
	}

	@Test
	public void testRemoveVariable() {
		GraphicalModel<BayesianFactor> model = new DAGModel<>();

		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);

		model.addParent(2, 0); // A3 <-- A1
		model.addParent(2, 1); // A3 <-- A2
		model.addParent(3, 1);
		model.addParent(0, 1);

		Strides d = model.getDomain(1, 3);

		BayesianFactor factor = BayesianFactorFactory.factory().domain(d).data().log();
		model.setFactor(3, factor);
		model.removeVariable(0);

		// in this model factors are updated inline!!!
		assertArrayEquals(new int[]{1, 3}, factor.getDomain().getVariables());
		assertArrayEquals(new int[]{1, 3}, model.getFactor(3).getDomain().getVariables());
	}

	@Test
	public void testRemoveParentIntInt() {
		//fail("Not yet implemented");
	}

	@Test
	public void testAddParent() {
		//fail("Not yet implemented");
	}

}
