package ch.idsia.crema.model.graphical;

import java.util.function.Function;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.Model;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;

// FIXME: #removeParent should accept a lambda
public interface GraphicalModel<F extends GenericFactor> extends Model<F> {
	/**
	 * Get the full domain associated with the specified variable. 
	 * This includes the variable and its parents.
	 * 
	 * @param variable the variable who's domain will be returned
	 * @return the variable and its parents as a Sorted Stride object
	 */
	Strides getFullDomain(int variable);
	
	/**
	 * Remove a variable's parents 
	 * 
	 * @param variable
	 * @param parent
	 */
	void removeParent(int variable, int parent);

	/**
	 * Remove a variable's parent indicating how to deal with the domain change
	 * @param variable
	 * @param parent
	 * @param change
	 */
	void removeParent(int variable, int parent, Function<F, F> change);

	
	void addParent(int variable, int parent);

	/**
	 * May NOT return NULL for valid variables!
	 *
	 * @param variable
	 * @return
	 */
	int[] getParents(int variable);
	IntSet getParentsSet(int variable);

	int[] getChildren(int variable);
	IntSet getChildrenSet(int variable);

	int[] getRoots();

	int[] getLeaves();

	F getFactor(int variable);

	default Int2ObjectMap<F> getFactorsMap() {
		Int2ObjectMap<F> map = new Int2ObjectOpenHashMap<>();
		for (int v : getVariables()) {
			map.put(v, getFactor(v));
		}
		return map;
	}

	void setFactor(int variable, F factor);

	@Override
	GraphicalModel<F> copy();

	default void addParents(int k, int[] parent) {
		for (int p : parent) {
			addParent(k, p);
		}
	}


}