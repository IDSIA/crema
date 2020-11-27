package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.Model;
import ch.idsia.crema.model.change.DomainChange;

// FIXME: #removeParent should accept a lambda
public interface GraphicalModel<F extends GenericFactor> extends Model<F> {

	void removeParent(int variable, int parent);

	void removeParent(int variable, int parent, DomainChange<F> change);

	void addParent(int variable, int parent);

	/**
	 * May NOT return NULL for valid variables!
	 * @param variable
	 * @return 
	 */
	int[] getParents(int variable);

	int[] getChildren(int variable);

	int[] getRoots();

	int[] getLeaves();

	F getFactor(int variable);
	
	void setFactor(int variable, F factor);

	@Override
	GraphicalModel<F> copy();

	default void addParents(int k, int[] parent){
		for (int p : parent) {
			addParent(k, p);
		}
	}
}