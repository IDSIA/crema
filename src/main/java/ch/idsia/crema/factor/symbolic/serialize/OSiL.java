package ch.idsia.crema.factor.symbolic.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ch.idsia.crema.factor.symbolic.CombinedFactor;
import ch.idsia.crema.factor.symbolic.DividedFactor;
import ch.idsia.crema.factor.symbolic.MarginalizedFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.Serializer;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;

public class OSiL implements Serializer  {
    /** internal variable names */
    private HashMap<SymbolicFactor, String> names; 
    
    private String name(SymbolicFactor factor) {    
        if (!names.containsKey(factor)) {
            String myname = "I" + names.size();
            names.put(factor, myname);
        }
        return names.get(factor);
    }


    //private String variables()

	@Override
	public String serialize(SymbolicFactor factor) {
        names = new HashMap<>();
        
        List<PriorFactor> priors = new ArrayList<>(); 
        LinkedList<SymbolicFactor> open = new LinkedList<>(); 
        Set<SymbolicFactor> closed = new HashSet<>(); 
        
        open.add(factor);
        while (!open.isEmpty()) {
            SymbolicFactor next = open.pop();
            if (closed.contains(next)) continue;

            if (next instanceof MarginalizedFactor) {
                MarginalizedFactor marginalizedFactor = (MarginalizedFactor) next;
                
                SymbolicFactor source = marginalizedFactor.getSource();
                int remsize = source.getDomain().getCardinality(marginalizedFactor.getVariable());

                source.getDomain().getReorderedIterator(  )
                for (int parameter = 0; parameter < marginalizedFactor.getDomain().getCombinations(); ++parameter) {

                }

                open.add(marginalizedFactor.getSource());
            } else if(next instanceof CombinedFactor) {
                CombinedFactor combinedFactor = (CombinedFactor) next;
                open.addAll(Arrays.asList(combinedFactor.getFactors()));
            } else if (next instanceof DividedFactor) {
                DividedFactor dividedFactor = (DividedFactor) next;
                open.addAll(Arrays.asList(dividedFactor.getFactors()));
            }

        }
		return null;
	} 

}