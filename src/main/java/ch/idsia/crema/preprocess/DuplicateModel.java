package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;

public class DuplicateModel<F extends GenericFactor> implements TransformerModel<F, GraphicalModel<F>> {

	@Override
	public GraphicalModel<F> execute(GraphicalModel<F> model) {
		return model.copy();
	}

}
