package ch.idsia.crema.inference.jtree;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;
import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    09.02.2018 10:43
 * <p>
 * This utility class contains a number of ready to use bayesian networks taken from books, examples or other sources.
 */
public class BayesianNetworkContainer {

	public BayesianNetwork network;
	public BayesianFactor[] factors;
	public int[] variables;

	private BayesianNetworkContainer(BayesianNetwork network, BayesianFactor[] factors, int... variables) {
		this.network = network;
		this.factors = factors;
		this.variables = variables;
	}

	/**
	 * This model is based on "Modeling and Reasoning with BN", Dawiche, p.155
	 *
	 * @return a BN
	 */
	public static BayesianNetworkContainer binary11Variables() {
		BayesianNetwork model = new BayesianNetwork();

		int A, B, C, D, E, F, G, H, I, J, K;

		// Winter?
		A = model.addVariable(2);
		B = model.addVariable(2);
		C = model.addVariable(3);
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

		f[A] = new BayesianFactor(model.getDomain(A), new double[]{.6, .4});
		f[B] = new BayesianFactor(model.getDomain(B), new double[]{.4, .6});
		f[C] = new BayesianFactor(model.getDomain(A, C));
		f[C].setData(new int[]{C, A}, new double[]{.1, .9, .2, .8});
		f[D] = new BayesianFactor(model.getDomain(A, B, D));
		f[D].setData(new int[]{D, A, B}, new double[]{.3, .7, .4, .6, .5, .5, .8, .2});
		f[E] = new BayesianFactor(model.getDomain(E), new double[]{.6, .4});
		f[F] = new BayesianFactor(model.getDomain(C, D, F));
		f[F].setData(new int[]{F, C, D}, new double[]{.5, .5, .7, .3, .2, .8, .6, .4});
		f[G] = new BayesianFactor(model.getDomain(E, G));
		f[G].setData(new int[]{G, E}, new double[]{.6, .4, .5, .5});
		f[H] = new BayesianFactor(model.getDomain(H), new double[]{.6, .4});
		f[I] = new BayesianFactor(model.getDomain(C, I));
		f[I].setData(new int[]{I, C}, new double[]{.1, .9, .8, .2});
		f[J] = new BayesianFactor(model.getDomain(F, G, J));
		f[J].setData(new int[]{J, F, G}, new double[]{.2, .8, .3, .7, .6, .4, .5, .5});
		f[K] = new BayesianFactor(model.getDomain(G, H, K));
		f[K].setData(new int[]{K, G, H}, new double[]{.4, .6, .8, .2, .5, .5, .7, .3});

		return new BayesianNetworkContainer(model, f, A, B, C, D, E, F, G, H, I, J, K);
	}

	/**
	 * This model is based on "Modeling and Reasoning with BN", Dawiche, p.378 and following
	 *
	 * @return a BN
	 */
	public static BayesianNetworkContainer mix5Variables() {
		BayesianNetwork model = new BayesianNetwork();
		BayesianFactor[] f = new BayesianFactor[5];

		// Winter?
		int A = model.addVariable(2);
		f[A] = new BayesianFactor(model.getDomain(A), new double[]{.6, .4}, false);

		// Sprinkler?
		int B = model.addVariable(2);
		model.addParent(B, A);
		f[B] = new BayesianFactor(model.getDomain(A, B), false);
		f[B].setData(new int[]{B, A}, new double[]{.2, .8, .75, .25});

		// Rain?
		int C = model.addVariable(2);
		model.addParent(C, A);
		f[C] = new BayesianFactor(model.getDomain(A, C), false);
		f[C].setData(new int[]{C, A}, new double[]{.8, .2, .1, .9});

		// Wet Grass?
		int D = model.addVariable(2);
		model.addParent(D, B);
		model.addParent(D, C);
		f[D] = new BayesianFactor(model.getDomain(B, C, D), false);
		f[D].setData(new int[]{D, B, C}, new double[]{.95, .05, .9, .1, .8, .2, 0, 1});

		// Slippery Road?
		int E = model.addVariable(2);
		model.addParent(E, C);
		f[E] = new BayesianFactor(model.getDomain(C, E), false);
		f[E].setData(new int[]{E, C}, new double[]{.7, .3, 0, 1});

		model.setFactors(f);

		return new BayesianNetworkContainer(model, f, A, B, C, D, E);
	}

	/**
	 * Generate a bayesian network using a random seed and some constraints.
	 *
	 * @param seed random seed
	 * @param n    number of variables
	 * @param p    parents for each node
	 * @return a BN
	 */
	public static BayesianNetworkContainer random(long seed, int n, int p) {
		Random random = new Random(seed);

		BayesianNetwork model = new BayesianNetwork();
		BayesianFactor[] f = new BayesianFactor[n];

		TIntList vars = new TIntArrayList();

		double d = random.nextDouble();

		int root = model.addVariable(2);
		f[root] = new BayesianFactor(model.getDomain(root), new double[]{d, 1 - d}, false);

		System.out.println(root + " " + f[root] + ": " + Arrays.toString(f[root].getData()));

		vars.add(root);

		for (int i = 1; i < n; i++) {
			int v = model.addVariable(2);
			vars.add(v);

			TDoubleList doubles = new TDoubleArrayList();
			TIntList ints = new TIntArrayList();
			ints.add(v);
			for (int j = 0, a = 0; j < i - 1 && a < p; j++) {
				if (random.nextDouble() < 0.5) {
					model.addParent(v, j);
					ints.add(j);
					a++;
				}
			}

			while (doubles.size() < Math.pow(2, ints.size())) {
				double y = random.nextDouble();
				doubles.add(y);
				doubles.add(1 - y);
			}

			int[] parents = ints.toArray();
			ints.sort();
			f[v] = new BayesianFactor(model.getDomain(ints.toArray()), false);
			f[v].setData(parents, doubles.toArray());

			System.out.println(v + " " + f[v] + ": " + Arrays.toString(f[v].getData()));
		}

		model.setFactors(f);

		return new BayesianNetworkContainer(model, f, vars.toArray());
	}

	public static BayesianNetworkContainer junctionTreeTheoryExample() {

		BayesianNetwork model = new BayesianNetwork();
		int A = model.addVariable(2); // 1
		int B = model.addVariable(2); // 2
		int C = model.addVariable(2); // 3
		model.addParent(C, A);
		model.addParent(C, B);
		int D = model.addVariable(2); // 4
		model.addParent(D, B);
		int E = model.addVariable(2); // 5
		int F = model.addVariable(2); // 6
		model.addParent(F, C);
		int G = model.addVariable(2); // 7
		model.addParent(G, D);
		int H = model.addVariable(2); // 8
		model.addParent(H, E);
		model.addParent(H, F);
		model.addParent(H, G);

		BayesianFactor[] f = new BayesianFactor[8];

		return new BayesianNetworkContainer(model, f, A, B, C, D, E, F, G, H);
	}

	/**
	 * This model is based on "Modeling and Reasoning with BN", Jensen, p.110, Fig. 4.1 "A simple Bayesian network BN".
	 *
	 * @return A simple Bayesian network BN
	 */
	public static BayesianNetworkContainer aSimpleBayesianNetwork() {

		BayesianNetwork model = new BayesianNetwork();
		int A1 = model.addVariable(2);
		int A2 = model.addVariable(2);
		int A3 = model.addVariable(2);
		int A4 = model.addVariable(2);
		int A5 = model.addVariable(2);
		int A6 = model.addVariable(2);

		model.addParent(A2, A1);
		model.addParent(A3, A1);
		model.addParent(A4, A2);
		model.addParent(A5, A2);
		model.addParent(A5, A3);
		model.addParent(A6, A3);

		BayesianFactor[] f = new BayesianFactor[6];

		f[A1] = new BayesianFactor(model.getDomain(A1));
		f[A1].setData(new int[]{A1}, new double[]{.7, .3});
		f[A2] = new BayesianFactor(model.getDomain(A1, A2));
		f[A2].setData(new int[]{A2, A1}, new double[]{.4, .6, .3, .7});
		f[A3] = new BayesianFactor(model.getDomain(A1, A3));
		f[A3].setData(new int[]{A3, A1}, new double[]{.5, .5, .8, .2});
		f[A4] = new BayesianFactor(model.getDomain(A2, A4));
		f[A4].setData(new int[]{A4, A2}, new double[]{.6, .4, .1, .9});
		f[A5] = new BayesianFactor(model.getDomain(A2, A3, A5));
		f[A5].setData(new int[]{A5, A2, A3}, new double[]{.1, .9, .8, .2, .4, .6, .6, .4});
		f[A6] = new BayesianFactor(model.getDomain(A6, A3));
		f[A6].setData(new int[]{A3, A6}, new double[]{.4, .6, .5, .5});

		model.setFactors(f);

		return new BayesianNetworkContainer(model, f, A1, A2, A3, A4, A5, A6);
	}
}
