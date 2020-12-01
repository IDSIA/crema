package ch.idsia.crema.inference.ve.order;

import ch.idsia.crema.model.graphical.GraphicalModel;

import java.util.function.Function;

/**
 * a Function that takes a model and returns an ordering sequence.
 *
 * @author huber
 */
public interface OrderingStrategy extends Function<GraphicalModel<?>, int[]> {

	/**
	 * @param model -
	 */
	@Override
	int[] apply(GraphicalModel<?> model);

}
