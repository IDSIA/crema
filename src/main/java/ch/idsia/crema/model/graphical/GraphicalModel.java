package ch.idsia.crema.model.graphical;

import java.util.function.BiFunction;
import java.util.function.Function;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.Model;
import ch.idsia.crema.model.change.DomainChange;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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

	int[] getChildren(int variable);

	int[] getRoots();

	int[] getLeaves();

	F getFactor(int variable);

	default TIntObjectMap<F> getFactorsMap() {
		TIntObjectMap<F> map = new TIntObjectHashMap<>();
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