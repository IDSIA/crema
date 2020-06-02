package ch.idsia.crema.factor.convert;

import java.util.Random;

import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.SeparateLinearFactor;
import ch.idsia.crema.model.Converter;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.solver.LinearSolver;
import ch.idsia.crema.solver.SolverFactory;
import ch.idsia.crema.utility.ArraysUtil;

/**
 * <p>Converts a Separately Specified Linear Credal Factor ({@link SeparateLinearFactor})
 * to a {@link InlineBayesianFactor} by returning a vertex obtained using a random directio
 * in the linear optimization.</p>
 * 
 * <p>Vertices may have different probabilities of being returned. This is due to the 
 * use of a random direction for the optimization and not simply choosing a random point.
 * The latter method would, however, require enumeration of the vertices; operation that 
 * we would like to avoid.</p>
 * 
 * @author david
 *
 */
@SuppressWarnings("rawtypes")
public class SeparateLinearToRandomBayesian implements Converter<SeparateLinearFactor, BayesianFactor> {

	private Random random;
	private boolean log = false;
	
	public SeparateLinearToRandomBayesian() {
		random = new Random();
	}
	
	public SeparateLinearToRandomBayesian(int seed) {
		random = new Random(seed);
	}
	
	public BayesianFactor apply(SeparateLinearFactor s) {
		return apply(s,-1); // var is not needed to convert, but required by the converter ifce
	}
	
	@Override
	public BayesianFactor apply(SeparateLinearFactor s, Integer var) {

		
		LinearSolver solver = SolverFactory.getInstance();
		
		int[] new_vars = ArraysUtil.append(s.getDataDomain().getVariables(), s.getSeparatingDomain().getVariables());
		Strides target = s.getDomain();
		
		double[] result = new double[target.getCombinations()];
		
		for (int offset = 0; offset < s.getSeparatingDomain().getCombinations(); ++offset) {
			solver.loadProblem(s.getLinearProblemAt(offset), random.nextBoolean() ? GoalType.MINIMIZE : GoalType.MAXIMIZE);
			
			double[] rand = new double[s.getDataDomain().getCombinations()];
			for (int i = 0; i < rand.length; ++i) rand[i] = random.nextDouble();
			
			solver.solve(rand, 0.0);
			double[] vertex = solver.getVertex();
			
			System.arraycopy(vertex, 0, result, offset * rand.length, rand.length);
		}
		
		BayesianFactor factor = new BayesianFactor(target, log);
		
		// set data by giving the domain
		factor.setData(new_vars, result);
		
		return factor;
	}

	@Override
	public Class<BayesianFactor> getTargetClass() {
		return BayesianFactor.class;
	}

	@Override
	public Class<SeparateLinearFactor> getSourceClass() {
		return SeparateLinearFactor.class;
	}
}
