package ch.idsia.crema.factor.symbolic;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.OperableFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    28.04.2021 15:04
 */
public interface SymbolicFactor extends OperableFactor<SymbolicFactor> {

	@Override
	Strides getDomain();

	@Override
	SymbolicFactor copy();

	SymbolicFactor combine(SymbolicFactor other);

	@Override
	SymbolicFactor marginalize(int variable);

	@Override
	SymbolicFactor filter(int variable, int state);

	SymbolicFactor divide(SymbolicFactor factor);

	SymbolicFactor[] getSources();

	@Override
	SymbolicFactor normalize(int... given);

}
