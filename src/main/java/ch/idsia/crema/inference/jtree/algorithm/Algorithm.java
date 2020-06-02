package ch.idsia.crema.inference.jtree.algorithm;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    14.02.2018 10:04
 */
public interface Algorithm<F, R> {

	void setInput(F model);

	R getOutput();

	R exec();

}
