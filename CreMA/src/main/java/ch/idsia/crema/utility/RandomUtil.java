package ch.idsia.crema.utility;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class RandomUtil {
    /**
     * Sample of vector where the sum of all its elements is 1
     * @param size
     * @param num_decimals
     * @param zero_allowed
     * @return
     */
    public static double[] sampleNormalized(int size, int num_decimals, boolean zero_allowed)
    {

        int upper = (int) Math.pow(10,num_decimals);
        if(!zero_allowed)
            upper -= size;

        Random r = ThreadLocalRandom.current();
        double data[] = new double[size];
        int sum = 0;
        for(int i=0; i<size-1; i++){
            if(sum<upper){
                int x = r.nextInt(upper-sum);
                sum += x;
                data[i] = x;
            }

        }
        data[data.length-1] = ((double)upper - sum);

        for(int i = 0; i<size; i++){
            if(!zero_allowed) {
                data[i] ++;
                data[i] = data[i]/(upper+size);
            }else {
                data[i] = data[i]/upper;
            }
        }
        List dataList =  Doubles.asList(data);
        Collections.shuffle(dataList);
        return Doubles.toArray(dataList);
    }

    /**
     * Sample an array of itegers from a uniform between 0 and a given upper bound (not included).
     * @param size - length of the output
     * @param upper_bound - indicates the upper bound of the sampling interval.
     * @param sample_all - allow to constraint that all passible values should appear in the output.
     * @return
     */

    public static int[] sampleUniform(int size, int upper_bound, boolean sample_all){

        if(sample_all && upper_bound>size){
            new IllegalArgumentException("ERROR: upper_bound cannot be greater than the array size");
        }

        int[] data = new int[size];
        Random r = ThreadLocalRandom.current();

        int current_i = 0;

        if(sample_all){
            for(int i=0; i<upper_bound; i++)
                data[i] = i;
            current_i = upper_bound;
        }
        for(int i = current_i; i<size; i++){
            data[i] = r.nextInt(upper_bound);
        }

        List dataList =  Ints.asList(data);
        Collections.shuffle(dataList);
        return Ints.toArray(dataList);
    }

}
