package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.utility.ArraysUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.IntStream;

public class BayesUAIParser extends NetUAIParser<BayesianNetwork>{

    private double[][] probs;

    public BayesUAIParser(String file) throws FileNotFoundException {
        TYPE = "BAYES";
        this.bufferedReader = initReader(file);
    }

    public BayesUAIParser(BufferedReader reader) {
        TYPE = "BAYES";
        this.bufferedReader = reader;
    }

    @Override
    protected void processFile() {
        parseType();
        parseVariablesInfo();
        parseDomainsLastIsHead();
        parseCPTs();
    }

    @Override
    protected BayesianNetwork build() {
        BayesianNetwork model = new BayesianNetwork();

        // Add the variables
        for (int i = 0; i < numberOfVariables; i++) {
            model.addVariable(cardinalities[i]);
        }

        // Adding the parents to each variable
        for (int k = 0; k < numberOfVariables; k++) {
            model.addParents(k, parents[k]);
        }

        // Build the bayesian Factor for each variable
        BayesianFactor[] cpt = new BayesianFactor[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            // Build the domain with the head/left variable at the end
            Strides dom = model.getDomain(parents[i]).concat(model.getDomain(i));

            double data[] = probs[i];
            if (parents[i].length>0)
                data = ArraysUtil.changeEndian(probs[i], dom.getSizes());
            cpt[i] = new BayesianFactor(dom, data);

        }

        model.setFactors(cpt);

        return model;
    }

    @Override
    protected void sanityChecks() {
        super.sanityChecks();
    }

    private void parseCPTs(){

        // Parse the probability values and store them in a 1D array
        // for each factor

        probs = new double[numberOfVariables][];
        for(int i=0; i<numberOfVariables;i++){
            int numValues = popInteger();
            probs[i] = new double[numValues];
            for(int j=0;j<numValues;j++){
                probs[i][j] = popDouble();
            }
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        String fileName = "./models/simple-bayes.uai"; // .cn File to open
        BayesianNetwork model = (BayesianNetwork) UAIParser.open(fileName);

        for(int x : model.getVariables()){
            System.out.println((model.getFactor(x)));
        }

    }

}
