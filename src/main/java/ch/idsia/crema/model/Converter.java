package ch.idsia.crema.model;

import java.util.function.BiFunction;

public interface Converter<From, To> extends BiFunction<From, Integer, To> {
	
	/**
	 * Convert the specified object to the target type based on the provided variable.
	 * If no variable is needed -1 can be provided. 
	 */
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
