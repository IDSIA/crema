package ch.idsia.crema.model;

import java.util.function.BiFunction;

public interface Converter<From, To> extends BiFunction<From, Integer, To> {
	@Override
	public To apply(From s, Integer variable);
	
	/**
	 * Most converter do not rely on the information about the conditioning. For those 
	 * @param s
	 * @return
	 */
	default To apply(From s) {
		return apply(s, -1);
	}
	
	public Class<To> getTargetClass();
	
	public Class<From> getSourceClass();
}
