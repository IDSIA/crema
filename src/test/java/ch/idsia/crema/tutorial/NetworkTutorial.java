package ch.idsia.crema.tutorial;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.junit.Test;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.symbolic.PriorFactor;
import ch.idsia.crema.factor.symbolic.SymbolicFactor;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.user.bayesian.CPT;

public class NetworkTutorial {
    @Test
    public void createSparseNetwork() {
        // [creating-sparse-model]
        SparseModel<BayesianFactor> model = 
            new SparseModel<BayesianFactor>();
            
        model.addVariable(2); // C
        model.addVariable(3); // A
        model.addVariable(2); // B   
        // [creating-sparse-model]
        

        // [creating-sparse-model-arcs]
        // add variable 1 and 2 as parents of variable 0
        model.addParents(0, 1, 2);
        // [creating-sparse-model-arcs]
        assertArrayEquals(new int[]{0}, model.getChildren(1));
        assertArrayEquals(new int[]{0}, model.getChildren(2));


        // [creating-sparse-model-remove]
        model.removeVariable(1);
        // [creating-sparse-model-remove]
        assertArrayEquals(new int[]{2}, model.getParents(0));

    }



    @Test
    public void createSparseDAG() {
        // [creating-sparse-dag-model]
        SparseDirectedAcyclicGraph model = 
            new SparseDirectedAcyclicGraph();
            
        model.addVariable(2); // C
        model.addVariable(3); // A
        model.addVariable(2); // B   
        // [creating-sparse-dag-model]
        

        // [creating-sparse-model-arcs]
        // add variable 1 and 2 as parents of variable 0
        model.addParents(0, 1, 2);
        // [creating-sparse-model-arcs]
        assertArrayEquals(new int[]{0}, model.getChildren(1));
        assertArrayEquals(new int[]{0}, model.getChildren(2));


        // [creating-sparse-model-remove]
        model.removeVariable(1);
        // [creating-sparse-model-remove]
        assertArrayEquals(new int[]{2}, model.getParents(0));

    }


    @Test
    public void createSymbolicNetwork() {
        SparseModel<SymbolicFactor> model = new SparseModel<>();
        int A = model.addVariable(2);
        int B = model.addVariable(2);
        int C = model.addVariable(2);

        model.addParent(A, C);
        model.addParent(B, C);
        
        BayesianFactor fac = new BayesianFactor(model.getDomain(A));
        BayesianFactor fbc = new BayesianFactor(model.getDomain(B));
        BayesianFactor fc  = new BayesianFactor(model.getDomain(C));
        
        // populate facors here

        PriorFactor pac = new PriorFactor(fac);
        PriorFactor pbc = new PriorFactor(fbc);
        PriorFactor pc = new PriorFactor(fc);

        model.setFactor(A, pac);
        model.setFactor(B, pbc);
        model.setFactor(C, pc);
        
        
    }
}
