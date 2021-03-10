package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.GenericFactor;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;

/**
 * TODO should we use dependency injection?
 *
 * @author davidhuber
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConverterFactory {
	public static ConverterFactory INSTANCE = new ConverterFactory();

	private final HashMap<Pair<Class<?>, Class<?>>, List<Converter<?, ?>>> converters;

	private ConverterFactory() {
		converters = new HashMap<>();
		ServiceLoader<Converter> loader = ServiceLoader.loadInstalled(Converter.class);

		for (Converter converter : loader) {
			register(converter);
		}

		register(new BayesianToExtensiveVertex());
		register(new SeparateLinearToExtensiveHalfspaceFactor());
		register(new SeparateLinearToHalfspaceFactor());
	}

	public void register(Converter<?, ?> converter) {
		Pair<Class<?>, Class<?>> key = new Pair<>(converter.getSourceClass(), converter.getTargetClass());
		List<Converter<?, ?>> data;
		if (!converters.containsKey(key)) {
			data = new ArrayList<>();
			converters.put(key, data);
		} else {
			data = converters.get(key);
		}
		data.add(converter);
	}

	/**
	 * @param source source type class
	 * @param target target type class
	 * @param <S>    source type
	 * @param <T>    target type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <S extends GenericFactor, T extends GenericFactor> Converter<S, T> getConverter(Class<S> source, Class<T> target) {
		final Pair<Class<S>, Class<T>> map = new Pair<>(source, target);

		if (!converters.containsKey(map))
			throw new UnsupportedOperationException("Could not find converter from " + source.getName() + " to " + target.getName());

		return (Converter<S, T>) converters.get(map);
	}

	/**
	 * @param source source type class
	 * @param target target type class
	 * @param var
	 * @param <S>    source type
	 * @param <T>    target type
	 * @return
	 */
	public <S extends GenericFactor, T extends GenericFactor> T convert(S source, Class<T> target, int var) {
		if (target.isInstance(source))
			return target.cast(source);

		Class<S> source_class = (Class<S>) source.getClass();

		Converter<S, T> converter = getConverter(source_class, target);
		return converter.apply(source, var);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (List<Converter<?, ?>> conv : converters.values()) {
			for (Converter<?, ?> c : conv) {
				builder.append(c.getClass().getSimpleName()).append("\n");
			}
		}
		return builder.toString();
	}

}
