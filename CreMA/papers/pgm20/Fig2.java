package pgm20;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.graphical.specialized.StructuralCausalModel;
import ch.idsia.crema.user.credal.Vertex;
import ch.idsia.crema.utility.RandomUtil;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.stream.Stream;

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

        SparseModel csmodel = smodel.toVertexNonMarkov(factors);


        smodel.printSummary();

        System.out.println("\nEquivalent credal network");
        System.out.println("=============================");


        VertexFactor k[] = new VertexFactor[csmodel.getVariablesCount()];
        for(int v: smodel.getVariables()){
            k[v] = (VertexFactor)csmodel.getFactor(v);
            if(smodel.isExogenous(v))
                System.out.println(k[v]);
        }


 /*

 DIVISION IS NOT IMPLEMENTED SO IT FAILS
 System.out.println("\ndo conditioning result:");

 VariableElimination ve = new FactorVariableElimination(csmodel.getVariables());
 ve.setFactors(csmodel.getFactors());
 ve.setEvidence(new TIntIntHashMap(new int[]{x}, new int[]{0}));
 BayesianFactor pcond = (BayesianFactor)ve.run(y);
 System.out.println(Arrays.toString(pcond.getData()));

 factors[0].getData()

 // fails
k[u].combine(k[x]).combine(k[y]).marginalize(u).divide(
        k[u].combine(k[x]).marginalize(u) // precision error here?
)

*/

        System.out.println("\ndo calculus result:");

        SparseModel do_csmodel = csmodel.intervention(x,0);

        // P(Y|do(x=0)) = P'(Y|x=0) =?= P'(Y)
        VariableElimination ve = new FactorVariableElimination(do_csmodel.getVariables());
        ve.setFactors(do_csmodel.getFactors());
        VertexFactor pdo = (VertexFactor)ve.run(y);
        System.out.println(pdo);


//// check results by performing the same queries of the sets of precise BNS


        do_csmodel = csmodel.intervention(x,1);


/// check do calculus
        Stream.of(do_csmodel.sampleVertex(10)).map(
                (bnet) -> {
                    VariableElimination veprec = new FactorVariableElimination(bnet.getVariables());
                    veprec.setFactors(bnet.getFactors());
                    return ((BayesianFactor) veprec.run(y)).getData();
                }
        ).toArray();





        /// conditioning
        Stream.of(csmodel.sampleVertex(10)).map(
                (bnet) -> {
                    VariableElimination veprec = new FactorVariableElimination(bnet.getVariables());
                    veprec.setEvidence(new TIntIntHashMap(new int[]{x}, new int[]{1}));
                    veprec.setFactors(bnet.getFactors());
                    return ((BayesianFactor) veprec.run(y)).getData();
                }
        ).toArray();



        // check that the joint induced is the same
        Stream.of(csmodel.sampleVertex(10)).map(
                bnet -> bnet.getFactor(u).combine(bnet.getFactor(x)).combine(bnet.getFactor(y)).marginalize(u).getData()
        ).toArray();

        // P(X)
        Stream.of(csmodel.sampleVertex(10)).map(
                bnet -> bnet.getFactor(u).combine(bnet.getFactor(x)).marginalize(u).getData()
        ).toArray();



        // P(Y|x1)
        Stream.of(csmodel.sampleVertex(10)).map(
                bnet -> bnet.getFactor(u).combine(bnet.getFactor(x)).combine(bnet.getFactor(y)).marginalize(u).divide(
                        bnet.getFactor(u).combine(bnet.getFactor(x)).marginalize(u)).filter(x,0).getData()
        ).toArray();


        // P(Y|x2)
        Stream.of(csmodel.sampleVertex(10)).map(
                bnet -> bnet.getFactor(u).combine(bnet.getFactor(x)).combine(bnet.getFactor(y)).marginalize(u).divide(
                        bnet.getFactor(u).combine(bnet.getFactor(x)).marginalize(u)).filter(x,1).getData()
        ).toArray();



        // ApproxLP

        Inference approx = new Inference();
        IntervalFactor resultsALP = null;
        resultsALP = approx.query(csmodel, y, -1);

        System.out.println(Arrays.toString(resultsALP.getUpper()));
        System.out.println(Arrays.toString(resultsALP.getLower()));



    }
}