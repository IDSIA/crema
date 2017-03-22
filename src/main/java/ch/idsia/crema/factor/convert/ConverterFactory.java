package ch.idsia.crema.factor.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.math3.util.Pair;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.Converter;

/**
 * TODO should we use dependency injection?
 * 
 * @author davidhuber
 *
 */
public class ConverterFactory {

	public static ConverterFactory INSTANCE = new ConverterFactory();

	private HashMap<
		Pair<Class<?>, Class<?>>,
		List<Converter<?, ?>>
	> converters;

	
	private ConverterFactory() {
		converters = new HashMap<>();
		@SuppressWarnings("rawtypes")
		ServiceLoader<Converter> loader = ServiceLoader.loadInstalled(Converter.class);
		
		@SuppressWarnings("rawtypes")
		Iterator<Converter> iterator = loader.iterator();
		
		while(iterator.hasNext()) {
			register(iterator.next());
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

	@SuppressWarnings("unchecked")
	protected <Source extends GenericFactor, Target extends GenericFactor> Converter<Source, Target> getConverter(
			Class<Source> source, Class<Target> target) {
		Pair<Class<Source>, Class<Target>> map = new Pair<>(
				source, target);
		return (Converter<Source, Target>) converters.get(map);

	}

	public <Source extends GenericFactor, Target extends GenericFactor> Target convert(
			Source source, Class<Target> target, int var) {
		if (target.isInstance(source))
			return target.cast(source);

		@SuppressWarnings("unchecked")
		Class<Source> source_class = (Class<Source>) source.getClass();

		Converter<Source, Target> converter = getConverter(source_class, target);
		return converter.apply(source, var);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (List<Converter<?,?>> conv : converters.values()) {
			for(Converter<?, ?> c : conv) {
				builder.append(c.getClass().getSimpleName()).append("\n");
			}
		}
		return builder.toString();
	}
	
	
	public static void main(String[] args) {
		System.out.println(ConverterFactory.INSTANCE.toString());
			
	}
}
