package ch.idsia.crema.preprocess.creators;

import ch.idsia.crema.model.graphical.MixedModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.03.2021 18:43
 */
// TODO: this class is used only by BinarizeEvidence but maybe the preprocess is not the correct place to have it...
public interface CreateFactor {

	/**
	 * Used by the {@link BinarizeEvidence} in order to add a new node that collect all the evidence in a binary form.
	 *
	 * @param model the model to work with
	 * @param parents the list of parents to merge
	 * @return the index of the new node added with this method
	 */
	int create(MixedModel model, List<Instance> parents);

}
