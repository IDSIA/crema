package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.model.graphical.GenericSparseModel;
import com.google.common.primitives.Ints;

import java.io.IOException;


public abstract class NetUAIWriter<T extends GenericSparseModel> extends UAIWriter<T>{
    @Override
    protected void sanityChecks() {
        // Check model consistency
        if(!target.correctFactorDomains())
            throw new IllegalArgumentException("Inconsistent model");

    }


    protected void writeVariablesInfo() throws IOException {
        // Write the number of variables in the network
        println(target.getVariables().length);
        // Write the number of states of each variable
        println(target.getSizes(target.getVariables()));
    }

    protected void writeDomains() throws IOException {

        // Write the number of factors
        println(target.getFactors().size());

        // Add the factor domains with children at the end
        for(int v: target.getVariables()){
            int[] parents = target.getParents(v);
            print(parents.length+1);
            print(parents);
            println(v);

        }


    }

    protected abstract void writeFactors();

}
