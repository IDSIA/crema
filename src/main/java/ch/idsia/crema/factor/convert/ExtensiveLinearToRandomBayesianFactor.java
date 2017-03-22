package ch.idsia.crema.factor.convert;

import java.util.Random;

import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.ExtensiveLinearFactor;
import ch.idsia.crema.model.Converter;
import ch.idsia.crema.solver.LinearSolver;
import ch.idsia.crema.solver.commons.Simplex;
// FIXME rename
public class ExtensiveLinearToRandomBayesianFactor implements Converter<ExtensiveLinearFactor, BayesianFactor> {
	private Random random = new Random();

	public BayesianFactor apply(ExtensiveLinearFactor s) {
		return apply(s, -1);
	}
	
	@Override
	public BayesianFactor apply(ExtensiveLinearFactor s, Integer var) {
		
		LinearSolver solver = new Simplex();
		solver.loadProblem(s, random.nextBoolean() ? GoalType.MINIMIZE : GoalType.MAXIMIZE);
		
		double[] rand = new double[s.getDomain().getCombinations()];
		for (int i = 0; i < rand.length; ++i) rand[i] = random.nextDouble();
		
		solver.solve(rand, 1.0);
		double[] vertex = solver.getVertex();
				
		return new BayesianFactor(s.getDomain(), vertex, true);
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
