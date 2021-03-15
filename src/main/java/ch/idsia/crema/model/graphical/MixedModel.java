package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.convert.ConverterFactory;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    10.03.2021 11:57
 */
public class MixedModel extends DAGModel<GenericFactor> {

	public MixedModel() {
	}

	public MixedModel(DAGModel<GenericFactor> original) {
		super(original);
	}

	@Override
	public MixedModel copy() {
		return new MixedModel(this);
	}

	public boolean checkSignature(Class<? extends GenericFactor>... types) {
		for (var type : types) {
			for (GenericFactor g : getFactors()) {
				// TODO: check if method is correct
				if (!g.getClass().isInstance(type))
					return false;
			}
		}

		return true;
	}

	public <T extends GenericFactor> T getConvertedFactor(Class<T> target, int variable) {
		final GenericFactor f = getFactor(variable);
		return ConverterFactory.INSTANCE.convert(f, target, variable);
	}

}
