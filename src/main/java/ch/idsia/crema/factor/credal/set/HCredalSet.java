package ch.idsia.crema.factor.credal.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;

import ch.idsia.crema.core.Strides;

public class HCredalSet extends AbstractSet {
	private LinearConstraintSet problem;
	private ArrayList<LinearConstraint> constraints;
	
	public HCredalSet() {
		constraints = new ArrayList<>();
	}
	
	/**
	 * Create the set with no constraints and the specified domain.
	 * 
	 * @param domain
	 */
	public HCredalSet(Strides domain) {
		super(domain);
	}
	
	/**
	 * Initialize with all the data. Note that the passed list IS copied
	 * @param domain
	 * @param constraints
	 */
	public HCredalSet(Strides domain, Collection<LinearConstraint> constraints) {
		super(domain);
		this.constraints = new ArrayList<>(constraints);
	}
	
	/**
	 * Get the linear problem composed of all the linear constraints
	 * @return
	 */
	public LinearConstraintSet getLinearProblem() {
		if (problem == null) {
			problem = new LinearConstraintSet(constraints);
		}
		return problem;
	}

	/**
	 * Set all Constrints at once.
	 * 
	 * @param constraints
	 */
	public void setConstraints(List<LinearConstraint> constraints) {
		this.constraints = new ArrayList<>(constraints);
		this.problem = null;
	}
	
	/**
	 * Add a constraint over the domain of the set.
	 * 
	 * @param constraint
	 * @return
	 */
	public HCredalSet addConstraint(LinearConstraint constraint) {
		this.constraints.add(constraint);
		this.problem = null;
		return this;
	}

	@Override
	public HCredalSet copy() {
		HCredalSet set = new HCredalSet();
		set.setDomain(getDomain());
		set.setConstraints(new ArrayList<>(constraints));
		return set;
	}
}
