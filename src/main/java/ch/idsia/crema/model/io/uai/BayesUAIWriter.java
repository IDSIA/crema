package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.IO;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.model.io.TypesIO;
import ch.idsia.crema.utility.ArraysUtil;
import com.google.common.primitives.Ints;

import java.io.BufferedWriter;
import java.io.IOException;


public class BayesUAIWriter extends NetUAIWriter<BayesianNetwork> {



    public BayesUAIWriter(BayesianNetwork target, String file) throws IOException {
        this.target = target;
        TYPE = UAITypes.BAYES;
        this.writer = initWriter(file);

    }
    public BayesUAIWriter(BayesianNetwork target, BufferedWriter writer){
        this.target = target;
        TYPE = UAITypes.BAYES;
        this.writer = writer;
    }


    @Override
    protected void sanityChecks() {
        return;
    }

    @Override
    protected void writeFactors() throws IOException {


        tofileln("");
        for(int v : target.getVariables()) {

            BayesianFactor f =  target.getFactor(v);
            int vsize = f.getDomain().getCardinality(v);

            f = f.reorderDomain(Ints.concat(new int[]{v},
                                        ArraysUtil.reverse(target.getParents(v))
            ));

            double[] probs = f.getData();
            tofileln(probs.length);

            for(double[] p : ArraysUtil.reshape2d(probs, probs.length/vsize, vsize))
                tofileln(p);

            //tofileln(probs);

            tofileln("");





/*
            if(f != null){
                if(target.isEndogenous(v)) {
                   int[] assig = target.getFactor(v).getAssignments(target.getParents(v));
                   tofile(assig.length+"\t");
                   tofileln(assig);
                }else{
                    double[] probs = target.getFactor(v).getData();
                    tofile(probs.length+"\t");
                    tofileln(probs);

                }
            }else{
                tofileln(0);
            }


 */
        }

    }

    @Override
    protected void writeTarget() throws IOException {
        writeType();
        writeVariablesInfo();
        writeDomains();
        writeFactors();
    }


    @Override
    protected void writeDomains() throws IOException {
        // Write the number of factors
        tofileln(target.getVariables().length);
        // Add the factor domains with children at the end
        for(int v: target.getVariables()){
            int[] parents = ArraysUtil.reverse(target.getParents(v));
            tofile(parents.length+1+"\t");
            tofile(parents);
            tofileln(v);
        }
    }


    public static boolean isCompatible(Object target){
        return target instanceof BayesianNetwork;
    }



    public static void main(String[] args) throws IOException {
        String fileName = "./models/bayes";

        BayesianNetwork bnet = (BayesianNetwork) IO.read(fileName+".uai");

        IO.write(bnet,fileName+"_2.uai");
    }



}
