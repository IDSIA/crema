package ch.idsia.crema.core;

public class DomainBuilder extends SimpleDomain {

	private DomainBuilder(int[] vars, int[] sizes) {
		super(vars, sizes);
	}

	public static DomainBuilder var(int... vars) {
		return new DomainBuilder(vars, null);
	}

	public DomainBuilder size(int... sizes) {
		return new DomainBuilder(getVariables(), sizes);
	}

	public Strides strides() {
		return new Strides(getVariables(), getSizes());
	}

}
