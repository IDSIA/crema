package ch.idsia.crema.preprocess.creators;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactorFactory;
import ch.idsia.crema.utility.IndexIterator;

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

        //shortcut in case the function is strictly monotone growing
        //double[] extremes= new double[]{operation.execute(lower[0], upper[1]), operation.execute(upper[0], lower[1])};
        //return Arrays.stream(extremes).sorted().toArray();

        double[] results = new double[2 * lower.length];
        results[0] = operation.execute(lower[0], lower[1]);
        results[1] = operation.execute(lower[0], upper[1]);
        results[2] = operation.execute(upper[0], lower[1]);
        results[3] = operation.execute(upper[0], upper[1]);

        Arrays.sort(results);
        double firstElement = results[0];
        double lastElement = results[results.length - 1];

        return new double[]{firstElement, lastElement};
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
        int dimChild = childCuts.length + 1;
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

            // the lower is set to an array of zeroes the upper is set to 1 in the position of the interval number
            factory.set(new double[dimChild], createUpper(intervalNumber, dimChild), comb);
            iterator.next();
        }
        return factory.get();
    }

    /**
     * @param interval array containing the position
     * @param dim      dimension of the array to be generated
     * @return double array with 1.0 set for every interval value
     */
    public double[] createUpper(int[] interval, int dim) {
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
