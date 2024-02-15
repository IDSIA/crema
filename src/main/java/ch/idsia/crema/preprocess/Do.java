package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import gnu.trove.map.TIntIntMap;

/**
 * Network surgery for do operations.
 * 
 * @param <F> type of the factor
 * @param <M> type of the Graphical Model
 */
public class Do <F extends OperableFactor<F>, M extends GraphicalModel<F>> 
implements ConverterEvidence<F, F, M, M> {
	
	public M execute(M model, TIntIntMap dos) {
		M copy = (M) model.copy();
		
		int[] keys = dos.keys();
		for (int key : keys) {
			
			// select the correct part of the factors by removing a child
			// via domain changer
			for (int child : model.getChildren(key)) {
				model.removeParent(child, key, (f) -> f.filter(key, dos.get(key)));
			}
			
			// completely remove the var now.
			model.removeVariable(key);
		}
		
		return copy;
	}
}
