import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.math3.optim.linear.Relationship;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class UaiParser {

    public static void main(String[] args) throws InterruptedException {

        String fileName = "./examples/network.cn"; // .cn File to open

        StringBuilder stringBuilder = new StringBuilder();

        String line = null;

        // Opening the .cn file
        try {
            FileReader fileReader =
                    new FileReader(fileName);

            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + " ");
            }

            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }

        // Splitting file elements (spaces, tabs and newlines have the same effect)
        String[] elements = stringBuilder.toString().split("[ \\t\\n]+");

        // Credal network
        SparseModel credalNetwork = new SparseModel();


        int offset = 1;

        // Parsing the number of variables in the network
        int numberOfVariables = Integer.parseInt(elements[offset]);

        // Parsing the cardinalities of the variables
        int[] cardinalities = new int[numberOfVariables];
        offset += 1;
        for (int i = 0; i < numberOfVariables; i++) {
            cardinalities[i] = Integer.parseInt(elements[offset + i]);
        }

        // Creating the credal network variables with their proper cardinality
        int[] x = new int[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            x[i] = credalNetwork.addVariable(cardinalities[i]);
        }

        // Parsing the number of tables (useless variable kept for compatibility reasons only)
        offset += numberOfVariables;
        int numberOfTables = Integer.parseInt(elements[offset]);
        offset += 1;

        // Parsing the number of parents and the parents
        int[][] parents = new int[numberOfVariables][];
        int numberOfParents = 0;
        for (int i = 0; i < numberOfVariables; i++) {
            numberOfParents = Integer.parseInt(elements[offset]) - 1;
            parents[i] = new int[numberOfParents];
            for (int k = 0; k < numberOfParents; k++) {
                parents[i][k] = Integer.parseInt(elements[offset + 2 + k]);
            }
            offset += numberOfParents + 2;
        }

        // Adding the parents to each variable
        for (int k = 0; k < numberOfVariables; k++) {
            credalNetwork.addParents(x[k], parents[k]);
        }

        // Parsing the a and b coefficient for each variable
        double[][] aCoeff = new double[numberOfVariables][];
        double[][] bCoeff = new double[numberOfVariables][];
        int n_a, n_b;
        for (int i = 0; i < numberOfVariables; i++) {
            n_a = Integer.parseInt(elements[offset]);
            aCoeff[i] = new double[n_a];
            for (int k = 0; k < n_a; k++) {
                aCoeff[i][k] = Double.parseDouble(elements[offset + 1 + k]);
            }
            offset += n_a + 1;
            n_b = Integer.parseInt(elements[offset]);
            bCoeff[i] = new double[n_b];
            for (int k = 0; k < n_b; k++) {
                bCoeff[i][k] = Double.parseDouble(elements[offset + 1 + k]);
            }
            offset += n_b + 1;
        }

        // Sanity check
        assert offset == elements.length;

        // Specifying the linear constraints for each variable
        SeparateHalfspaceFactor[] cpt = new SeparateHalfspaceFactor[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            int varsize = credalNetwork.getDomain(i).getSizes()[0];

            if (parents[i].length == 0) {
                // reshaped coeff A
                double[][] A2d = ArraysUtil.reshape2d(aCoeff[i], aCoeff[i].length / varsize);
                // build the factor
                cpt[i] = new SeparateHalfspaceFactor(credalNetwork.getDomain(i), A2d, bCoeff[i], Relationship.LEQ);
            } else {

                int par_comb = credalNetwork.getDomain(credalNetwork.getParents(i)).getCombinations();
                double[][] A2d_full = ArraysUtil.reshape2d(aCoeff[i], aCoeff[i].length / (varsize * par_comb));

                // Get the coefficients matrices for each combination of the parents
                double[][][] A = new double[par_comb][][];
                double[][] b = new double[par_comb][];

                for (int j = 0; j < par_comb; j++) {
                    double[][] Aj = ArraysUtil.copyOfRange(A2d_full, j * varsize, (j + 1) * varsize, 1);
                    int[] to_drop = ArraysUtil.rowsWhereAllZeros(Aj);
                    Aj = ArraysUtil.dropRows(Aj, to_drop);
                    double[] bj = ArraysUtil.dropColumns(new double[][]{bCoeff[i]}, to_drop)[0];
                    A[j] = Aj;
                    b[j] = bj;
                }

                // build the factor
                cpt[i] = new SeparateHalfspaceFactor(credalNetwork.getDomain(i),
                        credalNetwork.getDomain(credalNetwork.getParents(i)),
                        A, b, Relationship.LEQ);


            }
        }

        credalNetwork.setFactors(cpt);


        // Print factors for checking

        for (int i = 0; i < numberOfVariables; i++) {
            System.out.println("Variable " + i);
            ((SeparateHalfspaceFactor) credalNetwork.getFactor(i)).printLinearProblem();
        }


    }


}