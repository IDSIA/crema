import ch.idsia.credici.model.StructuralCausalModel;

public class CounterFactuals {
    public static void main(String[] args) {

        int x=0, y=1, u=2;
        StructuralCausalModel cmodel = new StructuralCausalModel();

        // add the variables
        cmodel.addVariables(2,2);
        cmodel.addVariable(2,true);
        // set the arcs
        cmodel.addParent(x,u);
        cmodel.addParent(y,u);
        cmodel.addParent(y,x);
        // fill the CPTs
        cmodel.fillWithRandomFactors(2);

        // Create some intervened models
        StructuralCausalModel[] models = {cmodel.intervention(0,0), cmodel.intervention(1,1)};

        // Create the counterfactual model by merging
        StructuralCausalModel cfmodel = cmodel.merge(models);

        // get variable x but in world 1
        cfmodel.getMap().getEquivalentVars(1,x);

        System.out.println("Original model:\n====================== "+cmodel);
        System.out.println("Counterfactual model:\n====================== "+cfmodel);


    }
}
