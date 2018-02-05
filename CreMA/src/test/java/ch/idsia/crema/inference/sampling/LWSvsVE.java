package ch.idsia.crema.inference.sampling;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.SparseModel;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    05.02.2018 15:44
 */
public class LWSvsVE {

	private SparseModel<BayesianFactor> model;

	private Random random = new Random(42);

	private static final int e = 5;
	private static final int m = 1000;
	private static final int n = 10;
	private static final int p = 3;

	@Before
	public void setUp() {
		model = new SparseModel<>();
		BayesianFactor[] f = new BayesianFactor[n];

		double d = random.nextDouble();

		int root = model.addVariable(2);
		f[root] = new BayesianFactor(model.getDomain(root), new double[]{d, 1 - d}, false);

		System.out.println(root + " " + f[root] + ": " + Arrays.toString(f[root].getData()));

		for (int i = 1; i < n; i++) {
			int v = model.addVariable(2);

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

			for (; doubles.size() < Math.pow(2, ints.size()); ) {
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
	}

	@Test
	public void vsVariableElimination() throws InterruptedException {

		// TODO: this test has an issue with variable elimination and empty nodes

		MinFillOrdering ordering = new MinFillOrdering();
		int[] seq = ordering.apply(model);

		double avg = 0.0;

		for (int q = 0; q < m; q++) {
			TIntIntMap evidence = new TIntIntHashMap(new int[]{3}, new int[]{0});

			int query = random.nextInt(n);

			for (int i = 0; i < e; i++) {
				int k = random.nextInt(n);
				if (k != query) {
					int v = random.nextInt(2);
					evidence.put(k, v);
				}
			}

			LikelihoodWeightingSampling lws = new LikelihoodWeightingSampling();
			Collection<BayesianFactor> collLWS = lws.apply(model, query, evidence);

			BayesianFactor resLWS = new ArrayList<>(collLWS).get(0);

			VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(seq);
			BayesianFactor resVE = ve.apply(model, query, evidence);

			double distance = resLWS.getData()[0] - resVE.getData()[0];

			avg += distance;

			System.out.println("LWS: " + resLWS + " " + Arrays.toString(resLWS.getData()));
			System.out.println("VE:  " + resVE + " " + Arrays.toString(resVE.getData()));
			System.out.println(distance + " ");
		}

		avg /= m;

		System.out.println("Average distance; " + avg);
	}

}
