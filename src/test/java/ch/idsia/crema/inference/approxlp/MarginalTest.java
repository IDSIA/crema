package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.graphical.MixedModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class MarginalTest {

	double zero = 0.0000000001;
	double one = 1 - zero;

	@BeforeAll
	static void beforeAll() {
		RandomUtil.setRandomSeed(0);
	}

	@Test
	public void test2NodeQuery() {
		final DAGModel<IntervalFactor> model = new DAGModel<>();

		model.addVariable(3);
		model.addVariable(3);

		IntervalFactor f0 = IntervalFactorFactory.factory().domain(model.getDomain(0), model.getDomain(1))
				.set(new double[]{0.1, 0.3, 0.5}, new double[]{0.3, 0.8, 0.6}, 0)
				.set(new double[]{0.1, 0.5, 0.1}, new double[]{0.3, 0.8, 0.7}, 1)
				.set(new double[]{0.2, 0.3, 0.5}, new double[]{0.2, 0.7, 0.9}, 2)
				.build();

		IntervalFactor f1 = IntervalFactorFactory.factory().domain(model.getDomain(1), model.getDomain())
				.set(new double[]{0.1, 0.3, 0.1}, new double[]{0.6, 0.8, 0.4})
				.build();

		model.setFactor(0, f0);
		model.setFactor(1, f1);

		try {
			ApproxLP1<IntervalFactor> inference = new ApproxLP1<>();
			IntervalFactor factor = inference.query(model, 0);

			assertArrayEquals(new double[]{0.11, 0.36, 0.18}, factor.getLower(), 1e-9);
			assertArrayEquals(new double[]{0.28, 0.71, 0.53}, factor.getUpper(), 1e-9);

			// should work now
//			inference.query(model, 1); TODO: this throws NoFeasibleSolution... again...
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test
	public void test3VNodeQuery() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		model.addVariable(3);
		model.addVariable(2);
		model.addVariable(2);

		IntervalFactor f0 = IntervalFactorFactory.factory().domain(model.getDomain(0), model.getDomain(1, 2))
				.set(new double[]{0.1, 0.3, 0.5}, new double[]{0.3, 0.8, 0.6}, 0, 0)
				.set(new double[]{0.1, 0.5, 0.1}, new double[]{0.3, 0.8, 0.7}, 0, 1)
				.set(new double[]{0.0, 0.4, 0.2}, new double[]{0.2, 0.7, 0.9}, 1, 0)
				.set(new double[]{0.4, 0.2, 0.1}, new double[]{0.8, 0.5, 0.7}, 1, 1)
				.build();

		IntervalFactor f1 = IntervalFactorFactory.factory().domain(model.getDomain(1), model.getDomain())
				.set(new double[]{0.1, 0.3}, new double[]{0.7, 0.9})
				.build();

		IntervalFactor f2 = IntervalFactorFactory.factory().domain(model.getDomain(2), model.getDomain())
				.set(new double[]{0.5, 0.2}, new double[]{0.8, 0.5})
				.build();

		model.setFactor(0, f0);
		model.setFactor(1, f1);
		model.setFactor(2, f2);

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			IntervalFactor factor = inference.query(model, 0);

			assertArrayEquals(new double[]{0.082, 0.31, 0.165}, factor.getLower(), 1e-9);
			assertArrayEquals(new double[]{0.43, 0.642, 0.56}, factor.getUpper(), 1e-9);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test
	public void testDiamondConfigQuery() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		int n1 = model.addVariable(2);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(3);

		int n0 = model.addVariable(3);

		// root
		IntervalFactor f0 = IntervalFactorFactory.factory().domain(model.getDomain(n0), model.getDomain(n1, n2))
				.set(new double[]{0.1, 0.3, 0.5}, new double[]{0.3, 0.8, 0.6}, 0, 0)
				.set(new double[]{0.1, 0.5, 0.1}, new double[]{0.3, 0.8, 0.7}, 0, 1)
				.set(new double[]{0.0, 0.4, 0.2}, new double[]{0.2, 0.7, 0.9}, 1, 0)
				.set(new double[]{0.4, 0.2, 0.1}, new double[]{0.8, 0.5, 0.7}, 1, 1)
				.build();
		model.setFactor(n0, f0);

		IntervalFactor f3 = IntervalFactorFactory.factory().domain(model.getDomain(n3), model.getDomain())
				.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5})
				.build();
		model.setFactor(n3, f3);

		IntervalFactor f1 = IntervalFactorFactory.factory().domain(model.getDomain(n1), model.getDomain(n3))
				.set(new double[]{0.6, 0.3}, new double[]{0.7, 0.4}, 0)
				.set(new double[]{0.1, 0.5}, new double[]{0.5, 0.9}, 1)
				.set(new double[]{0.1, 0.8}, new double[]{0.2, 0.9}, 2)
				.build();
		model.setFactor(n1, f1);

		IntervalFactor f2 = IntervalFactorFactory.factory().domain(model.getDomain(n2), model.getDomain(n3))
				.set(new double[]{0.1, 0.8}, new double[]{0.2, 0.9}, 0)
				.set(new double[]{0.5, 0.4}, new double[]{0.6, 0.5}, 1)
				.set(new double[]{0.3, 0.4}, new double[]{0.6, 0.7}, 2)
				.build();
		model.setFactor(n2, f2);

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			IntervalFactor factor = inference.query(model, n0);

			assertArrayEquals(new double[]{0.139, 0.3192, 0.155}, factor.getLower(), 1e-9);
			assertArrayEquals(new double[]{0.440, 0.6288, 0.504}, factor.getUpper(), 1e-9);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

	@Test
	public void testSimplePosteriorQuery() {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(2);

		// root
		IntervalFactor f0 = IntervalFactorFactory.factory().domain(model.getDomain(n0), model.getDomain(n1))
				.set(new double[]{0.7, zero}, new double[]{one, 0.3}, 0)
				.set(new double[]{zero, 0.7}, new double[]{0.3, one}, 1)
				.build();
		model.setFactor(n0, f0);

		IntervalFactor f1 = IntervalFactorFactory.factory().domain(model.getDomain(n1), model.getDomain())
				.set(new double[]{0.9, zero}, new double[]{one, 0.1})
				.build();
		model.setFactor(n1, f1);

		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence<GenericFactor> bin = new BinarizeEvidence<>();
		bin.setSize(2);
		final MixedModel mixedModel = bin.execute(model, evidence);
		int ev = bin.getEvidenceNode();

		try {
			ApproxLP1<GenericFactor> inference = new ApproxLP1<>();
			inference.setEvidenceNode(ev);
			IntervalFactor factor = inference.query(mixedModel, n1);

		/*
		assertArrayEquals(new double[]{ 0.954545454545, 0 }, factor.getLower(), 1e-9);
		assertArrayEquals(new double[]{1, 0.0454545454545454}, factor.getUpper(), 1e-9);
		*/
		} catch (Exception e) {
			System.err.println(e.getMessage());
			throw e;
		}
	}

}
