package ch.idsia.crema.learning;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class EMBayesianTest {

	final static InferenceJoined<GraphicalModel<BayesianFactor>, BayesianFactor> engine = new InferenceJoined<>() {
		@Override
		public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int... queries) {
			final CutObserved<BayesianFactor> co = new CutObserved<>();
			final GraphicalModel<BayesianFactor> coModel = co.execute(model, evidence);

			final RemoveBarren<BayesianFactor> rb = new RemoveBarren<>();
			final GraphicalModel<BayesianFactor> rbModel = rb.execute(coModel, evidence, queries);

			final MinFillOrdering mf = new MinFillOrdering();
			final int[] seq = mf.apply(rbModel);

			final FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(seq);
			fve.setNormalize(true);

			return fve.query(rbModel, evidence, queries);
		}

		@Override
		public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int query) {
			return query(model, evidence, new int[]{query});
		}
	};

	@Test
	void testFullObservedData2vars() {
		// this is a linear model A -> B
		final DAGModel<BayesianFactor> model = new DAGModel<>();
		final int A = model.addVariable(2);
		final int B = model.addVariable(2);

		model.addParent(B, A);

		final BayesianFactor fA = BayesianFactorFactory.factory().domain(model.getDomain(A))
				.set(0.4, 0) // P(A=0)
				.set(0.6, 1) // P(A=1)
				.get();

		final BayesianFactor fB = BayesianFactorFactory.factory().domain(model.getDomain(B, A))
				.set(0.4, 0, 0) // P(B=0|A=0)
				.set(0.6, 1, 0) // P(B=1|A=0)
				.set(0.7, 0, 1) // P(B=0|A=1)
				.set(0.3, 1, 1) // P(B=1|A=1)
				.get();

		model.setFactor(A, fA);
		model.setFactor(B, fB);

		final TIntIntMap[] observations = {
				new TIntIntHashMap(new int[]{A, B}, new int[]{0, 0}), // A=0 B=0
				new TIntIntHashMap(new int[]{A, B}, new int[]{0, 1}), // A=0 B=1
				new TIntIntHashMap(new int[]{A, B}, new int[]{1, 0}), // A=1 B=0
				new TIntIntHashMap(new int[]{A, B}, new int[]{1, 1}), // A=1 B=1
		};

		final EMBayesian em = new EMBayesian(engine);

		GraphicalModel<BayesianFactor> learnedModel;
		learnedModel = em.step(model, observations);

		System.out.println("A");
		System.out.printf("P(A=0):     %.2f -> %.2f%n", model.getFactor(A).getValue(0), learnedModel.getFactor(A).getValue(0));
		System.out.printf("P(A=1):     %.2f -> %.2f%n", model.getFactor(A).getValue(1), learnedModel.getFactor(A).getValue(1));

		System.out.println("B");
		System.out.printf("P(B=0|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 0), learnedModel.getFactor(B).getValue(0, 0));
		System.out.printf("P(B=1|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 1), learnedModel.getFactor(B).getValue(0, 1));
		System.out.printf("P(B=0|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 0), learnedModel.getFactor(B).getValue(1, 0));
		System.out.printf("P(B=1|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 1), learnedModel.getFactor(B).getValue(1, 1));

		Assertions.assertArrayEquals(learnedModel.getFactor(A).getData(), new double[]{.5, .5});
		Assertions.assertArrayEquals(learnedModel.getFactor(B).filter(A, 0).getData(), new double[]{.5, .5});
		Assertions.assertArrayEquals(learnedModel.getFactor(B).filter(A, 1).getData(), new double[]{.5, .5});
		Assertions.assertEquals(learnedModel.getFactor(B).getValue(0, 0), .5);
		Assertions.assertEquals(learnedModel.getFactor(B).getValue(0, 1), .5);
		Assertions.assertEquals(learnedModel.getFactor(B).getValue(1, 0), .5);
		Assertions.assertEquals(learnedModel.getFactor(B).getValue(1, 1), .5);
	}

	@Test
	void testFullObservedData3vars() {
		// this is a linear model A -> B -> C
		final DAGModel<BayesianFactor> model = new DAGModel<>();
		final int A = model.addVariable(2);
		final int B = model.addVariable(2);
		final int C = model.addVariable(2);

		model.addParent(B, A);
		model.addParent(C, B);

		final BayesianFactor fA = BayesianFactorFactory.factory().domain(model.getDomain(A))
				.set(0.5, 0) // P(A=0)
				.set(0.5, 1) // P(A=1)
				.get();

		final BayesianFactor fB = BayesianFactorFactory.factory().domain(model.getDomain(B, A))
				.set(0.4, 0, 0) // P(B=0|A=0)
				.set(0.6, 1, 0) // P(B=1|A=0)
				.set(0.7, 0, 1) // P(B=0|A=1)
				.set(0.3, 1, 1) // P(B=1|A=1)
				.get();

		final BayesianFactor fC = BayesianFactorFactory.factory().domain(model.getDomain(C, B))
				.set(0.1, 0, 0) // P(C=0|B=0)
				.set(0.9, 1, 0) // P(C=1|B=0)
				.set(0.2, 0, 1) // P(C=0|B=1)
				.set(0.8, 1, 1) // P(C=1|B=1)
				.get();

		model.setFactor(A, fA);
		model.setFactor(B, fB);
		model.setFactor(C, fC);

		final TIntIntMap[] observations = {
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{0, 1, 1}),
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{0, 1, 1}),
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{1, 0, 0}),
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{1, 0, 0}),
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{0, 1, 1}),
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{1, 0, 1}),
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{1, 1, 1}),
		};

		final EMBayesian em = new EMBayesian(engine);

		GraphicalModel<BayesianFactor> learnedModel;
		learnedModel = em.step(model, observations);

		System.out.println("A");
		System.out.printf("P(A=0):     %.2f %.2f%n", model.getFactor(A).getValue(0), learnedModel.getFactor(A).getValue(0));
		System.out.printf("P(A=1):     %.2f %.2f%n", model.getFactor(A).getValue(1), learnedModel.getFactor(A).getValue(1));

		System.out.println("B");
		System.out.printf("P(B=0|A=0): %.2f %.2f%n", model.getFactor(B).getValue(0, 0), learnedModel.getFactor(B).getValue(0, 0));
		System.out.printf("P(B=1|A=0): %.2f %.2f%n", model.getFactor(B).getValue(0, 1), learnedModel.getFactor(B).getValue(0, 1));
		System.out.printf("P(B=0|A=1): %.2f %.2f%n", model.getFactor(B).getValue(1, 0), learnedModel.getFactor(B).getValue(1, 0));
		System.out.printf("P(B=1|A=1): %.2f %.2f%n", model.getFactor(B).getValue(1, 1), learnedModel.getFactor(B).getValue(1, 1));

		System.out.println("C");
		System.out.printf("P(C=0|B=0): %.2f %.2f%n", model.getFactor(C).getValue(0, 0), learnedModel.getFactor(C).getValue(0, 0));
		System.out.printf("P(C=1|B=0): %.2f %.2f%n", model.getFactor(C).getValue(0, 1), learnedModel.getFactor(C).getValue(0, 1));
		System.out.printf("P(C=0|B=1): %.2f %.2f%n", model.getFactor(C).getValue(1, 0), learnedModel.getFactor(C).getValue(1, 0));
		System.out.printf("P(C=1|B=1): %.2f %.2f%n", model.getFactor(C).getValue(1, 1), learnedModel.getFactor(C).getValue(1, 1));

		Assertions.assertArrayEquals(learnedModel.getFactor(A).getData(), new double[]{.44, .56}, 1e-2);

		Assertions.assertEquals(learnedModel.getFactor(B).getValue(0, 0), .07, 1e-2);
		Assertions.assertEquals(learnedModel.getFactor(B).getValue(0, 1), .93, 1e-2);
		Assertions.assertEquals(learnedModel.getFactor(B).getValue(1, 0), .72, 1e-2);
		Assertions.assertEquals(learnedModel.getFactor(B).getValue(1, 1), .28, 1e-2);

		Assertions.assertEquals(learnedModel.getFactor(C).getValue(0, 0), .64, 1e-2);
		Assertions.assertEquals(learnedModel.getFactor(C).getValue(0, 1), .36, 1e-2);
		Assertions.assertEquals(learnedModel.getFactor(C).getValue(1, 0), .06, 1e-2);
		Assertions.assertEquals(learnedModel.getFactor(C).getValue(1, 1), .94, 1e-2);
	}

	@Test
	void testHiddenData3vars() throws InterruptedException {
		// this is a linear model A -> B
		final DAGModel<BayesianFactor> model = new DAGModel<>();
		final int A = model.addVariable(2);
		final int B = model.addVariable(2);
		final int C = model.addVariable(2);

		model.addParent(B, A);
		model.addParent(C, A);

		final BayesianFactor fA = BayesianFactorFactory.factory().domain(model.getDomain(A))
				.set(0.5, 0) // P(A=0)
				.set(0.5, 1) // P(A=1)
				.get();

		final BayesianFactor fB = BayesianFactorFactory.factory().domain(model.getDomain(B, A))
				.set(0.5, 0, 0) // P(B=0|A=0)
				.set(0.5, 1, 0) // P(B=1|A=0)
				.set(0.5, 0, 1) // P(B=0|A=1)
				.set(0.5, 1, 1) // P(B=1|A=1)
				.get();

		final BayesianFactor fC = BayesianFactorFactory.factory().domain(model.getDomain(C, A))
				.set(0.5, 0, 0) // P(C=0|A=0)
				.set(0.5, 1, 0) // P(C=1|A=0)
				.set(0.5, 0, 1) // P(C=0|A=1)
				.set(0.5, 1, 1) // P(C=1|A=1)
				.get();

		model.setFactor(A, fA);
		model.setFactor(B, fB);
		model.setFactor(C, fC);

		final TIntIntMap[] observations = {
				new TIntIntHashMap(new int[]{   B, C}, new int[]{   1, 1}), // A=? B=1 C=1
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{1, 0, 1}), // A=1 B=0 C=1
				new TIntIntHashMap(new int[]{A, B   }, new int[]{1, 1   }), // A=1 B=1 C=?
				new TIntIntHashMap(new int[]{A, B, C}, new int[]{1, 1, 0}), // A=1 B=1 C=0
				new TIntIntHashMap(new int[]{   B   }, new int[]{   0   }), // A=? B=0 C=?
		};

		final EMBayesian em = new EMBayesian(engine).useBayesianSmoothing(false);

		GraphicalModel<BayesianFactor> learnedModel;
		learnedModel = em.step(model, observations);

		System.out.println("A");
		System.out.printf("P(A=0):     %.2f -> %.2f%n", model.getFactor(A).getValue(0), learnedModel.getFactor(A).getValue(0));
		System.out.printf("P(A=1):     %.2f -> %.2f%n", model.getFactor(A).getValue(1), learnedModel.getFactor(A).getValue(1));

		System.out.println("B");
		System.out.printf("P(B=0|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 0), learnedModel.getFactor(B).getValue(0, 0));
		System.out.printf("P(B=1|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 1), learnedModel.getFactor(B).getValue(0, 1));
		System.out.printf("P(B=0|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 0), learnedModel.getFactor(B).getValue(1, 0));
		System.out.printf("P(B=1|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 1), learnedModel.getFactor(B).getValue(1, 1));

		FrequentistEM fem = (FrequentistEM) new FrequentistEM(model, engine)
				.setRegularization(0.0)
				.setInline(false);

		fem.run(Arrays.asList(observations), 1);

		System.out.println("A");
		System.out.printf("P(A=0):     %.2f -> %.2f%n", model.getFactor(A).getValue(0), fem.posteriorModel.getFactor(A).getValue(0));
		System.out.printf("P(A=1):     %.2f -> %.2f%n", model.getFactor(A).getValue(1), fem.posteriorModel.getFactor(A).getValue(1));

		System.out.println("B");
		System.out.printf("P(B=0|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 0), fem.posteriorModel.getFactor(B).getValue(0, 0));
		System.out.printf("P(B=1|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 1), fem.posteriorModel.getFactor(B).getValue(0, 1));
		System.out.printf("P(B=0|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 0), fem.posteriorModel.getFactor(B).getValue(1, 0));
		System.out.printf("P(B=1|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 1), fem.posteriorModel.getFactor(B).getValue(1, 1));

		System.out.println("C");
		System.out.printf("P(B=0|A=0): %.2f -> %.2f%n", model.getFactor(C).getValue(0, 0), fem.posteriorModel.getFactor(C).getValue(0, 0));
		System.out.printf("P(B=1|A=0): %.2f -> %.2f%n", model.getFactor(C).getValue(0, 1), fem.posteriorModel.getFactor(C).getValue(0, 1));
		System.out.printf("P(B=0|A=1): %.2f -> %.2f%n", model.getFactor(C).getValue(1, 0), fem.posteriorModel.getFactor(C).getValue(1, 0));
		System.out.printf("P(B=1|A=1): %.2f -> %.2f%n", model.getFactor(C).getValue(1, 1), fem.posteriorModel.getFactor(C).getValue(1, 1));

//		Assertions.assertArrayEquals(learnedModel.getFactor(A).getData(), new double[]{.5, .5});
//		Assertions.assertArrayEquals(learnedModel.getFactor(B).filter(A, 0).getData(), new double[]{.5, .5});
//		Assertions.assertArrayEquals(learnedModel.getFactor(B).filter(A, 1).getData(), new double[]{.5, .5});
//		Assertions.assertEquals(learnedModel.getFactor(B).getValue(0, 0), .5);
//		Assertions.assertEquals(learnedModel.getFactor(B).getValue(0, 1), .5);
//		Assertions.assertEquals(learnedModel.getFactor(B).getValue(1, 0), .5);
//		Assertions.assertEquals(learnedModel.getFactor(B).getValue(1, 1), .5);
	}

}
