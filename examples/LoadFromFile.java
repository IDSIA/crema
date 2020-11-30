import ch.idsia.crema.IO;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;

import java.io.IOException;

public class LoadFromFile
{
    public static void main(String[] args) throws IOException {

        SparseModel vnet = (SparseModel) IO.read("./models/simple-vcredal2.uai");

        System.out.println(vnet.getFactor(2));

    }

}
