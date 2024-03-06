package ch.idsia.crema.preprocess;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.preprocess.mergers.MergeFactorsBayesian;
import ch.idsia.crema.preprocess.mergers.MergeFactorsInterval;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    27.04.2021 14:19
 */
class MergeObservedTest {

	@Test
	void testMergeObservedBayesianMerge2of2() {
		DAGModel<BayesianFactor> model = new DAGModel<>();

		final int y = model.addVariable(4);
		final int x1 = model.addVariable(2);
		final int x2 = model.addVariable(2);

		model.addParent(x1, y);
		model.addParent(x2, y);

		final BayesianFactor fy = BayesianFactorFactory.factory().domain(model.getDomain(y))
				.data(new double[]{.2, .4, .4, .2})
				.get();

		final BayesianFactor fx1 = BayesianFactorFactory.factory().domain(model.getDomain(y, x1))
				.data(new double[]{.1, .2, .3, .4, .9, .8, .7, .6})
				.get();

		final BayesianFactor fx2 = BayesianFactorFactory.factory().domain(model.getDomain(y, x2))
				.data(new double[]{.9, .8, .7, .6, .1, .2, .3, .4})
				.get();

		model.setFactor(y, fy);
		model.setFactor(x1, fx1);
		model.setFactor(x2, fx2);

		final Int2IntMap obs = new Int2IntOpenHashMap();
		obs.put(x1, 0);
		obs.put(x2, 0);

		final MergeObserved<BayesianFactor> mo = new MergeObserved<>(new MergeFactorsBayesian());
		final DAGModel<BayesianFactor> merged = (DAGModel<BayesianFactor>) mo.execute(model, obs);

		System.out.println(merged);

		Assertions.assertEquals(2, merged.getVariables().length);
	}

	@Test
	void testMergeObservedIntervalMerge2of2() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		final int y = model.addVariable(4);
		final int x1 = model.addVariable(2);
		final int x2 = model.addVariable(2);

		model.addParent(x1, y);
		model.addParent(x2, y);

		final IntervalFactor fy = IntervalFactorFactory.factory().domain(model.getDomain(y), Strides.EMPTY)
				.lower(new double[]{.1, .3, .3, .1})
				.upper(new double[]{.2, .4, .4, .2})
				.get();

		final IntervalFactor fx1 = IntervalFactorFactory.factory().domain(model.getDomain(x1), model.getDomain(y))
				.lower(new double[]{.600, .375}, 0)
				.lower(new double[]{.750, .225}, 1)
				.lower(new double[]{.850, .125}, 2)
				.lower(new double[]{.950, .025}, 3)

				.upper(new double[]{.625, .400}, 0)
				.upper(new double[]{.775, .250}, 1)
				.upper(new double[]{.875, .150}, 2)
				.upper(new double[]{.975, .050}, 3)
				.get();

		final IntervalFactor fx2 = IntervalFactorFactory.factory().domain(model.getDomain(x2), model.getDomain(y))
				.lower(new double[]{.325, .650}, 0)
				.lower(new double[]{.600, .375}, 1)
				.lower(new double[]{.750, .225}, 2)
				.lower(new double[]{.850, .125}, 3)

				.upper(new double[]{.350, .675}, 0)
				.upper(new double[]{.625, .400}, 1)
				.upper(new double[]{.775, .250}, 2)
				.upper(new double[]{.875, .150}, 3)
				.get();

		model.setFactor(y, fy);
		model.setFactor(x1, fx1);
		model.setFactor(x2, fx2);

		final Int2IntMap obs = new Int2IntOpenHashMap();
		obs.put(x1, 0);
		obs.put(x2, 0);

		final MergeObserved<IntervalFactor> mo = new MergeObserved<>(new MergeFactorsInterval());
		final DAGModel<IntervalFactor> merged = (DAGModel<IntervalFactor>) mo.execute(model, obs);

		System.out.println(merged);

		Assertions.assertEquals(2, merged.getVariables().length);
	}

	@Test
	void testMergeObservedIntervalMerge2of3() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		final int y = model.addVariable(4);
		final int x1 = model.addVariable(2);
		final int x2 = model.addVariable(2);
		final int x3 = model.addVariable(2);

		model.addParent(x1, y);
		model.addParent(x2, y);
		model.addParent(x3, y);

		final IntervalFactor fy = IntervalFactorFactory.factory().domain(model.getDomain(y), Strides.EMPTY)
				.lower(new double[]{.1, .3, .3, .1})
				.upper(new double[]{.2, .4, .4, .2})
				.get();

		final IntervalFactor fx1 = IntervalFactorFactory.factory().domain(model.getDomain(x1), model.getDomain(y))
				.lower(new double[]{.600, .375}, 0)
				.lower(new double[]{.750, .225}, 1)
				.lower(new double[]{.850, .125}, 2)
				.lower(new double[]{.950, .025}, 3)

				.upper(new double[]{.625, .400}, 0)
				.upper(new double[]{.775, .250}, 1)
				.upper(new double[]{.875, .150}, 2)
				.upper(new double[]{.975, .050}, 3)
				.get();

		final IntervalFactor fx2 = IntervalFactorFactory.factory().domain(model.getDomain(x2), model.getDomain(y))
				.lower(new double[]{.325, .650}, 0)
				.lower(new double[]{.600, .375}, 1)
				.lower(new double[]{.750, .225}, 2)
				.lower(new double[]{.850, .125}, 3)

				.upper(new double[]{.350, .675}, 0)
				.upper(new double[]{.625, .400}, 1)
				.upper(new double[]{.775, .250}, 2)
				.upper(new double[]{.875, .150}, 3)
				.get();

		final IntervalFactor fx3 = IntervalFactorFactory.factory().domain(model.getDomain(x3), model.getDomain(y))
				.lower(new double[]{.225, .750}, 0)
				.lower(new double[]{.325, .650}, 1)
				.lower(new double[]{.600, .375}, 2)
				.lower(new double[]{.750, .225}, 3)

				.upper(new double[]{.250, .775}, 0)
				.upper(new double[]{.350, .675}, 1)
				.upper(new double[]{.625, .400}, 2)
				.upper(new double[]{.775, .250}, 3)
				.get();

		model.setFactor(y, fy);
		model.setFactor(x1, fx1);
		model.setFactor(x2, fx2);
		model.setFactor(x3, fx3);

		final Int2IntMap obs = new Int2IntOpenHashMap();
		obs.put(x1, 0);
		obs.put(x2, 0);

		final MergeObserved<IntervalFactor> mo = new MergeObserved<>(new MergeFactorsInterval());
		final DAGModel<IntervalFactor> merged = (DAGModel<IntervalFactor>) mo.execute(model, obs);

		System.out.println(merged);

		Assertions.assertEquals(3, merged.getVariables().length);
	}

}