package ch.idsia.crema.model;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.change.DomainChange;


public interface GraphicalModel<F extends GenericFactor> extends Model<F> {

	public abstract void removeParent(int variable, int parent);

	public abstract void removeParent(int variable, int parent, DomainChange<F> change);

	public abstract void addParent(int variable, int parent);

	/**
	 * May NOT return NULL for valid variables!
	 * @param variable
	 * @return 
	 */
	public abstract int[] getParents(int variable);

	public abstract int[] getChildren(int variable);
	
	public F getFactor(int variable);
	
	public void setFactor(int variable, F factor);

	@Override
	public GraphicalModel<F> copy();
}