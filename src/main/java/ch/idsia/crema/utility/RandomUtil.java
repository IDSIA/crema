package ch.idsia.crema.utility;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;


public class RandomUtil {

    private static Random random = new Random(1234);

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

        double data[] = new double[size];
        int sum = 0;
        for(int i=0; i<size-1; i++){
            if(sum<upper){
                int x = random.nextInt(upper-sum);
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
        Collections.shuffle(dataList, random);
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

        int current_i = 0;

        if(sample_all){
            for(int i=0; i<upper_bound; i++)
                data[i] = i;
            current_i = upper_bound;
        }
        for(int i = current_i; i<size; i++){
            data[i] = random.nextInt(upper_bound);
        }

        List dataList =  Ints.asList(data);
        Collections.shuffle(dataList, random);
        return Ints.toArray(dataList);
    }


    public static int[] sampleMultinomial(int numCounts, double[] probs){

        int num_decimals = DoubleStream.of(probs)
                .mapToInt(p ->Double.toString(p).split("\\.")[1].length())
                .max().orElse(10);

        int[] bounds = DoubleStream.of(probs).mapToInt(p -> (int) (Math.round(p*Math.pow(10, num_decimals)))).toArray();

        int acc = 0;
        for (int i = 0; i < bounds.length; i++) {
            acc += bounds[i];
            bounds[i] = acc;
        }

        int S[] = RandomUtil.sampleUniform(numCounts, (int) Math.pow(10, num_decimals), false);

        int[] counts = new int[probs.length];

        for(int s: S) {
            for (int i = 0; i < bounds.length; i++) {
                if (s < bounds[i]) {
                    counts[i]++;
                    break;
                }
            }
        }
        return counts;
    }

    public static int sampleCategorical(double[] probs){
        return  ArraysUtil.where(sampleMultinomial(1, probs), x -> x==1)[0];
    }


    public static void setRandom(Random random) {
        RandomUtil.random = random;
    }

    public static void setRandomSeed(long seed){
        setRandom(new Random(seed));
    }

    public static Random getRandom() {
        return random;
    }
}
