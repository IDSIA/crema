import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;


public class BayesianFactors {
    public static void main(String[] args) {

        // Create domain of 2 variables of sizes 2,3
        Domain d1 = DomainBuilder.var(0, 1).size(2, 3);

        // Some operations over a domain
        d1.getVariables();              // vector of variables (int[])
        d1.getSize();                   // number of variables
        d1.getCardinality(0);    // size of a specific variable
        d1.getSizeAt(0);


        // Crate a factor over the domain
        BayesianFactor f = new BayesianFactor(d1);  // P([0, 1])  ->  P([X|Y])

        f.getDomain();

        // Set the data (i.e., values) of the factor
        //                      x1   x2
        f.setData(new double[]{0.2, 0.8,   //y1
                0.5, 0.5,   //y2
                0.1, 0.9    //y3
        });

        f.marginalize(0).getData(); // = double[3] { 1.0, 1.0, 1.0 }


        ////// A factor can also be defined in the context of a specific model


        // Define the BN
        BayesianNetwork model = new BayesianNetwork();
        model.addVariables(2, 3);
        model.addParent(0, 1);

        // extract a domain with varaiables 0 and 1 in the BN
        Domain d2 = model.getDomain(0, 1);

        // Create an empty factor with that domain
        f = new BayesianFactor(model.getDomain(0, 1));


    }
}
//53

