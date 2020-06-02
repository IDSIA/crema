package pgm20.examples;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.causality.CausalVE;
import ch.idsia.crema.inference.causality.CredalCausalAproxLP;
import ch.idsia.crema.inference.causality.CredalCausalVE;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;

public class Fig2 {
    public static void main(String[] args) throws InterruptedException {


        RandomUtil.getRandom().setSeed(236226);
        RandomUtil.getRandom().setSeed(23621);  // non-overlapping intervals... but ~P(Y|X) = P(Y)
        RandomUtil.getRandom().setSeed(4434);
        RandomUtil.getRandom().setSeed(441134);



        StructuralCausalModel smodel = new StructuralCausalModel();
        int x = smodel.addVariable(2);
        int y = smodel.addVariable(2);
        int u = smodel.addVariable(5,true);

        smodel.addParent(x,u);
        smodel.addParent(y,u);
        smodel.addParent(y,x);



   //     BayesianFactor fx = BayesianFactor.deterministic(smodel.getDomain(x), smodel.getDomain(u), 0,0,0,1,1);
        BayesianFactor fy = BayesianFactor.deterministic(smodel.getDomain(y), smodel.getDomain(x,u), 1,1,0,0,0, 1,0,0,1,0);


        smodel.fillWithRandomFactors(2);
        //BayesianFactor fx = BayesianFactor.deterministic(smodel.getDomain(x), smodel.getDomain(u), 1,1,0,0,1);
        BayesianFactor fx = smodel.getFactor(x);
        //BayesianFactor fy = BayesianFactor.deterministic(smodel.getDomain(y), smodel.getDomain(x,u), 1,1,0,0,0, 1,0,0,1,0);



        smodel.setFactor(x,fx);
        smodel.setFactor(y,fy);

        BayesianFactor pu = BayesianFactor.random(smodel.getDomain(u), Strides.empty(),2,false);
        //BayesianFactor pu = new BayesianFactor(smodel.getDomain(u), new double[]{0.25, 0.35, 0.1, 0.2, 0.1});
        //BayesianFactor pu = new BayesianFactor(smodel.getDomain(u), new double[]{0.15, 0.35, 0.15, 0.2, 0.15});

        smodel.setFactor(u, pu);

        /////

        System.out.println(Arrays.toString(smodel.getProb(x).getData()));
        System.out.println(Arrays.toString(smodel.getProb(y).getData()));


        BayesianFactor[] factors = {fx.combine(fy).combine(pu).marginalize(u)};
        System.out.println("Empirical join: "+factors[0]);
        System.out.println(Arrays.toString(factors[0].reorderDomain(x).getData()));

        SparseModel csmodel = smodel.toCredalNetwork(factors);


        smodel.printSummary();

        System.out.println("\nEquivalent credal network");
        System.out.println("=============================");


        VertexFactor k[] = new VertexFactor[csmodel.getVariablesCount()];
        for(int v: smodel.getVariables()){
            k[v] = (VertexFactor)csmodel.getFactor(v);
            if(smodel.isExogenous(v))
                System.out.println(k[v]);
        }





        TIntIntHashMap intervention = new TIntIntHashMap();
        intervention.put(x,0);


        CausalVE inf1 = new CausalVE(smodel);
        BayesianFactor res1 = inf1.doQuery(y, intervention);
        System.out.println(res1);

        CredalCausalVE inf2 = new CredalCausalVE(smodel);
        VertexFactor res2 = inf2.doQuery(y, intervention);
        System.out.println(res2);

        CredalCausalAproxLP inf3 = new CredalCausalAproxLP(smodel);
        IntervalFactor res3 = inf3.doQuery(y, intervention);
        System.out.println(res3);



    }
}