package ch.idsia.crema.inference;
import ch.idsia.crema.core.DomainBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactorFactory;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.graphical.BayesianNetwork;

public class TimeIt {
    public static void main2(String[] args) {
      
        BayesianNetwork bn = new BayesianNetwork();

        int A = bn.addVariable(2);
        int B = bn.addVariable(2);
        int C = bn.addVariable(2);
        
        bn.addParent(B, A);
        bn.addParent(C, B);

        var pA = BayesianFactorFactory.factory().domain(bn.getDomain(A)).
                set(0.2, 0).
                set(0.8,1).
                get();

        var pBA = BayesianFactorFactory.factory().domain(bn.getDomain(A,B)).
                set(.3, 0, 0).
                set(.7, 0, 1).
                set(.6, 1, 0).
                set(.4, 1, 1).
                get();
        
        var pCB = BayesianFactorFactory.factory().domain(bn.getDomain(B,C)).
                set(.5, 0, 0).
                set(.5, 0, 1).
                set(.8, 1, 0).
                set(.2, 1, 1).
                get();
        var pC = pA.combine(pBA).marginalize(A).combine(pCB).marginalize(B).combine(pCB);
        
            
        System.out.println(pA.combine(pBA).marginalize(A).combine(pCB));
    }


    public static void main(String[] args) {
            main2(args);
            

        double dd  = 0;
        double sum = 0;
        
        long nt = System.nanoTime();
        int count = 100000;
        for (int i = 0; i < 100000; ++i) {
            

            var pA = BayesianFactorFactory.factory().
                    domain(DomainBuilder.var(1).size(2).strides()).
                    set(Math.random(), 0).
                    set(Math.random(),1).
                    get();

            var pBA = BayesianFactorFactory.factory().
                    domain(DomainBuilder.var(1,2).size(2,2).strides()).
                    set(Math.random(), 0, 0).
                    set(Math.random(), 0, 1).
                    set(Math.random(), 1, 0).
                    set(Math.random(), 1, 1).
                    get();
            
            var pCB = BayesianFactorFactory.factory().
                    domain(DomainBuilder.var(2,3).size(2,2).strides()).
                    set(Math.random(), 0, 0).
                    set(Math.random(), 0, 1).
                    set(Math.random(), 1, 0).
                    set(Math.random(), 1, 1).
                    get();

            var bf = pA.combine(pBA).marginalize(1).combine(pCB).marginalize(2);
            sum += bf.getValue(0);
        }
        
        dd = (System.nanoTime() - nt);

        System.out.println(" time is: " + dd / 100000000.0);
        
        System.out.println(" time is: [ms] " + dd / 100000000.0 / (double)count * 1000.0);

        System.out.println(sum);
    }
}