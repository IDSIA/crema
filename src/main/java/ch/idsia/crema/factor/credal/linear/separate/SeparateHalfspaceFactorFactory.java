package ch.idsia.crema.factor.credal.linear.separate;

import ch.idsia.crema.core.Strides;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorUtils.buildConstraints;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    26.04.2021 17:23
 */
public class SeparateHalfspaceFactorFactory {

	private Strides dataDomain = Strides.empty();
	private Strides groupDomain = Strides.empty();

	private final Int2ObjectMap<List<LinearConstraint>> data = new Int2ObjectOpenHashMap<>();

	private SeparateHalfspaceFactorFactory() {
	}

	public static SeparateHalfspaceFactorFactory factory() {
		return new SeparateHalfspaceFactorFactory();
	}

	public SeparateHalfspaceFactorFactory domain(Strides content, Strides separation) {
		dataDomain(content);
		groupDomain(separation);
		return this;
	}

	public SeparateHalfspaceFactorFactory dataDomain(Strides dataDomain) {
		this.dataDomain = dataDomain;
		return this;
	}

	public SeparateHalfspaceFactorFactory content(Strides content) {
		return dataDomain(content);
	}

	public SeparateHalfspaceFactorFactory domain(Strides domain) {
		return dataDomain(dataDomain);
	}

	public SeparateHalfspaceFactorFactory left(Strides left) {
		return dataDomain(left);
	}

	public SeparateHalfspaceFactorFactory groupDomain(Strides groupDomain) {
		this.groupDomain = groupDomain;

		for (int i = 0; i < groupDomain.getCombinations(); i++)
			data.put(i, new ArrayList<>());

		return this;
	}

	public SeparateHalfspaceFactorFactory separation(Strides content) {
		return groupDomain(content);
	}

	public SeparateHalfspaceFactorFactory condition(Strides condition) {
		return groupDomain(condition);
	}

	public SeparateHalfspaceFactorFactory conditionDomain(Strides conditionDomain) {
		return groupDomain(conditionDomain);
	}

	public SeparateHalfspaceFactorFactory right(Strides right) {
		return groupDomain(right);
	}

	public SeparateHalfspaceFactorFactory constraint(LinearConstraint c, int... states) {
		int offset = groupDomain.getOffset(states);
		if (!data.containsKey(offset))
			data.put(offset, new ArrayList<>());
		data.get(offset).add(c);
		return this;
	}

	public SeparateHalfspaceFactorFactory constraint(double[] data, Relationship rel, double value, int... states) {
		constraint(new LinearConstraint(data, rel, value), states);
		return this;
	}

	public SeparateHalfspaceFactorFactory constraints(double[][] coefficients, double[] values, Relationship... rel) {
		// Build the constraints (including non-negative constraints)
		LinearConstraint[] C = buildConstraints(true, true, coefficients, values, rel);

		// add constraints to this factor
		for (LinearConstraint c : C) {
			constraint(c);
		}

		return this;
	}

	public SeparateHalfspaceFactorFactory constraints(boolean normalized, boolean nonnegative, Strides left, double[][] coefficients, double[] values, Relationship... rel) {
		LinearConstraint[] C = buildConstraints(normalized, nonnegative, coefficients, values, rel);

		// add constraints to this factor
		for (LinearConstraint c : C) {
			constraint(c);
		}
		return this;
	}

	public SeparateHalfspaceFactorFactory constraints(double[][][] coefficients, double[][] values, Relationship rel) {
		if (groupDomain.equals(Strides.empty()))
			throw new IllegalArgumentException("Cannot add constraints without a defined group-domain or separation");

		for (int i = 0; i < groupDomain.getCombinations(); i++) {
			// Build the constraints (including normalization and non-negative one)
			LinearConstraint[] C = buildConstraints(true, true, coefficients[i], values[i], rel);
			// add constraints to this factor
			for (LinearConstraint c : C) {
				constraint(c, i);
			}
		}
		return this;
	}

	public SeparateHalfspaceFactorFactory data(List<List<LinearConstraint>> data) {
		for (int i = 0; i < data.size(); i++) {
			this.data.put(i, data.get(i));
		}
		return this;
	}

	/**
	 * Modifies the linear problem at a given offset.
	 *
	 * @param offset
	 * @param constraints
	 */
	public void linearProblemAt(int offset, LinearConstraintSet constraints) {
		List<LinearConstraint> list = new ArrayList<>(constraints.getConstraints());
		data.put(offset, list);
	}

	/**
	 * Modifies the linear problem at a given offset.
	 *
	 * @param offset
	 * @param constraints
	 */
	public void linearProblemAt(int offset, Collection<LinearConstraint> constraints) {
		data.put(offset, (List<LinearConstraint>) constraints);
	}

	public SeparateHalfspaceFactor get() {
		return new SeparateHalfspaceDefaultFactor(dataDomain, groupDomain, data);
	}

}
