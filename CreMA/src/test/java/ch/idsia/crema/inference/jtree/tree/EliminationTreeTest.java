package ch.idsia.crema.inference.jtree.tree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.FactorElimination2;
import ch.idsia.crema.model.graphical.SparseModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:47
 */
public class EliminationTreeTest {

	private EliminationTree T;
	private int A, B, C, D, E;

	@Before
	public void setUp() {
		// This model is based on "Modeling and Reasoning with BN", Dawiche, p.155
		SparseModel<BayesianFactor> model = new SparseModel<>();

		// Winter?
		A = model.addVariable(2);

		// Sprinkler?
		B = model.addVariable(2);
		model.addParent(B, A);

		// Rain?
		C = model.addVariable(2);
		model.addParent(C, A);

		// Wet Grass?
		D = model.addVariable(2);
		model.addParent(D, B);
		model.addParent(D, C);

		// Slippery Road?
		E = model.addVariable(2);
		model.addParent(E, C);

		BayesianFactor[] f = new BayesianFactor[5];

		f[A] = new BayesianFactor(model.getDomain(A), new double[]{.6, .4}, false);

		f[B] = new BayesianFactor(model.getDomain(A, B), false);
		f[B].setData(new int[]{B, A}, new double[]{.2, .8, .75, .25});

		f[C] = new BayesianFactor(model.getDomain(A, C), false);
		f[C].setData(new int[]{C, A}, new double[]{.8, .2, .1, .9});

		f[D] = new BayesianFactor(model.getDomain(B, C, D), false);
		f[D].setData(new int[]{D, B, C}, new double[]{.95, .05, .9, .1, .8, .2, 0, 1});

		f[E] = new BayesianFactor(model.getDomain(C, E), false);
		f[E].setData(new int[]{E, C}, new double[]{.7, .3, 0, 1});

		model.setFactors(f);

		// tree
		T = new EliminationTree();
		T.addNode(A, f[A]);
		T.addNode(B, f[B]);
		T.addNode(C, f[C]);
		T.addNode(D, f[D]);
		T.addNode(E, f[E]);
	}

	@Test
	public void buildEliminationTreeA() {
		T.addEdge(B, A); // 2-1
		T.addEdge(A, D); // 1-4
		T.addEdge(D, C); // 4-3
		T.addEdge(C, E); // 3-5
	}

	@Test
	public void buildEliminationTreeB() {
		T.addEdge(B, A); // 2-1
		T.addEdge(A, C); // 1-3
		T.addEdge(C, D); // 3-4
		T.addEdge(C, E); // 3-5
	}

	@After
	public void tearDown() {
		FactorElimination2 fe2 = new FactorElimination2();
		fe2.setEliminationTree(T, C);
		BayesianFactor f = fe2.FE2(C);

		System.out.println(f + " " + Arrays.toString(f.getData()));
	}
}