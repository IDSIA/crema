package ch.idsia.crema.factor.bayesian;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.utility.ArraysMath;

public class BayesianOperations {
//0.01656,0.0168,0.04284,0.018,0.02496,0.04728,0.03024,0.05856,0.06984 0.32508
//0.01656,0.0168,0.04284,0.018,0.02496,0.04728,0.03024,0.05856,0.06984
    @Test
    public void test() {
        Domain abc = DomainBuilder.var(0,1,2).size(3,2,2).strides();
        Domain bc = DomainBuilder.var(1,2).size(2,2).strides();
        Domain bde = DomainBuilder.var(1,3,4).size(2,3,3).strides();
        Domain de = DomainBuilder.var(3,4).size(3,3).strides();
        
        Domain d = DomainBuilder.var(3).size(3).strides();
        Domain a = DomainBuilder.var(0).size(3).strides();

        Domain ce = DomainBuilder.var(2,4).size(2,3).strides();
        Domain e =  DomainBuilder.var(4).size(3).strides();

        Domain ed = DomainBuilder.var(4,3).size(3,3).strides();

        var pa = BayesianFactorFactory.factory().domain(a).data(new double[]{0.3, 0.4, 0.3}).get();
        var pabc = BayesianFactorFactory.factory().domain(abc).data(new double[]{
            0.1, 0.2, 0.7, 
            0.3, 0.1, 0.6, 
            0.3, 0.5, 0.2, 
            0.5, 0.4, 0.1
        }).get();
        var pbde = BayesianFactorFactory.factory().domain(bde).data(new double[] {
                0.1, 0.9, 
                0.3, 0.7, 
                0.6, 0.4, 
                0.5, 0.5, 
                0.8, 0.2, 
                0.2, 0.8, 
                0.9, 0.1, 
                0.4, 0.6, 
                0.7, 0.3
        }).get();
        var pd = BayesianFactorFactory.factory().domain(d).data(new double[]{
            0.6, 0.2, 0.2
        }).get();
        var pce = BayesianFactorFactory.factory().domain(ce).data(new double[]{
            0.9, 0.1, 
            0.8, 0.2, 
            0.6, 0.4
        }).get();
        var ped = BayesianFactorFactory.factory().domain(de).data(new double[]{
            0.1, 0.1, 0.8, 
            0.2, 0.7, 0.1, 
            0.7, 0.2, 0.1
        }).get();

        var xx = pa.combine(pabc).combine(pbde)
                    .marginalize(1)
                    .combine(pce)
                    .marginalize(2)
                    .combine(pd, ped)
                    .marginalize(3);
        // var xx = pd.combine(ped);
        double o = ArraysMath.sum(xx.getData());
        System.out.println(Arrays.toString(xx.getData()));
    }

}
