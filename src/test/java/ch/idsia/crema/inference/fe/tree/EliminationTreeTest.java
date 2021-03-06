package ch.idsia.crema.inference.fe.tree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.fe.EliminationTree;
import ch.idsia.crema.inference.fe.FactorElimination2;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.utility.ArraysUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:47
 */
public class EliminationTreeTest {

	private EliminationTree T;
	private int A, B, C, D, E;

	@BeforeEach
	public void setUp() {
		// This model is based on "Modeling and Reasoning with BN", Dawiche, p.155
		DAGModel<BayesianFactor> model = new DAGModel<>();

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

		f[A] = BayesianFactorFactory.factory().domain(model.getDomain(A))
				.data(new double[]{.6, .4})
				.get();

		f[B] = BayesianFactorFactory.factory().domain(model.getDomain(A, B))
				.data(new int[]{B, A}, new double[]{.2, .8, .75, .25})
				.get();

		f[C] = BayesianFactorFactory.factory().domain(model.getDomain(A, C))
				.data(new int[]{C, A}, new double[]{.8, .2, .1, .9})
				.get();

		f[D] = BayesianFactorFactory.factory().domain(model.getDomain(B, C, D))
				.data(new int[]{D, B, C}, new double[]{.95, .05, .9, .1, .8, .2, 0, 1})
				.get();

		f[E] = BayesianFactorFactory.factory().domain(model.getDomain(C, E))
				.data(new int[]{E, C}, new double[]{.7, .3, 0, 1})
				.get();

		model.setFactors(f);

		// tree
		T = new EliminationTree();
		T.addNode(A, f[A]);
		T.addNode(B, f[B]);
		T.addNode(C, f[C]);
		T.addNode(D, f[D]);
		T.addNode(E, f[E]);
	}

	private void buildEliminationTreeA(EliminationTree T) {
		T.addEdge(B, A); // 2-1
		T.addEdge(A, D); // 1-4
		T.addEdge(D, C); // 4-3
		T.addEdge(C, E); // 3-5
	}

	private void buildEliminationTreeB(EliminationTree T) {
		T.addEdge(B, A); // 2-1
		T.addEdge(A, C); // 1-3
		T.addEdge(C, D); // 3-4
		T.addEdge(C, E); // 3-5
	}

	private BayesianFactor factorElimination(EliminationTree TA, int Q) {
		// TODO: see TODO in FactorElimination2
		FactorElimination2 fe2A = new FactorElimination2();
		fe2A.setEliminationTree(TA, Q);
		return fe2A.FE2(Q);
	}

	@Test
	public void testFE2() {
		EliminationTree TA = T.copy();
		EliminationTree TB = T.copy();

		buildEliminationTreeA(TA);
		buildEliminationTreeB(TB);

		BayesianFactor fA = factorElimination(TA, C);
		BayesianFactor fB = factorElimination(TB, C);

		System.out.println(fA + " " + Arrays.toString(fA.getData()));
		System.out.println(fB + " " + Arrays.toString(fB.getData()));

		assert (ArraysUtil.almostEquals(fA.getData(), fB.getData(), 0.00000001));
	}

	@Test
	public void testSeparator() {
		buildEliminationTreeA(T);

		int[] S14 = T.separator(A, D);

		System.out.println(Arrays.toString(S14));

		assertEquals(S14.length, 2);
		assertTrue(ArraysUtil.contains(A, S14));
		assertTrue(ArraysUtil.contains(B, S14));
	}

	@Test
	public void testClusterA() {
		buildEliminationTreeA(T);

		assertArrayEquals(T.cluster(A), new int[]{A, B});
		assertArrayEquals(T.cluster(B), new int[]{A, B});
		assertArrayEquals(T.cluster(C), new int[]{A, C});
		assertArrayEquals(T.cluster(D), new int[]{A, B, C, D});
		assertArrayEquals(T.cluster(E), new int[]{C, E,});
	}

	@Test
	public void testClusterB() {
		buildEliminationTreeB(T);

		assertArrayEquals(T.cluster(A), new int[]{A, B});
		assertArrayEquals(T.cluster(B), new int[]{A, B});
		assertArrayEquals(T.cluster(C), new int[]{A, B, C});
		assertArrayEquals(T.cluster(D), new int[]{B, C, D});
		assertArrayEquals(T.cluster(E), new int[]{C, E,});
	}
}