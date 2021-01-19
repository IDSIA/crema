import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Strides;
import org.apache.commons.math3.optim.linear.Relationship;

public class Constraints {
    public static void main(String[] args) {

        double p = 0.2;

        // Define the model
        SparseModel model = new SparseModel();
        int u = model.addVariable(3);

        SeparateHalfspaceFactor fu_constr = new SeparateHalfspaceFactor(model.getDomain(u), Strides.empty());


        // double[] data, Relationship rel, double value, int... states
        fu_constr.addConstraint(new double[]{1., 1., 0.,}, Relationship.EQ, p);
        fu_constr.addConstraint(new double[]{0., 0., 1.,}, Relationship.EQ, 1-p);

        // normalization constraint
        fu_constr.addConstraint(new double[]{1., 1., 1.,}, Relationship.EQ, 1);

        // positive constraints
        fu_constr.addConstraint(new double[]{1., 0., 0.,}, Relationship.GEQ, 0);
        fu_constr.addConstraint(new double[]{0., 1., 0.,}, Relationship.GEQ, 0);
        fu_constr.addConstraint(new double[]{0., 0., 1.,}, Relationship.GEQ, 0);


        HalfspaceToVertex conversor = new HalfspaceToVertex();

        VertexFactor fu_vert = conversor.apply(fu_constr,0);

        System.out.println(fu_vert);

    }

}
