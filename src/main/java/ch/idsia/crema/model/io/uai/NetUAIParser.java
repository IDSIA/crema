package ch.idsia.crema.model.io.uai;

import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.model.graphical.SparseModel;
import org.springframework.util.Assert;

import java.util.stream.IntStream;

public abstract class NetUAIParser<T extends SparseModel> extends UAIParser<T> {

    protected int numberOfVariables;
    protected int[] cardinalities;
    int numberOfTables;
    int[][] parents;

    // todo: this assume that variables take consecutive ids from 0
    protected void parseVariablesInfo(){
        // Parsing the number of variables in the network
        numberOfVariables = Integer.parseInt(popElement());
        // Parse the number of states of each variable
        cardinalities = new int[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            cardinalities[i] = Integer.parseInt(popElement());
            assert cardinalities[i]>1;
        }
    }

    protected void parseDomains(){
        numberOfTables = Integer.parseInt(popElement());

        // Parsing the number of parents and the parents
        parents = new int[numberOfTables][];
        int numberOfParents = 0;
        for (int i = 0; i < numberOfTables; i++) {
            numberOfParents = Integer.parseInt(popElement()) - 1;
            int left_var = Integer.parseInt(popElement());
            parents[left_var] = new int[numberOfParents];
            for (int k = 0; k < numberOfParents; k++) {
                parents[i][k] = Integer.parseInt(popElement());
            }
        }

    }

    @Override
    protected void sanityChecks() {
        super.sanityChecks();

        // Specific sanity checks for SparseModels
        Assert.isTrue(numberOfVariables == numberOfTables,
                "Wrong number of tables ("+numberOfTables+") and variables ("+numberOfVariables+")");

        Assert.isTrue(IntStream.of(cardinalities).allMatch(c -> c>1), "Wrong cardinalities");

        for(int i=0; i<parents.length; i++){
            for(int j=0; j<parents[i].length;j++){
                Assert.isTrue(parents[i][j]>=0 || parents[i][j]< numberOfVariables);
                Assert.isTrue(parents[i][j]!=i);

            }
        }


    }




    }
