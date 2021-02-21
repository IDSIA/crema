package ch.idsia.crema.factor.symbolic.serialize;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.symbolic.CombinedFactor;
import ch.idsia.crema.factor.symbolic.DividedFactor;
import ch.idsia.crema.factor.symbolic.FilteredFactor;
import ch.idsia.crema.factor.symbolic.MarginalizedFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.model.Model;


public class MOD implements SolverSerializer {

    private HashMap<SymbolicFactor, String> factorname = new HashMap<>();

    private StringBuilder variables = new StringBuilder();

    private LinkedList<String> constraints = new LinkedList<>();

    private String getName(SymbolicFactor factor, int offset) {
        String name;
        if (!factorname.containsKey(factor)) {
            name = "f" + factorname.size();
            factorname.put(factor, name);
        } else { 
            name = factorname.get(factor);
        }

        return name + "o" + offset;
    }


    private void dispatch(SymbolicFactor factor) {
        if (factor instanceof PriorFactor) {
            processPriorFactor((PriorFactor) factor);
        } else if (factor instanceof CombinedFactor) {
            processCombinedFactor((CombinedFactor) factor);
        } else if (factor instanceof MarginalizedFactor) {
            processMarginalizedFactor((MarginalizedFactor) factor);
        } else if (factor instanceof DividedFactor) {
            processDividedFactor((DividedFactor) factor);
        } else if (factor instanceof FilteredFactor) {
            processFilteredFactor((FilteredFactor) factor);
        }
    }

    private void processDividedFactor(DividedFactor factor) {
        
    }

    private void processFilteredFactor(FilteredFactor factor) {

    }

    private void processMarginalizedFactor(MarginalizedFactor factor) {

    }

    private void processCombinedFactor(CombinedFactor combined) {

    }  

    private void processPriorFactor(PriorFactor prior) {
        GenericFactor factor = prior.getFactor();
        if (factor instanceof IntervalFactor) { 
            IntervalFactor iFactor = (IntervalFactor) factor;
            iFactor.get
            constraints.add()

        }
    }

    @Override
    public String serialize(SymbolicFactor target, int state, boolean maximize) {
    
        LinkedList<SymbolicFactor> fifo = new LinkedList<>();
        fifo.addLast(target);
        while(!fifo.isEmpty()) {
            SymbolicFactor factor = fifo.pollFirst();
            fifo.addAll(Arrays.asList(factor.getSources()));
            dispatch(factor);
        }

        // actual collection we need
        Collections.reverse(constraints);
        return null;
    }
    
}
