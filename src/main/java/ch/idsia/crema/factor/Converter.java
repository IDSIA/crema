package ch.idsia.crema.factor;

import java.util.function.BiFunction;

/**
 * @param <S> source type
 * @param <T> target type
 */
public interface Converter<S, T> extends BiFunction<S, Integer, T> {

	@Override
	T apply(S s, Integer variable);

	/**
	 * Most converter do not rely on the information about the conditioning.
	 *
	 * @param s source object
	 * @return the converted object of type T
	 */
	default T apply(S s) {
		return apply(s, -1);
	}

	Class<T> getTargetClass();

	Class<S> getSourceClass();

}
