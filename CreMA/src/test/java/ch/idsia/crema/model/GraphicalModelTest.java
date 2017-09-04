package ch.idsia.crema.model;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GraphicalModelTest {

	@Test
	public void testAddStateNullChange() {
		SparseModel<BayesianFactor> model = new SparseModel<BayesianFactor>();
		model.addVariable(2);
		model.addVariable(2);
		model.addParent(1, 0);
		
		Strides strides = model.getDomain(0, 1);
		BayesianFactor f = new BayesianFactor(strides, new double[strides.getCombinations()], false);
		model.setFactor(1, f);
		
		// we have the null change manager installed by default
		model.addState(0);
		assertEquals(null, model.getFactor(1));
	}

	@Test
	public void testAlessandro() {
		
		SparseModel<VertexFactor> model = new SparseModel<VertexFactor>();
		int v1 = model.addVariable(3);
		int v2 = model.addVariable(3);
		int v3 = model.addVariable(3);
		
		/*
		Strides domain = model.getDomain(v2);
		VertexFactor f2 = new VertexFactor(domain, model.getDomain());
		f2.addVertex(new double[] { 0.2, 0.3, 0.5 });
		f2.addVertex(new double[] { 0.2, 0.1, 0.7 });
		f2.addVertex(new double[] { 0.3, 0.3, 0.4 });
		
		domain = model.getDomain(v3);
		VertexFactor f3 = new VertexFactor(domain, model.getDomain());
		f3.addVertex(new double[] { 0.2, 0.3, 0.5 });
		f3.addVertex(new double[] { 0.2, 0.1, 0.7 });
		f3.addVertex(new double[] { 0.3, 0.3, 0.4 });
		
		VertexFactor f1 = new VertexFactor(model.getDomain(v1), model.getDomain(v2, v3));
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },0,0);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },0,0);
		f1.addVertex(new double[] { 0.3, 0.3, 0.4 },0,0);
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },1,0);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },1,0);
		f1.addVertex(new double[] { 0.3, 0.3, 0.4 },1,0);
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },0,1);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },0,1);
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },1,1);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },1,1);
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },1,2);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },1,2);
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },0,2);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },0,2);
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },2,1);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },2,1);
		f1.addVertex(new double[] { 0.2, 0.3, 0.5 },2,0);
		f1.addVertex(new double[] { 0.2, 0.1, 0.7 },2,0);
		*/
		//model.setFactor(v1, f1);
		//model.setFactor(v2, f2);
		//model.setFactor(v3, f3);

		//ch.idsia.crema.factor.credal.vertex.algebra.DefaultAlgebra a = new ch.idsia.crema.factor.credal.vertex.algebra.DefaultAlgebra();
	
		// we have the null change manager installed by default
		//assertEquals(null, model.getFactor(1));
	}

	
	@Test
	public void testRemoveState() {
		SparseModel<BayesianFactor> model = new SparseModel<BayesianFactor>();

		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);

		model.addParent(2, 0); // A3 <-- A1
		model.addParent(2, 1); // A3 <-- A2
		model.addParent(3, 1);
		model.addParent(0, 1);

		Strides d = model.getDomain(1, 3);

		BayesianFactor factor = new BayesianFactor(d, true);
		model.setFactor(3, factor);
		model.removeVariable(0);

		// in this model factors are updated inline!!!
		assertArrayEquals(new int[] { 1, 3 }, factor.getDomain().getVariables());

		// since 0 was deleted the variable of the factor is 2
		assertArrayEquals(new int[] { 1, 3 }, model.getFactor(3).getDomain().getVariables());

	}

	@Test
	public void testRemoveVariable() {
		SparseModel<BayesianFactor> model = new SparseModel<BayesianFactor>();
		
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);

		model.addParent(2, 0); // A3 <-- A1
		model.addParent(2, 1); // A3 <-- A2
		model.addParent(3, 1);
		model.addParent(0, 1);

		Strides d = model.getDomain(1, 3);

		BayesianFactor factor = new BayesianFactor(d, true);
		model.setFactor(3, factor);
		model.removeVariable(0);

		// in this model factors are updated inline!!!
		assertArrayEquals(new int[] { 1, 3 }, factor.getDomain().getVariables());
		assertArrayEquals(new int[] { 1, 3 }, model.getFactor(3).getDomain().getVariables());
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
