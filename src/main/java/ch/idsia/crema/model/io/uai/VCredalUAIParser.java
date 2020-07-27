package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Parser for V-CREDAL networks in UAI format
 *  @author Rafael Cabañas
 */

public class VCredalUAIParser extends NetUAIParser<SparseModel<VertexFactor>>{


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
        parseDomainsFirstIsHead();
        parseVertices();
    }

    @Override
    protected SparseModel<VertexFactor> build() {
        SparseModel<VertexFactor> model = new SparseModel<>();

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
            
            int[] parent_list = ArraysUtil.reverse(parents[i]);
            int[] sizes = ArraysUtil.at(cardinalities, parent_list);
            
            Strides dataDomain = new Strides(parent_list, sizes);
            IndexIterator iter = dataDomain.getReorderedIterator(parents[i]);
            
            for(int j=0;j<parentComb;j++){
            	// here the sequential ordering is not correct! 
            	int jj = iter.next();
            	
                int numVertices = popInteger()/cardinalities[i];
                vertices[i][jj]=new double[numVertices][];
                for(int k=0; k<numVertices; k++){
                    vertices[i][jj][k] = new double[cardinalities[i]];
                    for(int s=0; s<cardinalities[i]; s++){
                        vertices[i][jj][k][s] = popDouble();
                    }
                }
            }

        }


    }

    public static void main(String[] args) throws IOException {
        String fileName = "./models/simple-vcredal.uai"; // .cn File to open
        SparseModel model = (SparseModel) UAIParser.read(fileName);

        for(int x : model.getVariables()){
            System.out.println(((VertexFactor)model.getFactor(x)));
        }

    }

}
