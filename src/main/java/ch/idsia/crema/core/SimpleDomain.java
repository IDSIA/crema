package ch.idsia.crema.core;

import java.util.Arrays;

/**
 * Domain implementation that does not include strides. You are likely to need {@link Strides} instead of this class.
 *
 * @author davidhuber
 */
public class SimpleDomain implements Domain {
	final private int[] variables;
	final private int[] sizes;
	final private int size;

	@Override
	public int getSize() {
		return size;
	}

	public SimpleDomain(Domain domain) {
		this.variables = domain.getVariables();
		this.sizes = domain.getSizes();
		size = variables.length;
	}

	public SimpleDomain(int[] variables, int[] sizes) {
		this.variables = variables;
		this.sizes = sizes;
		size = variables.length;
	}

	@Override
	public int indexOf(int variable) {
		return Arrays.indexOf(variables, variable);
	}

	@Override
	public boolean contains(int variable) {
		return indexOf(variable) >= 0;
	}

	@Override
	public int[] getVariables() {
		return variables;
	}

	@Override
	public int[] getSizes() {
		return sizes;
	}

	@Override
	public int getCardinality(int variable) {
		int offset = indexOf(variable);
		return sizes[offset];
	}

	@Override
	public int getSizeAt(int index) {
		return sizes[index];
	}

	@Override
	public void removed(int variable) {
		int index = -(indexOf(variable) + 1);
		for (; index < size; ++index) {
			--variables[index];
		}
	}

}
