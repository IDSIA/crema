package ch.idsia.crema.inference.ve;

import ch.idsia.crema.factor.FactorUtil;
import ch.idsia.crema.factor.OperableFactor;
import ch.idsia.crema.factor.algebra.FactorAlgebra;
import ch.idsia.crema.factor.algebra.Operation;
import ch.idsia.crema.inference.InferenceJoined;
import ch.idsia.crema.inference.ve.order.OrderingStrategy;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class VariableElimination<F extends OperableFactor<F>> implements InferenceJoined<GraphicalModel<F>, F> {

    private int[] sequence;
    private TIntIntMap order;

    private List<F> factors;

    private TIntIntMap instantiation;

    private Operation<F> operator;

    private boolean normalize = true;


    /**
     * Constructs a variable elimination specifying the algebra to be used for the
     * factors and the elimination order
     *
     * @param ops
     * @param sequence
     */
    public VariableElimination(int[] sequence) {
        setSequence(sequence);
        this.operator = new FactorAlgebra<>();
    }

    /**
     * Set the elimination sequence to be used. Variables will be eliminated in this order.
     * The sequence may include the query!
     * <p>Elimination sequencies can be generated with an {@link OrderingStrategy}.
     * </p>
     *
     * @param sequence
     */
    public void setSequence(int[] sequence) {
        order = new TIntIntHashMap();
        for (int i = 0; i < sequence.length; ++i) {
            order.put(sequence[i], i);
        }

        this.sequence = sequence;
    }

    /**
     * Populate the problem with the factors to be considered.
     * Collection version.
     *
     * @param factors
     */
    public void setFactors(Collection<? extends F> factors) {
        this.factors = new ArrayList<>(factors);
    }

    /**
     * Populate the problem with the factors to be considered.
     * Array version.
     *
     * @param factors
     */
    public void setFactors(F[] factors) {
        this.factors = Arrays.asList(factors);
    }

    /**
     * Fix some query states. The provided argument is a map of variable - state
     * associations.
     *
     * @param instantiation
     */
    public void setInstantiation(TIntIntMap instantiation) {
        this.instantiation = instantiation;
    }

	/** Alias of setInstantiation */
	public void setEvidence(TIntIntMap instantiation) {
        setInstantiation(instantiation);
    }

    /**
     * explicitly request normalization of the result
     * @param normalize
     */
    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    /**
     * Execute the variable elimination asking for the marginal or posterior of the specified
     * variables. If multiple variables are specified the joint over the query is computed.
     * <p>
     * <p>
     * The elimination sequence is to be specified via {@link VariableElimination#setSequence(int[])}.
     *
     * @param query
     * @return
     */
    public F run(int... query) {
        // variables should be sorted
        query = ArraysUtil.sort(query);

        FactorQueue<F> queue = new FactorQueue<>(sequence);
        queue.init(factors);
        boolean normalize = false;

        while (queue.hasNext()) {
            int variable = queue.getVariable();
            Collection<F> var_factors = queue.next();

            if (!var_factors.isEmpty()) {
                F last = FactorUtil.combine(operator, var_factors);
                if (instantiation != null && instantiation.containsKey(variable)) {
                    int state = instantiation.get(variable);
                    last = operator.filter(last, variable, state);
					//normalize = true;
                }
                if (Arrays.binarySearch(query, variable) >= 0) {
                    // query var // nothing to do
                } else {
                    last = operator.marginalize(last, variable);
                }
                queue.add(last);
            }
        }

        Collection<F> res = queue.getResults();
        F last = FactorUtil.combine(operator,res);
        
        if (this.normalize) {
            last = FactorUtil.normalize(operator, last);
        }
        
        return last;
    }


    private int[] union(int[] first, int[]... others) {
        TIntSet set = new TIntHashSet(first);
        for (int[] other : others) {
            set.addAll(other);
        }
        int[] data = set.toArray();
        Arrays.sort(data);
        return data;
    }


    
    // @Override
    // public F apply(GraphicalModel<F> model, int[] query, TIntIntMap assignement) throws InterruptedException {
    //     setInstantiation(assignement);
    //     setFactors(model.getFactors());
    //     query = union(query, assignement.keys());
    //     return run(query);
    // }

 

	@Override
	public F query(GraphicalModel<F> model, TIntIntMap evidence, int query) {
		return query(model, evidence, new int[]{ query });
	}

	@Override
	public F query(GraphicalModel<F> model, TIntIntMap observations, int... queries) {
		setInstantiation(observations);
		setFactors(model.getFactors());
		return run(queries);
	}


}
