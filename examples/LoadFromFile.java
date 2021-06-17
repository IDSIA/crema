package examples;

import ch.idsia.crema.IO;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

import java.io.IOException;

public class LoadFromFile
{
    public static void main(String[] args) throws IOException {

        DAGModel<IntervalFactor> vnet = IO.read("./models/simple-vcredal2.uai");

        System.out.println(vnet.getFactor(2));

    }

}
//18