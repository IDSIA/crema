package ch.idsia.crema.factor.convert;

import ch.idsia.credo.entropy.MaximumEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.core.Converter;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

public class IntervalToBayesianMaxEntropy implements Converter<IntervalFactor, BayesianFactor> {
	
	@Override
	public BayesianFactor apply(IntervalFactor intervals, Integer var) {
		BayesianFactor bf = new BayesianFactor(intervals.getDomain(), true);
		double[] store = bf.getInteralData();
		
		int[] source_vars = ArraysUtil.append(intervals.getDataDomain().getVariables(), intervals.getSeparatingDomain().getVariables());
		IndexIterator iterator = bf.getDomain().getReorderedIterator(source_vars);
		MaximumEntropy entropy= new MaximumEntropy();
		
		for (int offset = 0; offset < intervals.getSeparatingDomain().getCombinations(); ++offset) {
			double[] lowers = intervals.getLowerAt(offset);
			double[] uppers = intervals.getUpperAt(offset);
			double[] entro = entropy.compute(lowers, uppers);
			
			for (double val : entro) {
				store[iterator.next()] = bf.log(val);
			}
		}
	
		return bf;
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
