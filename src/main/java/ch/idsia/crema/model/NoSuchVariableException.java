package ch.idsia.crema.model;

import ch.idsia.crema.core.Domain;

import java.util.Arrays;

public class NoSuchVariableException extends RuntimeException {
	private static final long serialVersionUID = -1517007487763589611L;

	private final int var;
	private final Domain domain;

	public NoSuchVariableException(int var, Domain domain) {
		this.var = var;
		this.domain = domain;
	}

	@Override
	public String getMessage() {
		return var + " was not found in domain " + Arrays.toString(domain.getVariables());
	}

}
