package ch.idsia.crema.core;

public class Variable implements Comparable<Variable> {
	private int label;
	private int cardinality;

	public Variable(int label, int cardinality) {
		this.label = label;
		this.cardinality = cardinality;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Variable) {
			return label == ((Variable) obj).label;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return label;
	}

	@Override
	public int compareTo(Variable o) {
		return Integer.compare(label, o.label);
	}

	public int getLabel() {
		return label;
	}
	
	public int getCardinality() {
		return cardinality;
	}
}
