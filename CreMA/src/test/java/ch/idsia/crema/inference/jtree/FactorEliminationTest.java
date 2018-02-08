package ch.idsia.crema.inference.jtree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.jtree.tree.EliminationTree;
import ch.idsia.crema.model.graphical.SparseModel;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    08.02.2018 11:28
 */
public class FactorEliminationTest {

	int A, B, C, D, E, F, G, H, I, J, K;
	private EliminationTree T;

	@Before
	public void setUp() {
		// This model is based on "Modeling and Reasoning with BN", Dawiche, p.155
		SparseModel<BayesianFactor> model = new SparseModel<>();

		// Winter?
		A = model.addVariable(2);
		B = model.addVariable(2);
		C = model.addVariable(2);
		model.addParent(C, A);
		D = model.addVariable(2);
		model.addParent(D, A);
		model.addParent(D, B);
		E = model.addVariable(2);
		F = model.addVariable(2);
		model.addParent(F, C);
		model.addParent(F, D);
		G = model.addVariable(2);
		model.addParent(G, E);
		H = model.addVariable(2);
		I = model.addVariable(2);
		model.addParent(I, C);
		J = model.addVariable(2);
		model.addParent(J, F);
		model.addParent(J, G);
		K = model.addVariable(2);
		model.addParent(K, G);
		model.addParent(K, H);

		BayesianFactor[] f = new BayesianFactor[11];

		f[A] = new BayesianFactor(model.getDomain(A), new double[]{.6, .4}, false);
		f[B] = new BayesianFactor(model.getDomain(B), new double[]{.6, .4}, false);
		f[C] = new BayesianFactor(model.getDomain(A, C), false);
		f[C].setData(new int[]{C, A}, new double[]{.1, .9, .2, .8});
		f[D] = new BayesianFactor(model.getDomain(A, B, D), false);
		f[D].setData(new int[]{D, A, B}, new double[]{.3, .7, .4, .6, .5, .5, .8, .2});
		f[E] = new BayesianFactor(model.getDomain(E), new double[]{.6, .4}, false);
		f[F] = new BayesianFactor(model.getDomain(C, D, F), false);
		f[F].setData(new int[]{F, C, D}, new double[]{.5, .5, .7, .3, .2, .8, .6, .4});
		f[G] = new BayesianFactor(model.getDomain(E, G), false);
		f[G].setData(new int[]{G, E}, new double[]{.6, .4, .5, .5});
		f[H] = new BayesianFactor(model.getDomain(H), new double[]{.6, .4}, false);
		f[I] = new BayesianFactor(model.getDomain(C, I), false);
		f[I].setData(new int[]{I, C}, new double[]{.1, .9, .8, .2});
		f[J] = new BayesianFactor(model.getDomain(F, G, J), false);
		f[J].setData(new int[]{J, F, G}, new double[]{.2, .8, .3, .7, .6, .4, .5, .5});
		f[K] = new BayesianFactor(model.getDomain(G, H, K), false);
		f[K].setData(new int[]{K, G, H}, new double[]{.4, .6, .8, .2, .5, .5, .7, .3});

		model.setFactors(f);

		// tree
		T = new EliminationTree();
		T.addNode(A, f[A]);
		T.addNode(B, f[B]);
		T.addNode(C, f[C]);
		T.addNode(D, f[D]);
		T.addNode(E, f[E]);
		T.addNode(F, f[F]);
		T.addNode(G, f[G]);
		T.addNode(H, f[H]);
		T.addNode(I, f[I]);
		T.addNode(J, f[J]);
		T.addNode(K, f[K]);

		T.addEdge(A, D);
		T.addEdge(B, D);
		T.addEdge(D, G);
		T.addEdge(E, G);
		T.addEdge(C, F);
		T.addEdge(I, F);
		T.addEdge(F, J);
		T.addEdge(J, G);
		T.addEdge(H, K);
		T.addEdge(K, G);
	}

	@Test
	public void testMessagePassing() {
		FactorElimination fe = new FactorElimination();
		fe.setEvidence(new TIntIntHashMap());
		fe.setTree(T);
		fe.setRoot(G);
		fe.FE(G);
	}
}
