package ch.idsia.crema.inference.approxlp2;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class MarginalTest {

	@Test
	public void test2NodeQuery() throws InterruptedException {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		model.addVariable(3);
		model.addVariable(3);

		IntervalFactor f0 = new IntervalFactor(model.getDomain(0), model.getDomain(1));
		f0.set(new double[]{0.1, 0.3, 0.5}, new double[]{0.3, 0.8, 0.6}, 0);
		f0.set(new double[]{0.1, 0.5, 0.1}, new double[]{0.3, 0.8, 0.7}, 1);
		f0.set(new double[]{0.2, 0.3, 0.5}, new double[]{0.2, 0.7, 0.9}, 2);

		IntervalFactor f1 = new IntervalFactor(model.getDomain(1), model.getDomain());
		f1.set(new double[]{0.1, 0.3, 0.1}, new double[]{0.6, 0.8, 0.4});

		model.setFactor(0, f0);
		model.setFactor(1, f1);

		ApproxLP2 inference = new ApproxLP2();
		
		// this should work now!
		IntervalFactor factor = inference.query(model, 1);
		assertArrayEquals(new double[]{0.1, 0.3, 0.1}, factor.getLower(), 0.000000001);
		assertArrayEquals(new double[]{0.6, 0.8, 0.4}, factor.getUpper(), 0.000000001);

		factor = inference.query(model, 0);
		assertArrayEquals(new double[]{0.11, 0.36, 0.18}, factor.getLower(), 0.000000001);
		assertArrayEquals(new double[]{0.28, 0.71, 0.53}, factor.getUpper(), 0.000000001);
	}

	@Test
	public void test3VNodeQuery() throws InterruptedException {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		model.addVariable(3);
		model.addVariable(2);
		model.addVariable(2);

		IntervalFactor f0 = new IntervalFactor(model.getDomain(0), model.getDomain(1, 2));
		f0.set(new double[]{0.1, 0.3, 0.5}, new double[]{0.3, 0.8, 0.6}, 0, 0);
		f0.set(new double[]{0.1, 0.5, 0.1}, new double[]{0.3, 0.8, 0.7}, 0, 1);
		f0.set(new double[]{0.0, 0.4, 0.2}, new double[]{0.2, 0.7, 0.9}, 1, 0);
		f0.set(new double[]{0.4, 0.2, 0.1}, new double[]{0.8, 0.5, 0.7}, 1, 1);

		IntervalFactor f1 = new IntervalFactor(model.getDomain(1), model.getDomain());
		f1.set(new double[]{0.1, 0.3}, new double[]{0.7, 0.9});

		IntervalFactor f2 = new IntervalFactor(model.getDomain(2), model.getDomain());
		f2.set(new double[]{0.5, 0.2}, new double[]{0.8, 0.5});

		model.setFactor(0, f0);
		model.setFactor(1, f1);
		model.setFactor(2, f2);

		Inference<GenericFactor> inference = new Inference<>();
		IntervalFactor factor = inference.query(model, 0);

		assertArrayEquals(new double[]{0.082, 0.31, 0.165}, factor.getLower(), 0.000000001);
		assertArrayEquals(new double[]{0.43, 0.642, 0.56}, factor.getUpper(), 0.000000001);
	}

	@Test
	public void testDiamondConfigQuery() throws InterruptedException {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		int n1 = model.addVariable(2);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(3);

		int n0 = model.addVariable(3);

		// root
		IntervalFactor f0 = new IntervalFactor(model.getDomain(n0), model.getDomain(n1, n2));
		f0.set(new double[]{0.1, 0.3, 0.5}, new double[]{0.3, 0.8, 0.6}, 0, 0);
		f0.set(new double[]{0.1, 0.5, 0.1}, new double[]{0.3, 0.8, 0.7}, 0, 1);
		f0.set(new double[]{0.0, 0.4, 0.2}, new double[]{0.2, 0.7, 0.9}, 1, 0);
		f0.set(new double[]{0.4, 0.2, 0.1}, new double[]{0.8, 0.5, 0.7}, 1, 1);
		model.setFactor(n0, f0);

		IntervalFactor f3 = new IntervalFactor(model.getDomain(n3), model.getDomain());
		f3.set(new double[]{0.2, 0.7, 0.1}, new double[]{0.3, 0.8, 0.5});
		model.setFactor(n3, f3);

		IntervalFactor f1 = new IntervalFactor(model.getDomain(n1), model.getDomain(n3));
		f1.set(new double[]{0.6, 0.3}, new double[]{0.7, 0.4}, 0);
		f1.set(new double[]{0.1, 0.5}, new double[]{0.5, 0.9}, 1);
		f1.set(new double[]{0.1, 0.8}, new double[]{0.2, 0.9}, 2);
		model.setFactor(n1, f1);

		IntervalFactor f2 = new IntervalFactor(model.getDomain(n2), model.getDomain(n3));
		f2.set(new double[]{0.1, 0.8}, new double[]{0.2, 0.9}, 0);
		f2.set(new double[]{0.5, 0.4}, new double[]{0.6, 0.5}, 1);
		f2.set(new double[]{0.3, 0.4}, new double[]{0.6, 0.7}, 2);
		model.setFactor(n2, f2);

		Inference<GenericFactor> inference = new Inference<>();
		IntervalFactor factor = inference.query(model, n0);

		assertArrayEquals(new double[]{0.139, 0.3192, 0.155}, factor.getLower(), 0.000000001);
		assertArrayEquals(new double[]{0.440, 0.6288, 0.504}, factor.getUpper(), 0.000000001);
	}

	double zero = 0.0000000001;
	double one = 1 - zero;

	@Test
	public void testSimplePosteriorQuery() throws InterruptedException {
		GraphicalModel<GenericFactor> model = new DAGModel<>();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(2);

		// root
		IntervalFactor f0 = new IntervalFactor(model.getDomain(n0), model.getDomain(n1));
		f0.set(new double[]{0.7, zero}, new double[]{one, 0.3}, 0);
		f0.set(new double[]{zero, 0.7}, new double[]{0.3, one}, 1);
		model.setFactor(n0, f0);

		IntervalFactor f1 = new IntervalFactor(model.getDomain(n1), model.getDomain());
		f1.set(new double[]{0.9, zero}, new double[]{one, 0.1});
		model.setFactor(n1, f1);

		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence bin = new BinarizeEvidence();
		model = bin.execute(model, evidence, 2, false);
		int ev = bin.getLeafDummy();

		Inference<GenericFactor> inference = new Inference<>();
		IntervalFactor factor = inference.query(model, n1, ev);

		assertArrayEquals(new double[]{0.954545454545, 0}, factor.getLower(), 0.000000001);
		assertArrayEquals(new double[]{1, 0.0454545454545454}, factor.getUpper(), 0.000000001);
	}

}
