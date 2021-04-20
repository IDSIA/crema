package ch.idsia.crema.factor.convert;

import ch.idsia.crema.entropy.MaximumEntropy;
import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

public class IntervalToBayesianMaxEntropy implements Converter<IntervalFactor, BayesianFactor> {

	@Override
	public BayesianFactor apply(IntervalFactor intervals, Integer var) {
		int[] source_vars = ArraysUtil.append(intervals.getDataDomain().getVariables(), intervals.getSeparatingDomain().getVariables());
		IndexIterator iterator = intervals.getDomain().getReorderedIterator(source_vars);
		MaximumEntropy entropy = new MaximumEntropy();

		double[] store = new double[intervals.getDomain().getCombinations()]; // TODO check if getCombinations() return the correct length of the array

		for (int offset = 0; offset < intervals.getSeparatingDomain().getCombinations(); ++offset) {
			double[] lowers = intervals.getLowerAt(offset);
			double[] uppers = intervals.getUpperAt(offset);
			double[] entro = entropy.compute(lowers, uppers);

			for (double val : entro) {
				store[iterator.next()] = val;
			}
		}

		return new BayesianLogFactor(intervals.getDomain(), store);
	}

	@Override
	public Class<BayesianFactor> getTargetClass() {
		return BayesianFactor.class;
	}

	@Override
	public Class<IntervalFactor> getSourceClass() {
		return IntervalFactor.class;
	}
}
