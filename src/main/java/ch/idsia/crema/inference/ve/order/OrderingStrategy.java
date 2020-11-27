package ch.idsia.crema.inference.ve.order;

import java.util.function.Function;

import ch.idsia.crema.model.graphical.GraphicalModel;

/**
 * a Function that takes a model and returns an ordering sequence.
 * 
 * @author huber
 */
public interface OrderingStrategy extends Function<GraphicalModel<?>, int[]>{
	/**
	 * @param model - 
	 */
	@Override
	int[] apply(GraphicalModel<?> model);
	
}
