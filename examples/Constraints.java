package examples;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactorFactory;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import org.apache.commons.math3.optim.linear.Relationship;
public class Constraints {
    public static void main(String[] args) {

        double p = 0.2;

        // Define the model
        DAGModel<SeparateHalfspaceFactor> model = new DAGModel<>();
        int u = model.addVariable(3);

        SeparateHalfspaceFactor fu_constr = SeparateHalfspaceFactorFactory.factory().domain(model.getDomain(u), Strides.empty())

                // double[] data, Relationship rel, double value, int... states
                .constraint(new double[]{1., 1., 0.,}, Relationship.EQ, p)
                .constraint(new double[]{0., 0., 1.,}, Relationship.EQ, 1 - p)

                // normalization constraint
                .constraint(new double[]{1., 1., 1.,}, Relationship.EQ, 1)

                // positive constraints
                .constraint(new double[]{1., 0., 0.,}, Relationship.GEQ, 0)
                .constraint(new double[]{0., 1., 0.,}, Relationship.GEQ, 0)
                .constraint(new double[]{0., 0., 1.,}, Relationship.GEQ, 0)
                .get();


        HalfspaceToVertex conversor = new HalfspaceToVertex();

        VertexFactor fu_vert = conversor.apply(fu_constr,0);

        System.out.println(fu_vert);

    }

}
//41