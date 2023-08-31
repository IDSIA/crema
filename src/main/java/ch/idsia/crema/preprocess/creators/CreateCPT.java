package ch.idsia.crema.preprocess.creators;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.utility.IndexIterator;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateCPT {

    /**
     * Border returns the borders of the intervals
     *
     * @param lower array of lower bounds for all the variables
     *              ex: double[] lower = new double[]{1.55, 55.0};
     * @param upper array of upper bounds for all the variables
     *              ex: double[] upper = new double[]{1.90, 115.0};
     */
    public double[] borders(double[] lower, double[] upper, Op operation) {

        int DIM = (int) Math.pow(2, lower.length);
        double[] results = new double[DIM];

        for (int i = 0; i < DIM; i++) {
            StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));
            for(int j = binary.length(); j < lower.length; j++) {
                binary.insert( 0, '0' );
            }
            ArrayList<Double> paramList = new ArrayList<>();

            for(int j = 0; j < binary.length(); j++) {
                if(binary.charAt(j) == '0') {
                    paramList.add(lower[j]);
                } else {
                    paramList.add(upper[j]);
                }
            }
            results[i] = operation.execute(paramList.stream().mapToDouble(Double::doubleValue).toArray());
        }

        return new double[]{
                Arrays.stream(results).min().isPresent()? Arrays.stream(results).min().getAsDouble(): 0.0,
                Arrays.stream(results).max().isPresent()? Arrays.stream(results).max().getAsDouble(): 0.0};
    }

    /**
     * Method to create a CPT
     *
     * @param childVar    variable of the child
     * @param parentsVars array of variables of the parents
     * @param childCuts   array of cuts for the child
     * @param parentCuts  array of cuts for the parents
     * @param operation   operation to be performed with the cuts
     *                    example K(bmi|w,H)
     * @return IntervalFactor representing the CPT of the child given the parents
     */
    public IntervalFactor create(int childVar, int[] parentsVars, double[] childCuts, double[][] parentCuts, Op operation) {

        // root nodes creation
        // add child node
        int dimChild = childCuts.length-1;
        Strides stridesChild = Strides.var(childVar, dimChild);

        // create domain
        Strides dom = Strides.empty();
        for (int i = 0; i < parentsVars.length; i++) {
            dom = dom.and(parentsVars[i], parentCuts[i].length - 1);
        }
        // add parents nodes
        IntervalFactorFactory factory = IntervalFactorFactory.factory().domain(stridesChild, dom);

        // create iterator
        IndexIterator iterator = dom.getIterator();
        //iterate over all possible combinations
        while (iterator.hasNext()) {
            int[] comb = iterator.getPositions().clone();
            // be aware that the structure returned by the method has to be compliant with the child
            double[] parentIntervalLower = new double[parentCuts.length];
            double[] parentIntervalUpper = new double[parentCuts.length];

            for (int i = 0; i < parentCuts.length; i++) {
                parentIntervalLower[i] = parentCuts[i][comb[i]];
                parentIntervalUpper[i] = parentCuts[i][comb[i] + 1];
            }

            double[] intervalBorders = borders(parentIntervalLower, parentIntervalUpper, operation);
            //map the integers of the position of the childCuts
            int[] intervalNumber = Arrays.stream(intervalBorders).mapToInt(val -> whichPosition(childCuts, val)).toArray();

            boolean allEqual = intervalNumber.length == 1 || Arrays.stream(intervalNumber).allMatch(t -> t == intervalNumber[0]);
            if(allEqual){
                factory.set(createArrayWithOneAtIndex(intervalNumber, dimChild), createArrayWithOneAtIndex(intervalNumber, dimChild), comb);
            }else{
                factory.set(new double[dimChild], createArrayWithOneAtIndex(intervalNumber, dimChild), comb);
            }
            iterator.next();
        }
        return factory.get();
    }

    /**
     * @param interval array containing the position
     * @param dim      dimension of the array to be generated
     * @return double array with 1.0 set for every interval value
     */
    public double[] createArrayWithOneAtIndex(int[] interval, int dim) {
        double[] upper = new double[dim];

        // we set to 1.0 all the element relative to the interval
        for (int ind : interval) {
            upper[ind - 1] = 1.0;
        }
        return upper;
    }

    /**
     * Method to find the interval containing the specified value
     * Specials: if the number is lower than the first cut, it will be placed in the first interval
     * if the number is higher than the last cut, it will be placed in the last interval
     *
     * @param cutsX array of cuts, typically for the discretization
     * @param X     value that we want to place
     * @return integer of the interval containing x
     */
    //greedy
    public int whichPosition(double[] cutsX, double X) {
        int position = 0; //starts from 1
        for (int i = 1; i < cutsX.length - 1; i++) {
            if (X <= cutsX[i]) {
                break;
            }
            position++;
        }
        return position + 1;
    }
}
