package ch.idsia.crema.factor.credal.linear;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;

import ch.idsia.crema.model.Strides;

/**
 * A separately specified Credal factor that has a list of linear constrains for each 
 * separation.
 * 
 * @author huber
 *
 */
public class SeparateHalfspaceFactor extends SeparateFactor<SeparateHalfspaceFactor> implements SeparateLinearFactor<SeparateHalfspaceFactor> {
	
	private ArrayList<ArrayList<LinearConstraint>> data;
	
	public SeparateHalfspaceFactor(Strides content, Strides separation) {
		super(content, separation);
	
		data = new ArrayList<>(separation.getCombinations());
		for (int i = 0; i < separation.getCombinations(); i++) {
			data.add(new ArrayList<LinearConstraint>());
		}
	}

	public SeparateHalfspaceFactor(Strides content, Strides separation, ArrayList<ArrayList<LinearConstraint>> data) {
		super(content, separation);
		this.data = data;
	}
	
	@Override
	public SeparateHalfspaceFactor copy() {
		ArrayList<ArrayList<LinearConstraint>> new_data = new ArrayList<>(groupDomain.getCombinations());
		for (ArrayList<LinearConstraint> original : data) {
			ArrayList<LinearConstraint> new_matrix = new ArrayList<>(original.size());
			for (LinearConstraint original_row : original) {
				LinearConstraint copy = new LinearConstraint(original_row.getCoefficients().copy(), original_row.getRelationship(), original_row.getValue());
				new_matrix.add(copy);
			}
			new_data.add(new_matrix);
		}
		return new SeparateHalfspaceFactor(dataDomain, groupDomain, new_data);
	}

	public void addConstraint(double[] data, Relationship rel, double value, int... states) {
		int offset = groupDomain.getOffset(states);
		this.data.get(offset).add(new LinearConstraint(data, rel, value));
	}
	
	public double[] getRandomVertex(int... states) {
		Random random = new Random();
		int offset = groupDomain.getOffset(states);
		SimplexSolver solver = new SimplexSolver();
		
		double[] coeffs = new double[dataDomain.getCombinations()];
		for (int i = 0; i < dataDomain.getCombinations(); ++i) {
			coeffs[i] = random.nextDouble() + 1;
		}
		
		LinearObjectiveFunction c = new LinearObjectiveFunction(coeffs, 0);
		LinearConstraintSet Ab = new LinearConstraintSet(data.get(offset));
		
		PointValuePair pvp = solver.optimize(Ab, c);
		return pvp.getPointRef();
	}

	
	/** 
	 * Filter the factor by setting a specific variable to a state. This only works when the 
	 * var is in the grouping/separated part.
	 * 
	 * <p>Note that the constraits sets are not copied. So changing this factor will update
	 * also the filtered one.</p>
	 * 
	 * @param variable
	 * @param state
	 * @return
	 */
	@Override
	public SeparateHalfspaceFactor filter(int variable, int state) {
		int var_offset = groupDomain.indexOf(variable);
		int var_stride = groupDomain.getStrideAt(var_offset);
		int next_stride = groupDomain.getStrideAt(var_offset + 1);
		
		int state_offset = var_stride * state;
		int block_count = next_stride / var_stride;
		
		Strides new_domain = groupDomain.removeAt(var_offset);
		int new_size = new_domain.getCombinations();
		ArrayList<ArrayList<LinearConstraint>> new_constraints = new ArrayList<>(new_size);
		
		for (int i = 0; i < block_count; ++i) {
			int offset = i * next_stride + state_offset;
			new_constraints.addAll(data.subList(offset, offset + var_stride));
		}
		
		return new SeparateHalfspaceFactor(dataDomain, new_domain, new_constraints);
	}

	@Override
	public LinearConstraintSet getLinearProblem(int... states) {
		int offset = groupDomain.getOffset(states);
		return new LinearConstraintSet(data.get(offset));
	}
	
	@Override
	public LinearConstraintSet getLinearProblemAt(int offset) {
		return new LinearConstraintSet(data.get(offset));
	}
}