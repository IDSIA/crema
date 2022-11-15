package ch.idsia.crema.utility;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.inference.ve.order.MinFillOrdering;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ProbabilityUtil {

	/**
	 * Computes the symmetrized KL distance between two probability distributions.
	 *
	 * @param p
	 * @param q
	 * @param zeroSafe
	 * @return
	 */
	public static double KLSymmetrized(double[] p, double[] q, boolean... zeroSafe) {
		return KLDistance(p, q, zeroSafe) + KLDistance(q, p, zeroSafe);
	}

	/**
	 * Computes the symmetrized KL distance between two bayesian factors.
	 *
	 * @param p
	 * @param q
	 * @param zeroSafe
	 * @return
	 */
	public static double KLSymmetrized(BayesianFactor p, BayesianFactor q, boolean... zeroSafe) {
		return KLSymmetrized(p.getData(), q.getData(), zeroSafe);
	}

	/**
	 * Computes the symmetrized KL distance between two groups of bayesian factors.
	 *
	 * @param p
	 * @param q
	 * @param zeroSafe
	 * @return
	 */
	public static double KLSymmetrized(TIntObjectMap<BayesianFactor> p, TIntObjectMap<BayesianFactor> q, boolean... zeroSafe) {
		double l = 0.0;
		for (int v : q.keys())
			l += KLSymmetrized(p.get(v), q.get(v), zeroSafe);

		return l;
	}

	/**
	 * Computes the symmetrized KL distance between p and q
	 *
	 * @param p
	 * @param q
	 * @param zeroSafe
	 * @return
	 */
	public static double KLDistance(double[] p, double[] q, boolean... zeroSafe) {
		if (zeroSafe.length > 1)
		    throw new IllegalArgumentException("Wrong number of arguments.");
		if (p.length != q.length)
		    throw new IllegalArgumentException("Arrays of different sizes.");
		if (zeroSafe.length == 0)
			zeroSafe = new boolean[]{false};

		double distance = 0;
		int n = p.length;
		for (int i = 0; i < n; i++) {
			if (p[i] != 0 || q[i] != 0) {
				if (p[i] == 0 && q[i] > 0) {
					if (!zeroSafe[0]) distance += Double.POSITIVE_INFINITY;
				} else if (!(p[i] > 0 && q[i] == 0)) {    // otherwise is not defined
					distance += p[i] * (Math.log(p[i]) - Math.log(q[i]));
				}
			}
		}
		return distance;
	}

	public static double KLDistance(BayesianFactor p, BayesianFactor q, boolean... zeroSafe) {
		return KLDistance(p.getData(), q.getData(), zeroSafe);
	}

	public static double KLDistance(TIntObjectMap<BayesianFactor> p, TIntObjectMap<BayesianFactor> q, boolean... zeroSafe) {
		double l = 0.0;
		for (int v : q.keys())
			l += KLDistance(p.get(v), q.get(v), zeroSafe);

		return l;
	}

	public static double infoLoss(double[] p1, double[] p2, double[] q, boolean... zeroSafe) {
		return KLDistance(p1, q, zeroSafe) + KLDistance(p2, q, zeroSafe);
	}

	private static TIntIntMap select(TIntIntMap d, int... keys) {
		TIntIntMap dnew = new TIntIntHashMap();
		for (int k : keys) {
			if (d.containsKey(k)) {
				dnew.put(k, d.get(k));
			}
		}
		return dnew;
	}

	private static TIntIntMap[] selectColumns(TIntIntMap[] data, int... keys) {
		return Arrays.stream(data).map(d -> select(d, keys)).toArray(TIntIntMap[]::new);
	}

	public static BayesianFactor getCounts(TIntIntMap[] data, Strides dom) {
		// sort the variables in the domain
		dom = dom.sort();

		BayesianFactorFactory counts = BayesianFactorFactory.factory();
		counts.domain(dom);

		int[] vars = dom.getVariables();

		for (int i = 0; i < dom.getCombinations(); i++) {

			int[] states = dom.statesOf(i);

			TIntIntMap assignament = new TIntIntHashMap();
			for (int j = 0; j < vars.length; j++)
				assignament.put(vars[j], states[j]);

			counts.set(Stream.of(data)
					.filter(d -> IntStream.of(vars).allMatch(v -> d.get(v) == assignament.get(v)))
					.count(), i);
		}
		return counts.get();
	}

	public static double likelihood(BayesianFactor prob, BayesianFactor emp, int counts) {
		if (!prob.getDomain().equals(emp.getDomain()))
			throw new IllegalArgumentException("Wrong domains");

		double L = 1.0;
		for (int i = 0; i < prob.getDomain().getCombinations(); i++)
			L *= Math.pow(prob.getValueAt(i), counts * emp.getValueAt(i));
		return L;
	}

	public static double likelihood(TIntObjectMap<BayesianFactor> prob, TIntObjectMap<BayesianFactor> emp, int counts) {
		double L = 1.0;
		for (int k : emp.keys())
			L *= likelihood(prob.get(k), emp.get(k), counts);

		return L;
	}

	public static double logLikelihood(BayesianFactor prob, BayesianFactor emp, int counts) {
		if (!prob.getDomain().equals(emp.getDomain()))
			throw new IllegalArgumentException("Wrong domains");

		double l = 0.0;
		for (int i = 0; i < prob.getDomain().getCombinations(); i++) {
			if (prob.getValueAt(i) == 0) {
				if (emp.getValueAt(i) != 0) l += Double.NEGATIVE_INFINITY;
			} else {
				l += counts * emp.getValueAt(i) * Math.log(prob.getValueAt(i));
			}
		}
		return l;
	}

	public static double logLikelihood(TIntObjectMap<BayesianFactor> prob, TIntObjectMap<BayesianFactor> emp, int counts) {
		double l = 0.0;
		for (int k : emp.keys())
			l += logLikelihood(prob.get(k), emp.get(k), counts);

		return l;
	}

	public static double logLikelihood(BayesianFactor prob, TIntIntMap[] data) {
		final Strides dom = prob.getDomain();
		final TIntIntMap[] D = selectColumns(data, dom.getVariables());
		final BayesianFactor counts = getCounts(D, dom);

		double llk = 0.0;

		for (int i = 0; i < dom.getCombinations(); i++) {
			int[] s = dom.statesOf(i);
			double c = counts.getValue(s);
			if (c > 0)
				llk += c * Math.log(prob.getValue(s));
		}

		return llk;
	}

	public static double logLikelihood(TIntObjectMap<BayesianFactor> prob, TIntIntMap[] data) {
		double l = 0.0;
		for (int k : prob.keys())
			l += logLikelihood(prob.get(k), data);

		return l;
	}

	public static double logLikelihood(GraphicalModel<BayesianFactor> model, TIntIntMap[] data) {
		final MinFillOrdering mfo = new MinFillOrdering();
		final int[] order = mfo.apply(model);

		final CutObserved<BayesianFactor> co = new CutObserved<>();
		final RemoveBarren<BayesianFactor> rm = new RemoveBarren<>();

		final VariableElimination<BayesianFactor> ve = new FactorVariableElimination<>(order);
		ve.setNormalize(false); // we are interested in P(e)

		double ll = 0;
		GraphicalModel<BayesianFactor> net;

		for (TIntIntMap obs : data) {
			net = co.execute(model, obs);
			net = rm.execute(net, obs, obs.keys());

			final BayesianFactor bf = ve.query(net, obs, obs.keys());

			ll += Math.log(bf.getData()[0]);
		}

		return ll;
	}

	public static double ratioLikelihood(BayesianFactor prob, BayesianFactor emp, int counts) {
		return logLikelihood(prob, emp, counts) / logLikelihood(emp, emp, counts);
	}

	public static double ratioLikelihood(TIntObjectMap<BayesianFactor> prob, TIntObjectMap<BayesianFactor> emp, int counts) {
		return likelihood(prob, emp, counts) / likelihood(emp, emp, counts);
	}

	public static double ratioLogLikelihood(BayesianFactor prob, BayesianFactor emp, int counts) {
		return logLikelihood(emp, emp, counts) / logLikelihood(prob, emp, counts);
	}

	public static double ratioLogLikelihood(TIntObjectMap<BayesianFactor> prob, TIntObjectMap<BayesianFactor> emp, int counts) {
		return logLikelihood(emp, emp, counts) / logLikelihood(prob, emp, counts);
	}

	public static double maxLogLikelihood(TIntObjectMap<BayesianFactor> emp, int counts) {
		return logLikelihood(emp, emp, counts);
	}

}
