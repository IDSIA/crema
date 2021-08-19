package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.algebra.bayesian.BayesianOperation;
import ch.idsia.crema.factor.algebra.bayesian.LogBayesianOperation;
import ch.idsia.crema.factor.algebra.bayesian.SimpleBayesianOperation;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import ch.idsia.crema.utility.RandomUtil;
import com.google.common.primitives.Doubles;

import java.util.Collection;
import java.util.stream.DoubleStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    15.04.2021 18:40
 */
public class BayesianFactorUtilities {

	public static BayesianFactor random(Strides left, Strides right) {
		double[][] data = new double[right.getCombinations()][];

		for (int i = 0; i < data.length; i++) {
			data[i] = RandomUtil.sampleNormalized(left.getCombinations());
		}

		return new BayesianDefaultFactor(left.concat(right), Doubles.concat(data));
	}

	/**
	 * <p>
	 * This generic implementation checks if the current factor is in log space and also the given one. If both are
	 * false, then a {@link SimpleBayesianOperation} algebra will be used and a {@link BayesianDefaultFactor} will be
	 * produced; otherwise a {@link LogBayesianOperation} algebra will be used, the second factor will be converted to
	 * a {@link BayesianLogFactor} (if needed) and a {@link BayesianLogFactor} will be returned.
	 * </p>
	 *
	 * <p>
	 * The {@link BayesianOperation#add(BayesianFactor, int, BayesianFactor, int)} method will be used.
	 * </p>
	 *
	 * @param one the factor on the left
	 * @param two the factor on the right to combine with
	 * @return a {@link BayesianLogFactor} if this factor works in log-space, otherwise a {@link BayesianDefaultFactor}
	 */
	public BayesianAbstractFactor addition(BayesianAbstractFactor one, BayesianAbstractFactor two) {
		final boolean oneIsLog = one.isLog();
		final boolean twoIsLog = two.isLog();
		final BayesianOperation<BayesianAbstractFactor> ops;

		if (!oneIsLog && !twoIsLog) {
			ops = new SimpleBayesianOperation<>();

			return one.combine(two, BayesianDefaultFactor::new, ops::add);
		} else {
			ops = new LogBayesianOperation<>();

			if (!one.isLog())
				one = new BayesianLogFactor(one);
			if (!two.isLog())
				two = new BayesianLogFactor(two);

			return one.combine(two, BayesianLogFactor::new, ops::add);
		}
	}

	/**
	 * Compute the KLDivergence between two {@link BayesianFactor}s.
	 *
	 * @param base
	 * @param approx
	 * @return
	 */
	public static double KLDivergence(BayesianFactor base, BayesianFactor approx) {
		IndexIterator it = approx.getDomain().getReorderedIterator(base.getDomain().getVariables());
		double kl = 0;
		for (int i = 0; i < base.getData().length; i++) {
			int j = it.next();
			double p = base.getValueAt(i);
			double q = approx.getValueAt(j);

			kl += p * (Math.log(p) - Math.log(q));
		}
		return kl;
	}

	// TODO: what to do with this method?
	public boolean isDeterministic(BayesianFactor f, int... given) {
		if (!DoubleStream.of(f.getData()).allMatch(x -> x == 0.0 || x == 1.0))
			return false;

		int[] left = ArraysUtil.difference(f.getDomain().getVariables(), given);

		for (int v : left) {
			f = f.marginalize(v);
		}

		// TODO: _all_ match?
		return DoubleStream.of(f.getData()).allMatch(x -> x == 1.0);
	}

	/**
	 * Combine this factor with the provided one and return the
	 * result as a new factor.
	 *
	 * @param factors
	 * @return
	 */
	public static BayesianDefaultFactor combineAll(BayesianDefaultFactor... factors) {
		if (factors.length < 1)
			throw new IllegalArgumentException("wrong number of factors");
		else if (factors.length == 1)
			return factors[0].copy();

		BayesianDefaultFactor out = factors[0];
		for (int i = 1; i < factors.length; i++) {
			out = out.combine(factors[i]);
		}
		return out;
	}

	public static BayesianDefaultFactor combineAll(Collection<BayesianDefaultFactor> factors) {
		return combineAll(factors.toArray(BayesianDefaultFactor[]::new));
	}

}
