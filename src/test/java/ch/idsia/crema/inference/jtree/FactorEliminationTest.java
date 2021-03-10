package ch.idsia.crema.inference.jtree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.fe.EliminationTree;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    08.02.2018 11:28
 */
public class FactorEliminationTest {

	private EliminationTree T;

	TIntIntHashMap evidence;
	int Q;

	@Before
	public void setUp() {
		BayesianNetworkContainer bn = BayesianNetworkContainer.mix5Variables();
		BayesianFactor[] f = bn.factors;

		int A = bn.variables[0];
		int B = bn.variables[1];
		int C = bn.variables[2];
		int D = bn.variables[3];
		int E = bn.variables[4];

		Q = C;

		evidence = new TIntIntHashMap(new int[]{B, E}, new int[]{1, 0});

		// tree
		T = new EliminationTree();
		T.addNode(A, f[A]);
		T.addNode(B, f[B]);
		T.addNode(C, f[C]);
		T.addNode(D, f[D]);
		T.addNode(E, f[E]);

		T.addEdge(A, C);
		T.addEdge(B, D);
		T.addEdge(D, C);
		T.addEdge(E, C);
	}

//	@Before
//	public void setUp() {
//		BayesianNetworks bn = BayesianNetworks.binary11Variables();
//		BayesianFactor[] f = bn.factors;
//
//		int A = bn.variables[0];
//		int B = bn.variables[1];
//		int C = bn.variables[2];
//		int D = bn.variables[3];
//		int E = bn.variables[4];
//		int F = bn.variables[5];
//		int G = bn.variables[6];
//		int H = bn.variables[7];
//		int I = bn.variables[8];
//		int J = bn.variables[9];
//		int K = bn.variables[10];
//
//		Q = G;
//		observations = new int[]{C};
//
//		 tree
//		T = new EliminationTree();
//		T.addNode(A, f[A]);
//		T.addNode(B, f[B]);
//		T.addNode(C, f[C]);
//		T.addNode(D, f[D]);
//		T.addNode(E, f[E]);
//		T.addNode(F, f[F]);
//		T.addNode(G, f[G]);
//		T.addNode(H, f[H]);
//		T.addNode(I, f[I]);
//		T.addNode(J, f[J]);
//		T.addNode(K, f[K]);
//
//		T.addEdge(A, D);
//		T.addEdge(B, D);
//		T.addEdge(D, G);
//		T.addEdge(E, G);
//		T.addEdge(C, F);
//		T.addEdge(I, F);
//		T.addEdge(F, J);
//		T.addEdge(J, G);
//		T.addEdge(H, K);
//		T.addEdge(K, G);
//	}

	@Ignore // TODO: see TODO in FactorElimination class
	@Test
	public void testMessagePassing() {
//		FactorElimination fe = new FactorElimination();
//		fe.setEvidence(evidence);
//		fe.setTree(T);
//		fe.setRoot(Q);
//		fe.query(Q);
	}
}
