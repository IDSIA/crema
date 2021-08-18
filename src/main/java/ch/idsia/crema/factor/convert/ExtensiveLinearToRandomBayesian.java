package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianLogFactor;
import ch.idsia.crema.factor.credal.linear.extensive.ExtensiveLinearFactor;
import ch.idsia.crema.solver.LinearSolver;
import ch.idsia.crema.solver.commons.Simplex;
import ch.idsia.crema.utility.RandomUtil;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import java.util.Random;

// FIXME rename
@SuppressWarnings("rawtypes")
public class ExtensiveLinearToRandomBayesian implements Converter<ExtensiveLinearFactor, BayesianFactor> {

	private final Random random = RandomUtil.getRandom();

	public BayesianFactor apply(ExtensiveLinearFactor s) {
		return apply(s, -1);
	}

	@Override
	public BayesianFactor apply(ExtensiveLinearFactor s, Integer var) {

		LinearSolver solver = new Simplex();
		solver.loadProblem(s, random.nextBoolean() ? GoalType.MINIMIZE : GoalType.MAXIMIZE);

		double[] rand = new double[s.getDomain().getCombinations()];
		for (int i = 0; i < rand.length; ++i)
			rand[i] = random.nextDouble();

		solver.solve(rand, 1.0);
		double[] vertex = solver.getVertex();

		return new BayesianLogFactor(s.getDomain(), vertex);
	}

	@Override
	public Class<ExtensiveLinearFactor> getSourceClass() {
		return ExtensiveLinearFactor.class;
	}

	@Override
	public Class<BayesianFactor> getTargetClass() {
		return BayesianFactor.class;
	}

}
