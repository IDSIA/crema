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
import ch.idsia.crema.utility.ProbabilityUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Test;

class EMBayesianTest {

	final static InferenceJoined<GraphicalModel<BayesianFactor>, BayesianFactor> engine = new InferenceJoined<>() {
		@Override
		public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int... queries) {
			final CutObserved<BayesianFactor> co = new CutObserved<>();
			final GraphicalModel<BayesianFactor> coModel = co.execute(model, evidence);

			final MinFillOrdering mf = new MinFillOrdering();
			final int[] seq = mf.apply(coModel);

			final RemoveBarren<BayesianFactor> rb = new RemoveBarren<>();
			final GraphicalModel<BayesianFactor> infModel = rb.execute(coModel, evidence, queries);
			rb.filter(seq);

			final FactorVariableElimination<BayesianFactor> fve = new FactorVariableElimination<>(seq);
			fve.setNormalize(true);

			return fve.query(infModel, evidence, queries);
		}

		@Override
		public BayesianFactor query(GraphicalModel<BayesianFactor> model, TIntIntMap evidence, int query) {
			return query(model, evidence, new int[]{query});
		}
	};

	@Test
	void testFullObservedData2vars() {

		// this is a linear model A -> B -> C
		final DAGModel<BayesianFactor> model = new DAGModel<>();
		final int A = model.addVariable(2);
		final int B = model.addVariable(2);

		model.addParent(B, A);

		final BayesianFactor fA = BayesianFactorFactory.factory().domain(model.getDomain(A))
				.set(0.5, 0)
				.set(0.5, 1)
				.get();

		final BayesianFactor fB = BayesianFactorFactory.factory().domain(model.getDomain(B, A))
				.set(0.4, 0, 0)
				.set(0.6, 0, 1)
				.set(0.7, 1, 0)
				.set(0.3, 1, 1)
				.get();

		model.setFactor(A, fA);
		model.setFactor(B, fB);

		final TIntIntMap[] observations = {
				new TIntIntHashMap(new int[]{A, B}, new int[]{0, 0}),
				new TIntIntHashMap(new int[]{A, B}, new int[]{0, 1}),
				new TIntIntHashMap(new int[]{A, B}, new int[]{1, 0}),
				new TIntIntHashMap(new int[]{A, B}, new int[]{1, 1}),
		};

		final EMBayesian em = new EMBayesian(engine);

		GraphicalModel<BayesianFactor> learnedModel;
		learnedModel = em.run(model, observations);

		System.out.println("it score");
		for (int i = 0; i <= em.getIterations(); i++) {
			System.out.printf("%2d %8.4f%n", i, em.getScores().get(i));
		}

		System.out.println("A");
		System.out.printf("P(A=0):     %.2f -> %.2f%n", model.getFactor(A).getValue(0), learnedModel.getFactor(A).getValue(0));
		System.out.printf("P(A=1):     %.2f -> %.2f%n", model.getFactor(A).getValue(1), learnedModel.getFactor(A).getValue(1));

		System.out.println("B");
		System.out.printf("P(B=0|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 0), learnedModel.getFactor(B).getValue(0, 0));
		System.out.printf("P(B=0|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 0), learnedModel.getFactor(B).getValue(1, 0));
		System.out.printf("P(B=1|A=0): %.2f -> %.2f%n", model.getFactor(B).getValue(0, 1), learnedModel.getFactor(B).getValue(0, 1));
		System.out.printf("P(B=1|A=1): %.2f -> %.2f%n", model.getFactor(B).getValue(1, 1), learnedModel.getFactor(B).getValue(1, 1));
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
				.set(0.5, 0)
				.set(0.5, 1)
				.get();

		final BayesianFactor fB = BayesianFactorFactory.factory().domain(model.getDomain(B, A))
				.set(0.4, 0, 0)
				.set(0.6, 0, 1)
				.set(0.7, 1, 0)
				.set(0.3, 1, 1)
				.get();

		final BayesianFactor fC = BayesianFactorFactory.factory().domain(model.getDomain(C, B))
				.set(0.1, 0, 0)
				.set(0.9, 0, 1)
				.set(0.2, 1, 0)
				.set(0.8, 1, 1)
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
		};

		final EMBayesian em = new EMBayesian(engine);

		GraphicalModel<BayesianFactor> learnedModel;
//		learnedModel = em.step(model, observations);
		learnedModel = em.run(model, observations);

		double originalLikelihood = ProbabilityUtil.logLikelihood(model, observations);
		double learnedLikelihood = ProbabilityUtil.logLikelihood(learnedModel, observations);

		System.out.println("original model likelihood: " + originalLikelihood);
		System.out.println("learned model likelihood:  " + learnedLikelihood);

		System.out.println("A");
		System.out.printf("P(A=0):     %.2f %.2f%n", model.getFactor(A).getValue(0), learnedModel.getFactor(A).getValue(0));
		System.out.printf("P(A=1):     %.2f %.2f%n", model.getFactor(A).getValue(1), learnedModel.getFactor(A).getValue(1));

		System.out.println("B");
		System.out.printf("P(B=0|A=0): %.2f %.2f%n", model.getFactor(B).getValue(0, 0), learnedModel.getFactor(B).getValue(0, 0));
		System.out.printf("P(B=0|A=1): %.2f %.2f%n", model.getFactor(B).getValue(1, 0), learnedModel.getFactor(B).getValue(1, 0));
		System.out.printf("P(B=1|A=0): %.2f %.2f%n", model.getFactor(B).getValue(0, 1), learnedModel.getFactor(B).getValue(0, 1));
		System.out.printf("P(B=1|A=1): %.2f %.2f%n", model.getFactor(B).getValue(1, 1), learnedModel.getFactor(B).getValue(1, 1));

		System.out.println("C");
		System.out.printf("P(C=0|B=0): %.2f %.2f%n", model.getFactor(C).getValue(0, 0), learnedModel.getFactor(C).getValue(0, 0));
		System.out.printf("P(C=0|B=1): %.2f %.2f%n", model.getFactor(C).getValue(1, 0), learnedModel.getFactor(C).getValue(1, 0));
		System.out.printf("P(C=1|B=0): %.2f %.2f%n", model.getFactor(C).getValue(0, 1), learnedModel.getFactor(C).getValue(0, 1));
		System.out.printf("P(C=1|B=1): %.2f %.2f%n", model.getFactor(C).getValue(1, 1), learnedModel.getFactor(C).getValue(1, 1));
	}
}
