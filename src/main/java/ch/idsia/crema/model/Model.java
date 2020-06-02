package ch.idsia.crema.model;

import ch.idsia.crema.factor.GenericFactor;

import java.util.Collection;

public interface Model<F extends GenericFactor> {

    /**
     * Make a copy of the mutable parts of the model.
     * This makes a deep copy of the model. Some unmutable parts are not copied though.
     * <p>
     * <P>The type of the returned object MUST be assignable to this type.</P>
     *
     * @return
     */
    Model<F> copy();

    /**
     * Get an instance of the domain for the specified variables of the
     * model. The variables must be known to the model. Unknown variables lead
     * to unspecified behaviour.
     *
     * @param variables
     * @return the domain
     */
    Strides getDomain(int... variables);

    /**
     * remove a specific state from a variable
     *
     * @param variable the variable to be modified
     */
    void addState(int variable);

    /**
     * remove a specific state from a variable
     *
     * @param variable the variable to be modified
     * @param state    the state to be removed
     */
    void removeState(int variable, int state);

    /**
     * The number of variable that compose the model.
     *
     * @return the number of variables in the model
     */
    int getVariablesCount();

    /**
     * Returns a sorted array of all the variables stored in the model.
     * The returned Variables must be sorted.
     * <p>
     * <p>Implementer must make sure that returned array could be modified and
     * such changes must not affect the network or previously return arrays</p>
     * <p>
     * XXX is the sorting of the result needed. RemoveBarren does need it to be sorted!
     *
     * @return
     */
    int[] getVariables();

    /**
     * Get the cardinality of a variable in the model.
     * <p>
     * <p>Asking the cardinality for an unknown variable has an unspecified result and might lead to
     * exceptions. Implementations will defined the actual behaviour and outcome of an illegal request.</p>
     *
     * @param variable the variable we are interested in
     * @return the size/cardinality of the variable
     */
    int getSize(int variable);

    /**
     * Get the cardinality of a number of variables.
     * All variables requested must be part of the model.
     *
     * @param variables the array (vararg) of variables we are interested in
     * @return an array of the same size of variables containing their size
     * @see {@link SimpleModel.getSize}
     */
    int[] getSizes(int... variables);

    /**
     * Remove a variable from the model. All variables and factors must update their indices
     *
     * @param variable
     */
    void removeVariable(int variable);

    /**
     * Add a new variable to the model. Added variables will be appended to the model and the index of the added
     * variable will be returned. Adding a variable will always return a value that is greater than any other variable in
     * the model.
     *
     * @param size - int the number of states in the variable
     * @return int - the label/index/id assigned to the variable
     */
    int addVariable(int size);

    /**
     * Specify all the factor of the model via a single array.
     * Factors must be ordered by variable. First factor is therefor
     * for first variable.
     *
     * @param factors
     */
    void setFactors(F[] factors);

    /**
     * @return all the factor of the model in a collection.
     *         Factors are ordered by variable. First factor is therefor
     *         for first variable.
     */
    Collection<F> getFactors();
}
