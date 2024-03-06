package ch.idsia.crema.inference.approxlp1;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.graphical.MixedModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.RemoveBarren;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class PosteriorTest {

	@Test
	public void targetFree2() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();
		int E = model.addVariable(2);
		int X0 = model.addVariable(2);

		IntervalFactor fe = IntervalFactorFactory.factory().domain(model.getDomain(E), model.getDomain(X0))
				.lower(new double[]{0.2, 0.7}, 0)
				.upper(new double[]{0.3, 0.8}, 0)

				.lower(new double[]{0.6, 0.0}, 1)
				.upper(new double[]{1.0, 0.4}, 1)
				.get();
		model.setFactor(E, fe);

		IntervalFactor fx0 = IntervalFactorFactory.factory().domain(model.getDomain(X0), model.getDomain())
				.bounds(0.1, 0.2, 0)
				.bounds(0.8, 0.9, 1)
				.get();

		// alternative bounds set
		//.lower(new double[] { .1, .8 });
		//.upper(new double[] { .2, .9 });
		model.setFactor(X0, fx0);

		Int2IntMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		GraphicalModel<GenericFactor> bmodel = be.execute(model, observation);
		int evidence = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inf = new ApproxLP1<>();
			inf.setEvidenceNode(evidence);
			IntervalFactor ifact = inf.query(bmodel, X0);

//		assertArrayEquals(new double[] { 0.07 / 0.43, 0.32 / 0.46 }, ifact.getLower(), 0.001);
//		assertArrayEquals(new double[] { 0.14 / 0.46, 0.36 / 0.43 }, ifact.getUpper(), 0.001);

			ApproxLP2<GenericFactor> a2 = new ApproxLP2<>();
			a2.initialize(null);

			IntervalFactor i2 = a2.query(model, observation, X0);
			System.out.println(Arrays.toString(i2.getLower()) + " vs " + Arrays.toString(ifact.getLower()));
			System.out.println(Arrays.toString(i2.getUpper()) + " vs " + Arrays.toString(ifact.getUpper()));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}


	@Test
	public void targetFree2WithParent() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();
		int E = model.addVariable(3);
		int X0 = model.addVariable(2);
		int Xj = model.addVariable(3);

		BayesianFactor fe = BayesianFactorFactory.factory().domain(model.getDomain(E, X0))
				.data(new double[]{.3, .6, .1, .6, .2, .2})
				.get();
		model.setFactor(E, fe);

		IntervalFactor fx0 = IntervalFactorFactory.factory().domain(model.getDomain(X0), model.getDomain(Xj))
				.bounds(0.1, 0.2, 0, 0)
				.bounds(0.8, 0.9, 1, 0)

				.bounds(0.5, 0.8, 0, 1)
				.bounds(0.2, 0.5, 1, 1)

				.bounds(0.4, 0.8, 0, 2)
				.bounds(0.2, 0.6, 1, 2)
				.get();

		model.setFactor(X0, fx0);

		BayesianFactor fj = BayesianFactorFactory.factory().domain(model.getDomain(Xj))
				.data(new double[]{.3, .5, .2})
				.get();
		model.setFactor(Xj, fj);

		Int2IntMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		MixedModel mixedModel = be.execute(model, observation);
		int evidence = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inf = new ApproxLP1<>();
			inf.setEvidenceNode(evidence);
			IntervalFactor ifact = inf.query(mixedModel, X0);

			assertArrayEquals(new double[]{0.6279069767434073, 0.16964285714332666}, ifact.getLower(), 0.001);
			assertArrayEquals(new double[]{0.8303571428566734, 0.37209302325659266}, ifact.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test
	public void targetFree3() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();
		int E = model.addVariable(2);
		int X0 = model.addVariable(3);

		BayesianFactor fe = BayesianFactorFactory.factory().domain(model.getDomain(E, X0))
				.data(new double[]{.3, .7, .6, .4, .8, .2})
				.get();
		model.setFactor(E, fe);

		IntervalFactor fx0 = IntervalFactorFactory.factory().domain(model.getDomain(X0), model.getDomain())
				.bounds(0.1, 0.2, 0)
				.bounds(0.2, 0.5, 1)
				.bounds(0.5, 0.7, 2)
				.get();

		model.setFactor(X0, fx0);

		Int2IntMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		MixedModel mixedModel = be.execute(model, observation);
		int evidence = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inf = new ApproxLP1<>();
			inf.setEvidenceNode(evidence);
			IntervalFactor ifact = inf.query(mixedModel, X0);

			assertArrayEquals(new double[]{0.21212121212087243, 0.23529411764741176, 0.277777777778395}, ifact.getLower(), 0.001);
			assertArrayEquals(new double[]{0.4117647058825294, 0.48484848484822773, 0.48275862069158587}, ifact.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test
	public void targetFree2WithChild() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();
		int E = model.addVariable(2);
		int Xj = model.addVariable(2);
		int X0 = model.addVariable(2);

		BayesianFactor fe = BayesianFactorFactory.factory().domain(model.getDomain(E, Xj))
				.data(new double[]{.3, .7, .6, .4})
				.get();
		model.setFactor(E, fe);

		BayesianFactor fx0 = BayesianFactorFactory.factory().domain(model.getDomain(X0))
				.data(new double[]{.3, .7})
				.get();
		model.setFactor(X0, fx0);

		IntervalFactor fj = IntervalFactorFactory.factory().domain(model.getDomain(Xj), model.getDomain(X0))
				.bounds(.2, .6, 0, 0)
				.bounds(.4, .8, 1, 0)
				.bounds(.4, .7, 0, 1)
				.bounds(.3, .6, 1, 1)
				.get();
		model.setFactor(Xj, fj);

		Int2IntMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		MixedModel mixedModel = be.execute(model, observation);
		int evidence = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inf = new ApproxLP1<>();
			inf.setEvidenceNode(evidence);
			IntervalFactor ifact = inf.query(mixedModel, X0);

			assertArrayEquals(new double[]{0.24424778761090626, 0.6765799256506011}, ifact.getLower(), 0.001);
			assertArrayEquals(new double[]{0.3234200743493989, 0.7557522123890937}, ifact.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test
	public void testDiamondBayesianPosteriorQuery() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(2);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);

		// root
		BayesianFactor f0 = BayesianFactorFactory.factory().domain(model.getDomain(n0, n1, n2))
				.data(new double[]{0.1, 0.9, 0.4, 0.6, 0.8, 0.2, 0.3, 0.7})
				.get();
		model.setFactor(n0, f0);

		BayesianFactor f3 = BayesianFactorFactory.factory().domain(model.getDomain(n3))
				.data(new double[]{0.3, 0.7})
				.get();
		model.setFactor(n3, f3);

		BayesianFactor f1 = BayesianFactorFactory.factory().domain(model.getDomain(n1, n3))
				.data(new double[]{0.4, 0.6, 0.5, 0.5})
				.get();
		model.setFactor(n1, f1);

		BayesianFactor f2 = BayesianFactorFactory.factory().domain(model.getDomain(n2, n3))
				.data(new double[]{0.7, 0.3, 0.1, 0.9})
				.get();
		model.setFactor(n2, f2);

		Int2IntMap evidence = new Int2IntOpenHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		MixedModel mixedModel = be.execute(model, evidence);
		int ev = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);
			IntervalFactor factor = inference.query(mixedModel, n3);

			assertArrayEquals(new double[]{0.22188969645147497, 0.7781103035492434}, factor.getLower(), 0.001);
			assertArrayEquals(new double[]{0.22188969645147497, 0.7781103035492434}, factor.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test
	public void testDiamondBayesianPosteriorQuery3() {
		MixedModel model = new MixedModel();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(3);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);

		// root
		BayesianFactor f0 = BayesianFactorFactory.factory().domain(model.getDomain(n0, n1, n2))
				.data(new double[]{0.1, 0.9, 0.4, 0.6, 0.5, 0.5, 0.8, 0.2, 0.3, 0.7, 0.9, 0.1})
				.get();
		model.setFactor(n0, f0);

		IntervalFactor f3 = IntervalFactorFactory.factory().domain(model.getDomain(n3), model.getDomain())
				.bounds(.3, .4, 0)
				.bounds(.6, .7, 1)
				.get();
		model.setFactor(n3, f3);

		IntervalFactor f1 = IntervalFactorFactory.factory().domain(model.getDomain(n1), model.getDomain(n3))
				.bounds(.1, .6, 0, 0)
				.bounds(.5, .8, 1, 0)
				.bounds(.3, .8, 2, 0)
				.bounds(.3, .7, 0, 1)
				.bounds(.3, .5, 1, 1)
				.bounds(.2, .4, 2, 1)
				.get();
		model.setFactor(n1, f1);

		BayesianFactor f2 = BayesianFactorFactory.factory().domain(model.getDomain(n2, n3))
				.data(new double[]{0.7, 0.3, 0.1, 0.9})
				.get();
		model.setFactor(n2, f2);

		Int2IntMap evidence = new Int2IntOpenHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		model = be.execute(model, evidence);
		int ev = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);
			IntervalFactor factor = inference.query(model, n3);

			assertArrayEquals(new double[]{0.22050585639124004, 0.6383476227590834}, factor.getLower(), 0.001);
			assertArrayEquals(new double[]{0.3616523772409166, 0.7794941436087599}, factor.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}

		RemoveBarren<GenericFactor> barren = new RemoveBarren<>();
		GraphicalModel<GenericFactor> barrenModel = barren.execute(model, ObservationBuilder.vars(ev).states(1), n1);

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);
			// no need to update n1 as we use the sparse model
			IntervalFactor factor = inference.query(barrenModel, n1);

			assertArrayEquals(new double[]{0.24827348066293425, 0.20153743315534500, 0.3076654443861050}, factor.getLower(), 0.001);
			assertArrayEquals(new double[]{0.48011911017679076, 0.36128775834693705, 0.5276243093920449}, factor.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	/**
	 * Same as {@link #testDiamondBayesianPosteriorQuery3()} but with parent for n3
	 *
	 * @
	 */
	@Test
	public void testDiamondBayesianPosteriorQuery4() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(3);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);
		int n4 = model.addVariable(3);

		// root
		BayesianFactor f0 = BayesianFactorFactory.factory().domain(model.getDomain(n0, n1, n2))
				.data(new double[]{0.1, 0.9, 0.4, 0.6, 0.5, 0.5, 0.8, 0.2, 0.3, 0.7, 0.9, 0.1})
				.get();
		model.setFactor(n0, f0);

		IntervalFactor f3 = IntervalFactorFactory.factory().domain(model.getDomain(n3), model.getDomain(n4))
				.bounds(.3, .4, 0, 0)
				.bounds(.6, .7, 1, 0)
				.bounds(.1, .4, 0, 1)
				.bounds(.6, .9, 1, 1)
				.bounds(.2, .5, 0, 2)
				.bounds(.5, .8, 1, 2)
				.get();
		model.setFactor(n3, f3);

		IntervalFactor f4 = IntervalFactorFactory.factory().domain(model.getDomain(n4), model.getDomain())
				.bounds(.1, .6, 0)
				.bounds(.3, .7, 1)
				.bounds(.1, .4, 2)
				.get();
		model.setFactor(n4, f4);

		IntervalFactor f1 = IntervalFactorFactory.factory().domain(model.getDomain(n1), model.getDomain(n3))
				.bounds(.1, .6, 0, 0)
				.bounds(.5, .8, 1, 0)
				.bounds(.3, .8, 2, 0)
				.bounds(.3, .7, 0, 1)
				.bounds(.3, .5, 1, 1)
				.bounds(.2, .4, 2, 1)
				.get();
		model.setFactor(n1, f1);

		BayesianFactor f2 = BayesianFactorFactory.factory().domain(model.getDomain(n2, n3))
				.data(new double[]{0.7, 0.3, 0.1, 0.9})
				.get();
		model.setFactor(n2, f2);

		Int2IntMap evidence = new Int2IntOpenHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		MixedModel mixedModel = be.execute(model, evidence);
		int ev = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);
			IntervalFactor factor = inference.query(mixedModel, n3);

			assertArrayEquals(new double[]{0.09702615320599722, 0.5996234703482123}, factor.getLower(), 0.001);
			assertArrayEquals(new double[]{0.4003765296517877, 0.9029738467940027}, factor.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}

		try {
			RemoveBarren<GenericFactor> barren = new RemoveBarren<>();
			GraphicalModel<GenericFactor> barrenModel = barren.execute(mixedModel, ObservationBuilder.vars(ev).states(1), n1);

			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);
			// no need to update n1 as we use the sparse model
			IntervalFactor factor = inference.query(barrenModel, n1);

			assertArrayEquals(new double[]{0.23845184770432107, 0.1682985757886567, 0.28836654178948645}, factor.getLower(), 0.001);
			assertArrayEquals(new double[]{0.5354735898541261, 0.3697586787464407, 0.5279955207164615}, factor.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	/**
	 * Network with unconnected parts will work after barren
	 */
	@Test()
	public void testLeftOver() {
		MixedModel model = new MixedModel();
		int n = model.addVariable(3);
		int n0 = model.addVariable(2);
		int n1 = model.addVariable(3);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);
		int n4 = model.addVariable(3);
		int n5 = model.addVariable(3);

		// stray
		BayesianFactor fx = BayesianFactorFactory.factory().domain(model.getDomain(n))
				.data(new double[]{0.1, 0.3, 0.6})
				.get();
		model.setFactor(n, fx);

		IntervalFactor fy = IntervalFactorFactory.factory().domain(model.getDomain(n5), model.getDomain(n))
				.bounds(.3, .4, 0, 0)
				.bounds(.2, .7, 1, 0)
				.bounds(.2, .5, 2, 0)
				.bounds(.1, .4, 0, 1)
				.bounds(.3, .9, 1, 1)
				.bounds(.2, .5, 2, 1)
				.bounds(.2, .5, 0, 2)
				.bounds(.3, .8, 1, 2)
				.bounds(.3, .8, 2, 2)
				.get();
		model.setFactor(n5, fy);

		BayesianFactor f0 = BayesianFactorFactory.factory().domain(model.getDomain(n0, n1, n2))
				.data(new double[]{0.1, 0.9, 0.4, 0.6, 0.5, 0.5, 0.8, 0.2, 0.3, 0.7, 0.9, 0.1})
				.get();
		model.setFactor(n0, f0);

		IntervalFactor f3 = IntervalFactorFactory.factory().domain(model.getDomain(n3), model.getDomain(n4))
				.bounds(.3, .4, 0, 0)
				.bounds(.6, .7, 1, 0)
				.bounds(.1, .4, 0, 1)
				.bounds(.6, .9, 1, 1)
				.bounds(.2, .5, 0, 2)
				.bounds(.5, .8, 1, 2)
				.get();
		model.setFactor(n3, f3);

		IntervalFactor f4 = IntervalFactorFactory.factory().domain(model.getDomain(n4), model.getDomain())
				.bounds(.1, .6, 0)
				.bounds(.3, .7, 1)
				.bounds(.1, .4, 2)
				.get();
		model.setFactor(n4, f4);

		IntervalFactor f1 = IntervalFactorFactory.factory().domain(model.getDomain(n1), model.getDomain(n3))
				.bounds(.1, .6, 0, 0)
				.bounds(.5, .8, 1, 0)
				.bounds(.3, .8, 2, 0)
				.bounds(.3, .7, 0, 1)
				.bounds(.3, .5, 1, 1)
				.bounds(.2, .4, 2, 1)
				.get();
		model.setFactor(n1, f1);

		BayesianFactor f2 = BayesianFactorFactory.factory().domain(model.getDomain(n2, n3))
				.data(new double[]{0.7, 0.3, 0.1, 0.9})
				.get();
		model.setFactor(n2, f2);

		Int2IntMap evidence = new Int2IntOpenHashMap();
		evidence.put(n0, 0);

		RemoveBarren<GenericFactor> barren = new RemoveBarren<>();
		final GraphicalModel<GenericFactor> modelBarren = barren.execute(model, evidence, n3);

		BinarizeEvidence<GenericFactor> be = new BinarizeEvidence<>(2);
		model = be.execute(modelBarren, evidence);
		int ev = be.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);
			IntervalFactor factor = inference.query(model, n3);

			assertArrayEquals(new double[]{0.09702615320599722, 0.5996234703482123}, factor.getLower(), 0.001);
			assertArrayEquals(new double[]{0.4003765296517877, 0.9029738467940027}, factor.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);

			// no need to update n1 as we use the sparse model
			IntervalFactor factor = inference.query(model, n1);

			assertArrayEquals(new double[]{0.23845184770432107, 0.1682985757886567, 0.28836654178948645}, factor.getLower(), 0.001);
			assertArrayEquals(new double[]{0.5354735898541261, 0.3697586787464407, 0.5279955207164615}, factor.getUpper(), 0.001);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}
}
