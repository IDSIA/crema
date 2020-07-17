import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.Inference;
import ch.idsia.crema.inference.ve.CredalVariableElimination;
import ch.idsia.crema.model.ObservationBuilder;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.user.credal.Vertex;


public class PGMpaper {
    public static void main(String[] args) {

        // define the structure
        SparseModel cnet = new SparseModel();
        int a = cnet.addVariable(2);
        int b = cnet.addVariable(3);
        cnet.addParent(a,b);
        // add credal set K(B)
        VertexFactor fb = new VertexFactor(cnet.getDomain(b), Strides.empty());
        fb.addVertex(new double[]{0.2, 0.5, 0.3});
        fb.addVertex(new double[]{0.3, 0.4, 0.3});
        fb.addVertex(new double[]{0.3, 0.2, 0.5});
        cnet.setFactor(b,fb);
        // add credal set K(A|B)
        VertexFactor fa = new VertexFactor(cnet.getDomain(a), cnet.getDomain(b));
        fa.addVertex(new double[]{0.5, 0.5}, 0);
        fa.addVertex(new double[]{0.6, 0.4}, 0);
        fa.addVertex(new double[]{0.3, 0.7}, 1);
        fa.addVertex(new double[]{0.4, 0.4}, 1);
        fa.addVertex(new double[]{0.2, 0.8}, 2);
        fa.addVertex(new double[]{0.1, 0.9}, 2);
        cnet.setFactor(a,fa);
        // set up the inference and run the queries
        Inference inf = new CredalVariableElimination(cnet);
        VertexFactor res1 = (VertexFactor) inf.query(b, ObservationBuilder.observe(a, 0));
        VertexFactor res2 = (VertexFactor) inf.query(a);

        double[][][] vertices = res1.getData();


    }
}
