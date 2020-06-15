package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.user.credal.Vertex;
import ch.idsia.crema.utility.ArraysUtil;
import org.apache.commons.math3.optim.linear.Relationship;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.IntStream;

public class VCredalUAIParser extends NetUAIParser<SparseModel>{


    private double[][][][] vertices = new double[numberOfVariables][][][];

    public VCredalUAIParser(String file) throws FileNotFoundException {
        TYPE = "V-CREDAL";
        this.bufferedReader = initReader(file);
    }

    public VCredalUAIParser(BufferedReader reader) {
        TYPE = "V-CREDAL";
        this.bufferedReader = reader;
    }

    @Override
    protected void processFile() {
        parseType();
        parseVariablesInfo();
        parseDomains();
        parseVertices();
    }

    @Override
    protected SparseModel build() {
        SparseModel model = new SparseModel();

        // Add the variables
        for (int i = 0; i < numberOfVariables; i++) {
            model.addVariable(cardinalities[i]);
        }

        // Adding the parents to each variable
        for (int k = 0; k < numberOfVariables; k++) {
            model.addParents(k, parents[k]);
        }

        // Specifying the linear constraints for each variable
        VertexFactor[] cpt = new VertexFactor[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            cpt[i] = new VertexFactor(model.getDomain(i), model.getDomain(parents[i]), vertices[i]);
        }

        model.setFactors(cpt);

        return model;
    }

    @Override
    protected void sanityChecks() {
        super.sanityChecks();
    }

    private void parseVertices(){

        // Parsing the a and b coefficient for each variable
        vertices = new double[numberOfVariables][][][];
        for(int i=0; i<numberOfVariables;i++){
            int parentComb = IntStream.of(parents[i]).map(p->cardinalities[p]).reduce((a,b)-> a*b).orElse(1);
            vertices[i] = new double[parentComb][][];
            for(int j=0;j<parentComb;j++){
                int numVertices = Integer.parseInt(popElement())/cardinalities[i];
                vertices[i][j]=new double[numVertices][];
                for(int k=0; k<numVertices; k++){
                    vertices[i][j][k] = new double[cardinalities[i]];
                    for(int s=0; s<cardinalities[i]; s++){
                        vertices[i][j][k][s] = Double.parseDouble(popElement());
                    }
                }
            }

        }


    }

    public static void main(String[] args) throws IOException {
        String fileName = "./models/simple-vcredal.uai"; // .cn File to open
        SparseModel model = (SparseModel) UAIParser.open(fileName);

        for(int x : model.getVariables()){
            System.out.println(((VertexFactor)model.getFactor(x)));
        }

    }

}
