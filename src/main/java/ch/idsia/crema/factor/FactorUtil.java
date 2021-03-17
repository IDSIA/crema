package ch.idsia.crema.factor;

import ch.idsia.crema.model.math.Operation;
import ch.idsia.crema.utility.ArraysUtil;

import java.util.Collection;
import java.util.Iterator;

/**
 * A utility class with function to help with factors. These methods are not part
 * of the Factor API to avoid clogging.
 *
 * @author david
 */
public class FactorUtil {
	/**
	 * @param f factor to normalize
	 * @param over set of variables to keep
	 * @return a normalized {@link GenericFactor} over a set of its variable (may be empty)
	 */
	public static <F extends GenericFactor> F normalize(Operation<F> op, F f, int... over) {
		F div = marginal(op, f, over);
		return op.divide(f, div);
	}

	/**
	 * Normalize a Factor over a set of its variable (may be empty)
	 *
	 * @param f
	 * @param over
	 * @return
	 */
	public static <F extends OperableFactor<F>> F normalize(F f, int... over) {
		F div = f;
		for (int v : ArraysUtil.removeAllFromSortedArray(f.getDomain().getVariables(), over)) {
			div = div.marginalize(v);
		}
		return f.divide(div);
	}

	/**
	 * Combine a factor with a collection of other factors
	 */
	public static <F extends OperableFactor<F>> F combine(F first, Collection<F> others) {
		for (F other : others) {
			first = first.combine(other);
		}
		return first;
	}

	/**
	 * Combine a collection of factors
	 */
	public static <F extends OperableFactor<F>> F combine(Collection<F> factors) {
		Iterator<F> iterator = factors.iterator();
		F first = iterator.next();
		while (iterator.hasNext()) {
			first = first.combine(iterator.next());
		}
		return first;
	}

	/**
	 * Combine a factor with an array of other factors
	 */
	@SafeVarargs
	public static <F extends OperableFactor<F>> F combine(F first, F... others) {
		for (F other : others) {
			first = first.combine(other);
		}

		return first;
	}


	/**
	 * Combine an array of factors (One is mandatory)
	 *
	 * @param factors
	 * @return
	 */
	@SafeVarargs
	public static <F extends OperableFactor<F>> F combine(F... factors) {
		F first = factors[0];
		for (int i = 1; i < factors.length; ++i) {
			first = first.combine(factors[i]);
		}
		return first;
	}

	/**
	 * Combine a collection of factors using a custom operator
	 */
	public static <F extends GenericFactor> F combine(Operation<F> op, Collection<F> factors) {
		Iterator<F> iterator = factors.iterator();
		F first = iterator.next();
		while (iterator.hasNext()) {
			first = op.combine(first, iterator.next());
		}

		return first;
	}

	/**
	 * Combine a factor with an array of factors using an operation set.
	 */
	@SafeVarargs
	public static <F extends GenericFactor> F combine(Operation<F> op, F first, F... others) {
		for (F other : others) {
			first = op.combine(first, other);
		}
		return first;
	}

	/**
	 * Combine an array of factors using an operation set.
	 *
	 * @throws IndexOutOfBoundsException if factors is empty
	 */
	@SafeVarargs
	public static <F extends GenericFactor> F combine(Operation<F> op, F... factors) {
		F first = factors[0];
		for (int i = 1; i < factors.length; ++i) {
			first = op.combine(first, factors[i]);
		}
		return first;
	}

	/**
	 * return the marginal of a factors. Not this is a marginalization of all
	 * but the "over" variables
	 */
	public static <F extends OperableFactor<F>> F marginal(F factor, int... over) {
		for (int var : ArraysUtil.removeAllFromSortedArray(factor.getDomain().getVariables(), over)) {
			factor = factor.marginalize(var);
		}
		return factor;
	}

	/**
	 * marginalize multiple variables out of a factor
	 */
	public static <F extends OperableFactor<F>> F marginalize(F factor, int... vars) {
		for (int var : vars) {
			factor = factor.marginalize(var);
		}
		return factor;
	}


	/**
	 * return the marginal of a factors. Note this is a marginalization of all
	 * but the "over" variables
	 */
	public static <F extends GenericFactor> F marginal(Operation<F> op, F factor, int... over) {
		for (int var : ArraysUtil.removeAllFromSortedArray(factor.getDomain().getVariables(), over)) {
			factor = op.marginalize(factor, var);
		}
		return factor;
	}

	/**
	 * marginalize multiple variables out of a factor
	 */
	public static <F extends GenericFactor> F marginalize(Operation<F> op, F factor, int... vars) {
		for (int var : vars) {
			factor = op.marginalize(factor, var);
		}
		return factor;
	}

	/**
	 * <p>
	 * An alternative take on filtering some rows out of a factor. Note that
	 * factors do implement filter, but there is debate whether this is correct.
	 * </p>
	 *
	 * <p>
	 * The indicator is used as we cannot create and indicator factor without
	 * knowing the actual data type (F is generic).
	 * </p>
	 *
	 * @param factor
	 * @param indicator
	 * @return
	 */
	public static <F extends OperableFactor<F>> F filter(F factor, F indicator) {
		return factor.combine(indicator).marginalize(indicator.getDomain().getVariables()[0]);
	}

}
