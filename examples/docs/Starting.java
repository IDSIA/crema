package examples.docs;

import ch.idsia.crema.core.ObservationBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.inference.ve.CredalVariableElimination;
import ch.idsia.crema.model.graphical.DAGModel;


public class Starting {
    public static void main(String[] args) {
        double p = 0.2;
        double eps = 0.0001;

        /*  CN defined with vertex Factor  */

        // Define the model (with vertex factors)
        DAGModel<VertexFactor> model = new DAGModel<>();
        int A = model.addVariable(3);
        int B = model.addVariable(2);
        model.addParent(B,A);

        // Define a credal set of the partent node
        VertexFactor fu = VertexFactorFactory.factory().domain(model.getDomain(A), Strides.empty())
                .addVertex(new double[]{0., 1-p, p})
                .addVertex(new double[]{1-p, 0., p})
                .get();

        model.setFactor(A,fu);


        // Define the credal set of the child
        VertexFactor fx = VertexFactorFactory.factory().domain(model.getDomain(B), model.getDomain(A))
                .addVertex(new double[]{1., 0.,}, 0)
                .addVertex(new double[]{1., 0.,}, 1)
                .addVertex(new double[]{0., 1.,}, 2)
                .get();

        model.setFactor(B,fx);

        // Run exact inference
        CredalVariableElimination inf = new CredalVariableElimination();
        inf.query(model, ObservationBuilder.observe(B,0), A);

    }
}
