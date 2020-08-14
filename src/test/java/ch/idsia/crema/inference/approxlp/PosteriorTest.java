package ch.idsia.crema.inference.approxlp;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.ObservationBuilder;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class PosteriorTest {

	@Test
	public void targetFree2() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();
		int E = model.addVariable(2);
		int X0 = model.addVariable(2);

		IntervalFactor fe = new IntervalFactor(model.getDomain(E), model.getDomain(X0));
		fe.setLower(new double[] { 0.2, 0.7 }, 0);
		fe.setUpper(new double[] { 0.3, 0.8 }, 0);
		
		fe.setLower(new double[] { 0.6, 0.0 }, 1);
		fe.setUpper(new double[] { 1.0, 0.4 }, 1);
		model.setFactor(E, fe);

		IntervalFactor fx0 = new IntervalFactor(model.getDomain(X0), model.getDomain());
		fx0.setBounds(0.1, 0.2, 0);
		fx0.setBounds(0.8, 0.9, 1);

		// alternative bounds set
		// fx0.setLower(new double[] { .1, .8 });
		// fx0.setUpper(new double[] { .2, .9 });
		model.setFactor(X0, fx0);

		TIntIntHashMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence be = new BinarizeEvidence();
		SparseModel<GenericFactor> bmodel = be.execute(model, observation, 2, false);
		int evidence = be.getLeafDummy();
		
		Inference inf = new Inference();
		IntervalFactor ifact = inf.query(bmodel, X0, evidence);

//		assertArrayEquals(new double[] { 0.07 / 0.43, 0.32 / 0.46 }, ifact.getLower(), 0.001);
//		assertArrayEquals(new double[] { 0.14 / 0.46, 0.36 / 0.43 }, ifact.getUpper(), 0.001);
		
		
		ApproxLP2 a2 = new ApproxLP2();
		a2.initialize(null);
		
		IntervalFactor i2 = a2.query(model, X0, observation);
		System.out.println(Arrays.toString(i2.getLower()) + " vs " + Arrays.toString(ifact.getLower()));
		System.out.println(Arrays.toString(i2.getUpper()) + " vs " + Arrays.toString(ifact.getUpper()));
	}

	
	@Test
	public void targetFree2WithParent() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();
		int E = model.addVariable(3);
		int X0 = model.addVariable(2);
		int Xj = model.addVariable(3);

		BayesianFactor fe = new BayesianFactor(model.getDomain(E, X0), false);
		fe.setData(new double[] { .3, .6, .1, .6, .2, .2 });
		model.setFactor(E, fe);

		IntervalFactor fx0 = new IntervalFactor(model.getDomain(X0), model.getDomain(Xj));
		fx0.setBounds(0.1, 0.2, 0, 0);
		fx0.setBounds(0.8, 0.9, 1, 0);

		fx0.setBounds(0.5, 0.8, 0, 1);
		fx0.setBounds(0.2, 0.5, 1, 1);

		fx0.setBounds(0.4, 0.8, 0, 2);
		fx0.setBounds(0.2, 0.6, 1, 2);

		model.setFactor(X0, fx0);

		BayesianFactor fj = new BayesianFactor(model.getDomain(Xj), false);
		fj.setData(new double[] { .3, .5, .2 });
		model.setFactor(Xj, fj);

		TIntIntHashMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence be = new BinarizeEvidence();
		int evidence = be.executeInline(model, observation, 2, false);

		Inference inf = new Inference();
		IntervalFactor ifact = inf.query(model, X0, evidence);

		assertArrayEquals(new double[] { 0.6279069767434073, 0.16964285714332666 }, ifact.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.8303571428566734, 0.37209302325659266 }, ifact.getUpper(), 0.001);
	}

	@Test
	public void targetFree3() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();
		int E = model.addVariable(2);
		int X0 = model.addVariable(3);

		BayesianFactor fe = new BayesianFactor(model.getDomain(E, X0), false);
		fe.setData(new double[] { .3, .7, .6, .4, .8, .2 });
		model.setFactor(E, fe);

		IntervalFactor fx0 = new IntervalFactor(model.getDomain(X0), model.getDomain());
		fx0.setBounds(0.1, 0.2, 0);
		fx0.setBounds(0.2, 0.5, 1);
		fx0.setBounds(0.5, 0.7, 2);

		model.setFactor(X0, fx0);

		TIntIntHashMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence be = new BinarizeEvidence();
		int evidence = be.executeInline(model, observation, 2, false);

		Inference inf = new Inference();
		IntervalFactor ifact = inf.query(model, X0, evidence);

		assertArrayEquals(new double[] { 0.21212121212087243, 0.23529411764741176, 0.277777777778395 }, ifact.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.4117647058825294, 0.48484848484822773, 0.48275862069158587 }, ifact.getUpper(), 0.001);
	}

	@Test
	public void targetFree2WithChild() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();
		int E = model.addVariable(2);
		int Xj = model.addVariable(2);
		int X0 = model.addVariable(2);

		BayesianFactor fe = new BayesianFactor(model.getDomain(E, Xj), false);
		fe.setData(new double[] { .3, .7, .6, .4 });
		model.setFactor(E, fe);

		BayesianFactor fx0 = new BayesianFactor(model.getDomain(X0), false);
		fx0.setData(new double[] { .3, .7 });
		model.setFactor(X0, fx0);

		IntervalFactor fj = new IntervalFactor(model.getDomain(Xj), model.getDomain(X0));
		fj.setBounds(.2, .6, 0, 0);
		fj.setBounds(.4, .8, 1, 0);
		fj.setBounds(.4, .7, 0, 1);
		fj.setBounds(.3, .6, 1, 1);
		model.setFactor(Xj, fj);

		TIntIntHashMap observation = ObservationBuilder.observe(E, 1);
		BinarizeEvidence be = new BinarizeEvidence();
		model = be.execute(model, observation, 2, false);
		int evidence = be.getLeafDummy();
		
		Inference inf = new Inference();
		IntervalFactor ifact = inf.query(model, X0, evidence);

		assertArrayEquals(new double[] { 0.24424778761090626, 0.6765799256506011 }, ifact.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.3234200743493989, 0.7557522123890937 }, ifact.getUpper(), 0.001);
	}

	@Test
	public void testDiamondBayesianPosteriorQuery() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(2);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);

		// root
		BayesianFactor f0 = new BayesianFactor(model.getDomain(n0, n1, n2), false);
		f0.setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.8, 0.2, 0.3, 0.7 });
		model.setFactor(n0, f0);

		BayesianFactor f3 = new BayesianFactor(model.getDomain(n3), false);
		f3.setData(new double[] { 0.3, 0.7 });
		model.setFactor(n3, f3);

		BayesianFactor f1 = new BayesianFactor(model.getDomain(n1, n3), false);
		f1.setData(new double[] { 0.4, 0.6, 0.5, 0.5 });
		model.setFactor(n1, f1);

		BayesianFactor f2 = new BayesianFactor(model.getDomain(n2, n3), false);
		f2.setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		model.setFactor(n2, f2);

		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence bin = new BinarizeEvidence();
		int ev = bin.executeInline(model, evidence, 2, false);

		Inference inference = new Inference();
		IntervalFactor factor = inference.query(model, n3, ev);

		assertArrayEquals(new double[] { 0.22188969645147497, 0.7781103035492434 }, factor.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.22188969645147497, 0.7781103035492434 }, factor.getUpper(), 0.001);
	}

	@Test
	public void testDiamondBayesianPosteriorQuery3() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(3);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);

		// root
		BayesianFactor f0 = new BayesianFactor(model.getDomain(n0, n1, n2), false);
		f0.setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.5, 0.5, 0.8, 0.2, 0.3, 0.7, 0.9, 0.1 });
		model.setFactor(n0, f0);

		IntervalFactor f3 = new IntervalFactor(model.getDomain(n3), model.getDomain());
		f3.setBounds(.3, .4, 0);
		f3.setBounds(.6, .7, 1);
		model.setFactor(n3, f3);

		IntervalFactor f1 = new IntervalFactor(model.getDomain(n1), model.getDomain(n3));
		f1.setBounds(.1, .6, 0, 0);
		f1.setBounds(.5, .8, 1, 0);
		f1.setBounds(.3, .8, 2, 0);
		f1.setBounds(.3, .7, 0, 1);
		f1.setBounds(.3, .5, 1, 1);
		f1.setBounds(.2, .4, 2, 1);
		model.setFactor(n1, f1);

		BayesianFactor f2 = new BayesianFactor(model.getDomain(n2, n3), false);
		f2.setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		model.setFactor(n2, f2);

		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence bin = new BinarizeEvidence();
		int ev = bin.executeInline(model, evidence, 2, false);

		Inference inference = new Inference();
		IntervalFactor factor = inference.query(model, n3, ev);

		assertArrayEquals(new double[] { 0.22050585639124004, 0.6383476227590834 }, factor.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.3616523772409166, 0.7794941436087599 }, factor.getUpper(), 0.001);

		inference = new Inference();
		
		RemoveBarren barren = new RemoveBarren();
		barren.execute(model, new int[] { n1 }, ObservationBuilder.vars(ev).states(1));
		// no need to update n1 as we use the sparse model
		factor = inference.query(model, n1, ev);

		assertArrayEquals(new double[] { 0.24827348066293425, 0.20153743315534500, 0.3076654443861050 }, factor.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.48011911017679076, 0.36128775834693705, 0.5276243093920449 }, factor.getUpper(), 0.001);
	}
	
	/**
	 * Same as {@link #testDiamondBayesianPosteriorQuery3()} but with parent for n3 
	 * @throws InterruptedException
	 */
	@Test
	public void testDiamondBayesianPosteriorQuery4() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();

		int n0 = model.addVariable(2);
		int n1 = model.addVariable(3);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);
		int n4 = model.addVariable(3);
	
		// root
		BayesianFactor f0 = new BayesianFactor(model.getDomain(n0, n1, n2), false);
		f0.setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.5, 0.5, 0.8, 0.2, 0.3, 0.7, 0.9, 0.1 });
		model.setFactor(n0, f0);

		IntervalFactor f3 = new IntervalFactor(model.getDomain(n3), model.getDomain(n4));
		f3.setBounds(.3, .4, 0, 0);
		f3.setBounds(.6, .7, 1, 0);
		f3.setBounds(.1, .4, 0, 1);
		f3.setBounds(.6, .9, 1, 1);
		f3.setBounds(.2, .5, 0, 2);
		f3.setBounds(.5, .8, 1, 2);
		model.setFactor(n3, f3);

		IntervalFactor f4 = new IntervalFactor(model.getDomain(n4), model.getDomain());
		f4.setBounds(.1, .6, 0);
		f4.setBounds(.3, .7, 1);
		f4.setBounds(.1, .4, 2);
		model.setFactor(n4, f4);

		IntervalFactor f1 = new IntervalFactor(model.getDomain(n1), model.getDomain(n3));
		f1.setBounds(.1, .6, 0, 0);
		f1.setBounds(.5, .8, 1, 0);
		f1.setBounds(.3, .8, 2, 0);
		f1.setBounds(.3, .7, 0, 1);
		f1.setBounds(.3, .5, 1, 1);
		f1.setBounds(.2, .4, 2, 1);
		model.setFactor(n1, f1);

		BayesianFactor f2 = new BayesianFactor(model.getDomain(n2, n3), false);
		f2.setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		model.setFactor(n2, f2);

		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(n0, 0);

		BinarizeEvidence bin = new BinarizeEvidence();
		int ev = bin.executeInline(model, evidence, 2, false);

		Inference inference = new Inference();
		IntervalFactor factor = inference.query(model, n3, ev);

		assertArrayEquals(new double[] { 0.09702615320599722, 0.5996234703482123 }, factor.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.4003765296517877, 0.9029738467940027 }, factor.getUpper(), 0.001);

		inference = new Inference();
		
		RemoveBarren barren = new RemoveBarren();
		barren.execute(model, new int[] { n1 }, ObservationBuilder.vars(ev).states(1));
		// no need to update n1 as we use the sparse model
		factor = inference.query(model, n1, ev);

		assertArrayEquals(new double[] { 0.23845184770432107, 0.1682985757886567, 0.28836654178948645 }, factor.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.5354735898541261, 0.3697586787464407, 0.5279955207164615 }, factor.getUpper(), 0.001);
	}
	
	
	
	/**
	 * Network with unconnected parts will work after barren
	 * 
	 * @throws InterruptedException
	 */
	@Test()
	public void testLeftOver() throws InterruptedException {
		SparseModel<GenericFactor> model = new SparseModel<>();
		int n = model.addVariable(3);
		int n0 = model.addVariable(2);
		int n1 = model.addVariable(3);
		int n2 = model.addVariable(2);
		int n3 = model.addVariable(2);
		int n4 = model.addVariable(3);
		int n5 = model.addVariable(3);
		
		// stray
		BayesianFactor fx = new BayesianFactor(model.getDomain(n), false);
		fx.setData(new double[] { 0.1, 0.3, 0.6 });
		model.setFactor(n, fx);
		
		IntervalFactor fy = new IntervalFactor(model.getDomain(n5), model.getDomain(n));
		fy.setBounds(.3, .4, 0, 0);
		fy.setBounds(.2, .7, 1, 0);
		fy.setBounds(.2, .5, 2, 0);
		fy.setBounds(.1, .4, 0, 1);
		fy.setBounds(.3, .9, 1, 1);
		fy.setBounds(.2, .5, 2, 1);
		fy.setBounds(.2, .5, 0, 2);
		fy.setBounds(.3, .8, 1, 2);
		fy.setBounds(.3, .8, 2, 2);
		model.setFactor(n5, fy);
		
		BayesianFactor f0 = new BayesianFactor(model.getDomain(n0, n1, n2), false);
		f0.setData(new double[] { 0.1, 0.9, 0.4, 0.6, 0.5, 0.5, 0.8, 0.2, 0.3, 0.7, 0.9, 0.1 });
		model.setFactor(n0, f0);

		IntervalFactor f3 = new IntervalFactor(model.getDomain(n3), model.getDomain(n4));
		f3.setBounds(.3, .4, 0, 0);
		f3.setBounds(.6, .7, 1, 0);
		f3.setBounds(.1, .4, 0, 1);
		f3.setBounds(.6, .9, 1, 1);
		f3.setBounds(.2, .5, 0, 2);
		f3.setBounds(.5, .8, 1, 2);
		model.setFactor(n3, f3);

		IntervalFactor f4 = new IntervalFactor(model.getDomain(n4), model.getDomain());
		f4.setBounds(.1, .6, 0);
		f4.setBounds(.3, .7, 1);
		f4.setBounds(.1, .4, 2);
		model.setFactor(n4, f4);

		IntervalFactor f1 = new IntervalFactor(model.getDomain(n1), model.getDomain(n3));
		f1.setBounds(.1, .6, 0, 0);
		f1.setBounds(.5, .8, 1, 0);
		f1.setBounds(.3, .8, 2, 0);
		f1.setBounds(.3, .7, 0, 1);
		f1.setBounds(.3, .5, 1, 1);
		f1.setBounds(.2, .4, 2, 1);
		model.setFactor(n1, f1);

		BayesianFactor f2 = new BayesianFactor(model.getDomain(n2, n3), false);
		f2.setData(new double[] { 0.7, 0.3, 0.1, 0.9 });
		model.setFactor(n2, f2);

		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(n0, 0);
	
		RemoveBarren barren = new RemoveBarren();
		model = barren.execute(model, new int[] { n3 }, evidence);
		
		BinarizeEvidence bin = new BinarizeEvidence();
		int ev = bin.executeInline(model, evidence, 2, false);
		
		
		Inference inference = new Inference();
		IntervalFactor factor = inference.query(model, n3, ev);

		assertArrayEquals(new double[] { 0.09702615320599722, 0.5996234703482123 }, factor.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.4003765296517877, 0.9029738467940027 }, factor.getUpper(), 0.001);

		
		inference = new Inference();
		
		// no need to update n1 as we use the sparse model
		factor = inference.query(model, n1, ev);

		assertArrayEquals(new double[] { 0.23845184770432107, 0.1682985757886567, 0.28836654178948645 }, factor.getLower(), 0.001);
		assertArrayEquals(new double[] { 0.5354735898541261, 0.3697586787464407, 0.5279955207164615 }, factor.getUpper(), 0.001);
	}
}
