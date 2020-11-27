package ch.idsia.crema.inference.approxlp2;

import java.util.Arrays;

import org.apache.commons.math3.optim.linear.NoFeasibleSolutionException;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.SeparateLinearToExtensiveHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.ExtensiveLinearFactor;
import ch.idsia.crema.factor.credal.linear.SeparateLinearFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.solver.LinearFractionalSolver;
import ch.idsia.crema.solver.commons.FractionalSolver;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;

public class Posterior extends Manager {

	//private static final double BAD = Double.NaN;
	private SeparateLinearToExtensiveHalfspaceFactor sep2ext = new SeparateLinearToExtensiveHalfspaceFactor();

	private TIntIntMap evidence;
	
	public Posterior(GraphicalModel<? extends GenericFactor> model, GoalType dir, int x0, int x0state, TIntIntMap evidence) {
		super(model, dir, x0, x0state);
		this.evidence = evidence;
	}

	protected LinearFractionalSolver createSolver(int free) {
		FractionalSolver simplex = new FractionalSolver();
		GenericFactor f = model.getFactor(free);
		ExtensiveLinearFactor<?> factor = null;
		if (f instanceof SeparateLinearFactor) {
			factor = sep2ext.apply((SeparateLinearFactor<?>) f);
		} else if (f instanceof ExtensiveLinearFactor) {
			factor = (ExtensiveLinearFactor<?>) f;
		} else {
			return null;
		}
		simplex.loadProblem(factor, goal);
		return simplex;
	}

	
	double computeApproxPXe(BayesianFactor joint, int free, int query, int[] parents) {
		joint = joint.marginalize(query).marginalize(free);
		for (int parent : parents) {
			joint = joint.marginalize(parent);
		}
		return joint.getValue(1);
	}
	
	/**
	 * Evaluate the value for a move and get the new vertex
	 * <p>
	 * <img src="data:image/gif;base64,R0lGODlhkAE9AOfQAAAAAAICAgMDAwQEBAYGBgcHBwgICAkJCQ0NDQ4ODhAQEBISEhcXFxgYGBsbGxwcHB0dHR8fHyAgICIiIiMjIyQkJCUlJScnJygoKCkpKSsrKy0tLS8vLzIyMjMzMzU1NTg4ODo6Ojs7Ozw8PD09PT4+Pj8/P0BAQENDQ0REREVFRUZGRkdHR0hISEpKSktLS0xMTE5OTk9PT1BQUFFRUVJSUlVVVVZWVldXV1hYWFpaWltbW1xcXF5eXl9fX2BgYGFhYWNjY2RkZGVlZWZmZmdnZ2hoaGpqamtra2xsbG5ubm9vb3BwcHFxcXJycnNzc3R0dHV1dXZ2dnd3d3h4eHl5eXp6ent7e3x8fH19fX9/f4CAgIGBgYKCgoODg4SEhIWFhYeHh4mJiYqKiouLi4yMjI2NjY6Ojo+Pj5GRkZKSkpOTk5SUlJWVlZaWlpeXl5iYmJmZmZqampubm5ycnJ2dnZ6enp+fn6CgoKGhoaKioqOjo6SkpKWlpaampqenp6mpqaqqqqurq6ysrK2tra6urq+vr7CwsLGxsbKysrOzs7S0tLW1tba2tre3t7i4uLq6uru7u7y8vL29vb6+vr+/v8DAwMHBwcLCwsPDw8TExMbGxsfHx8jIyMrKysvLy8zMzM3Nzc7Ozs/Pz9DQ0NHR0dLS0tPT09TU1NXV1dbW1tjY2Nra2tvb29zc3N3d3d7e3t/f3+Dg4OHh4eLi4uPj4+Tk5OXl5ebm5ufn5+jo6Onp6erq6uzs7O3t7e7u7u/v7/Dw8PHx8fLy8vPz8/T09PX19fb29vf39/j4+Pn5+fr6+vv7+/z8/P39/f7+/v///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////yH5BAEKAP8ALAAAAACQAT0AAAj+AKEJHEiwoMGDCBMqXMiwocOHECNKnEixosWLGDNq3Mixo8ePIEOKHEmypMmTKFOqXMmypcuXMGPKnEmzps2bOHPq3Mmzp8+fQIMKHUq0qNGjSJMqXcq0qdOnUKNKnUq1qtWB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"/>
	 * </p>
	 */
	@Override
	public double eval(Solution from, Move doing) {

		int free = doing.getFree();
		int[] parents = model.getParents(free);
		int pindex = Arrays.binarySearch(parents, x0);

		// long targetObs = Observations.make(targetVaribale, targetState);
		// long dummyObs = Observations.make(dummy, 1);

		double[] numerator;
		double[] denominator;

		// note that observed nodes have their outbound links cut before being
		// binarized.
		
		// parents of free contain the query and the free var is observed
		if (pindex >= 0 && evidence.containsKey(free)) {
			// x0 (the query) is among the parent of the free variable

			// prepare the full domain by first adding the free var to its
			// parents
			int[] xjpj = ArraysUtil.addToSortedArray(parents, free);
			
			BayesianFactor p_x0xexjpj = calcPosterior(from, xjpj, evidence);
			BayesianFactor p_xjpj = calcMarginal(from, xjpj);
			
			BayesianFactor p_x0xe_xjpj = p_x0xexjpj.divide(p_xjpj);
			BayesianFactor p_pj = p_xjpj.marginalize(free);
			BayesianFactor p_num = p_x0xe_xjpj.combine(p_pj);

			// we need to filter the target variable
			numerator = p_num.filter(x0, x0state).combine(getX0factor()).getData();
			
			// no need to filter (the sum will elimintate x0)
			BayesianFactor p_denom = p_x0xexjpj.combine(p_pj);
			denominator = p_denom.getData();
			
		} else if (pindex >= 0) {
			// x0 (the query) is among the parent of the free variable

			// prepare the full domain by first adding the free var to its
			// parents
			int[] xjpj = ArraysUtil.addToSortedArray(parents, free);
			
			BayesianFactor p_x0xexjpj = calcPosterior(from, xjpj, evidence);
			BayesianFactor p_xjpj = calcMarginal(from, xjpj);
			
			BayesianFactor p_x0xe_xjpj = p_x0xexjpj.divide(p_xjpj);
			BayesianFactor p_pj = p_xjpj.marginalize(free);
			BayesianFactor p_num = p_x0xe_xjpj.combine(p_pj);

			// we need to filter the target variable
			numerator = p_num.filter(x0, x0state).combine(getX0factor()).getData();
			
			// no need to filter (the sum will elimintate x0)
			BayesianFactor p_denom = p_x0xexjpj.combine(p_pj);
			denominator = p_denom.getData();
			
		} else if (free == x0) { 
			
			int[] xjpj = ArraysUtil.addToSortedArray(parents, free); // == x0pj

			BayesianFactor p_xexjpj = calcPosterior(from, xjpj, evidence);
			BayesianFactor p_xjpj = calcMarginal(from, xjpj);
			BayesianFactor p_pj = p_xjpj.marginalize(free);
			BayesianFactor p_xe_xjpj = p_xexjpj.divide(p_xjpj);
			BayesianFactor p_num = p_xe_xjpj.combine(p_pj);
			
			numerator = p_num.combine(getX0factor()).getData();
		
			// filter evidence
			BayesianFactor p_denom = p_xexjpj.combine(p_pj);
			denominator = p_denom.getData();
			
		} else if(evidence.containsKey(free)) {
			// xj is observed
			int[] xjpj = ArraysUtil.addToSortedArray(parents, free);
			int[] all = ArraysUtil.addToSortedArray(xjpj, x0);
			
			// num = [P(x0xE|xj,pj) * P(xE|xj,pj) * P(pj)]

			BayesianFactor p_x0xex0pj = calcPosterior(from, all, evidence);
			BayesianFactor p_xjpj = calcMarginal(from, xjpj);
			BayesianFactor p_pj = p_xjpj.marginalize(free);
			
			BayesianFactor p_x0xe_xjpj = p_x0xex0pj.divide(p_xjpj);
			BayesianFactor p_num = p_x0xe_xjpj.combine(p_pj).filter(x0, x0state);
			
			numerator = p_num.getData();
			
			// P(xE|xj,pj) * P(pj)
			BayesianFactor p_denom = p_x0xe_xjpj.marginalize(x0).combine(p_pj);
			denominator = p_denom.getData();

			
		} else {
			int[] xjpj = ArraysUtil.addToSortedArray(parents, free);
			int[] all = ArraysUtil.addToSortedArray(xjpj, x0);
			
			// num = [P(x0xE|xj,pj) * P(xE|xj,pj) * P(pj)]

			BayesianFactor p_x0xex0pj = calcPosterior(from, all, evidence);
			BayesianFactor p_xjpj = calcMarginal(from, xjpj);
			BayesianFactor p_pj = p_xjpj.marginalize(free);
			
			BayesianFactor p_x0xe_xjpj = p_x0xex0pj.divide(p_xjpj);
			BayesianFactor p_num = p_x0xe_xjpj.combine(p_pj).filter(x0, x0state);
			
			numerator = p_num.getData();
			
			// P(xE|xj,pj) * P(pj)
			BayesianFactor p_denom = p_x0xe_xjpj.marginalize(x0).combine(p_pj);
			denominator = p_denom.getData();
		}
				
		LinearFractionalSolver solver = createSolver(free);
		try {
			solver.solve(numerator, 0.0, denominator, 0.0);
		} catch (NoFeasibleSolutionException ex) {
			throw ex;
		}
		
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
		
		if (!Double.isNaN(solution.getScore()))
			return solution.getScore();
		
		BayesianFactor marg = calcPosterior(solution, new int[] { x0 }, evidence);
		double value = marg.getValue(x0state);
		solution.setScore(value);
		return value;
	}
}
