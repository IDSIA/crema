package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.model.graphical.GraphicalModel;

public class DupModel<F extends Factor<F>> implements Transformer<GraphicalModel<F>> {

	@Override
	public GraphicalModel<F> execute(GraphicalModel<F> model) {
		return model.copy();
	}

}
