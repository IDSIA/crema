package ch.idsia.crema.inference.approxlp;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.SeparateLinearToExtensiveHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.ExtensiveLinearFactor;
import ch.idsia.crema.factor.credal.linear.SeparateLinearFactor;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.solver.LinearSolver;
import ch.idsia.crema.solver.commons.Simplex;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

/**
 * Marginal problem Objective Function. This class implements the evaluation of the moves 
 * for a Marginal Inference using the ApproxLP algorithm.
 * 
 * @author huber
 */
public class Marginal extends Manager {
	
	private static final double BAD = Double.NaN;
	private SeparateLinearToExtensiveHalfspaceFactor sep2ext = new SeparateLinearToExtensiveHalfspaceFactor();

	public Marginal(GraphicalModel<? extends GenericFactor> model, GoalType dir, int x0, int x0state) {
		super(model, dir, x0, x0state);
	}
	
	/**
	 * Internal creation of a solver used for the optimization step 
	 * of the move evaluation. The returned solver is a simple (non fractional)
	 * Apache commons math3 based Linear solver.
	 */
	protected LinearSolver createSolver(int free) {
		Simplex simplex = new Simplex();
		GenericFactor f = model.getFactor(free); 
		ExtensiveLinearFactor<?> factor = null;
		if (f instanceof SeparateLinearFactor) {
			factor = sep2ext.apply((SeparateLinearFactor<?>) f);
		} else if(f instanceof ExtensiveLinearFactor) {
			factor = (ExtensiveLinearFactor<?>) f;
		} else {
			return null;
		}
		simplex.loadProblem(factor, goal);
		return simplex;
	}
	
	@Override
	public double eval(Solution from, Move doing) {
		int free = doing.getFree();
		LinearSolver solver = createSolver(free);//linearProblems[free]; 
		// not a good move
		if (solver == null) return BAD;
		

		int[] parent = model.getParents(free);			
		double[] objective;
		BayesianFactor tmp = null;
		// int i = Arrays.binarySearch(parent, x0);
		// x0 is part of the parents of the free variable
		//		if (i >= 0) {
		//			// When performing a marginal query there is no evidence
		//			// and all descendants of the query are cut by the barren filter. 
		//			// This makes it impossible that the query has children and therefore
		//			// that there is any variable with the query (x0) as its parent!
		//			InlineBayesianFactor f = calcMarginal(from, parent);
		//			f = f.filter(x0, x0state);
		//			objective = f.getData();
		//			
		//		} else 
		if (free == x0) {
			BayesianFactor f = calcMarginal(from, parent);	

			// now we have some more unknowns (they will not play here but
			// we have to set them to zero)
			// we need to include x0's other states into the objective
			
			f = f.combine(getX0factor());
			objective = f.getData();
			
		} else {
			int[] target = ArraysUtil.addToSortedArray(parent, free);
			int[] all = ArraysUtil.addToSortedArray(target, x0);
			
			BayesianFactor f = replaceZerosInMarginal(calcMarginal(from, all));
			
			BayesianFactor p = f; 
			for (int aparent : ArraysUtil.removeAllFromSortedArray(all, parent)) {
				p = p.marginalize(aparent);
			}
			
			BayesianFactor norm = f;
			for (int atarget : ArraysUtil.removeAllFromSortedArray(all, target)) {
				norm = norm.marginalize(atarget);
			}
			
			f = f.divide(norm).combine(p);
			
			objective = f.filter(x0, x0state).getData();
			
		}		
//		try {
			solver.solve(objective, 0.0);
//		}catch (NoFeasibleSolutionException ex) {
//			System.out.println("ERRO" + Arrays.stream(tmp.getInteralData()).sum());
//		//	System.err.println(free + "  " + Arrays.toString(objective));	
//		}
		
		BayesianFactor solution = from.getData().get(free);
		solution = new BayesianFactor(solution.getDomain(), solution.isLog());
		solution.setData(solver.getVertex());
		
		doing.setValues(solution);
		doing.setScore(solver.getValue());
		
		fixNotMoving(from, doing);
		
		return doing.getScore();
	}
	
	
	@Override
	public double eval(Solution solution) {
		if (solution.getScore() != BAD) return solution.getScore();
		
		BayesianFactor marg = calcMarginal(solution, new int[] { x0 });
		double value = marg.getValue(x0state);
		solution.setScore(value);
		return value;
	}
}
