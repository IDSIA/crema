import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.convert.HalfspaceToVertex;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import org.apache.commons.math3.optim.linear.Relationship;

import java.util.Arrays;

public class CredalInference_Alessandro {

    public static double[] indicator(int pos, int lng){
        double[] v = new double[lng];
        for(int i=0; i<lng; i++){
            v[i] = 0;
            if(i == pos || pos == -1)
                v[i] =1.0;
        }
        return v;}


    public static void main(String[] args) throws InterruptedException {

        double p = 0.3;

        System.out.println("Experiment #0 (U->X)");
        SparseModel sm0 = new SparseModel();

        // U is ternary, X is binary
        int u = sm0.addVariable(3);
        int x = sm0.addVariable(2);
        SeparateHalfspaceFactor ku = new SeparateHalfspaceFactor(sm0.getDomain(u), Strides.empty());

        // U -> X
        sm0.addParent(x,u);

        // CPT P(X|U) (six parameters)
        // The structural equation is such that
        // U=0 => X=0, U=1 => X=0, U=2 => X=1
        SeparateHalfspaceFactor px = new SeparateHalfspaceFactor(sm0.getDomain(x), sm0.getDomain(u));

        // Non-negativity of all the probabilities in the CPT
        px.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 0);
        px.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 0);
        px.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 1);
        px.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 1);
        px.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 2);
        px.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 2);

        // Normalization
        px.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 0);
        px.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 1);
        px.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 2);

        // U=0 => X=0 means P(X=0|U=0)=1
        px.addConstraint(new double[]{1,0}, Relationship.EQ, 1, 0);
        // U=0 => X=0 means P(X=0|U=1)=1
        px.addConstraint(new double[]{1,0}, Relationship.EQ, 1, 1);
        // U=0 => X=0 means P(X=1|U=2)=1
        px.addConstraint(new double[]{0,1}, Relationship.EQ, 1, 2);

        // Say that, at the observable level, P(X=0) = p (and P(X=1) = 1-p))
        // P(X=0) = p = \sum_u P(X=0|u) P(u) = P(U=0)+P(U=1)
        // P(X=1) = 1-p = \sum_u P(X=1|u) P(u) = P(U=2)

        // K(U)
        // Non-negativity
        ku.addConstraint(new double[]{1,0,0}, Relationship.GEQ, 0);
        ku.addConstraint(new double[]{0,1,0}, Relationship.GEQ, 0);
        ku.addConstraint(new double[]{0,0,1}, Relationship.GEQ, 0);

        // Normalization
        ku.addConstraint(new double[]{1,1,1}, Relationship.EQ, 1);

        // Linear constraints
        ku.addConstraint(new double[]{1,1,0}, Relationship.EQ, p);
        //ku.addConstraint(new double[]{0,0,1}, Relationship.EQ, 1-p);

        // Add factors to the model
        sm0.setFactor(x, px);
        sm0.setFactor(u, ku);

        //Run inference
        Inference inference = new Inference();
        ApproxLP2 approxLP2 = new ApproxLP2();
        double[] upperVE = inference.query(sm0, x).getUpper();
        double[] lowerVE = inference.query(sm0, x).getLower();
        double[] upperLP = approxLP2.query(sm0, x).getUpper();
        double[] lowerLP = approxLP2.query(sm0, x).getUpper();

        System.out.println(Arrays.toString(upperVE));
        System.out.println(Arrays.toString(lowerVE));
        System.out.println(Arrays.toString(upperLP));
        System.out.println(Arrays.toString(lowerLP));

        // ------------------------------------------------------- //

        System.out.println("Experiment #1 (U1->X1->X2<-U2)");

        SparseModel sm = new SparseModel();

        int u1 = sm.addVariable(3);
        int x1 = sm.addVariable(2);
        int u2 = sm.addVariable(5);
        int x2 = sm.addVariable(2);

        sm.addParent(x1,u1);
        sm.addParents(x2,x1,u2);
        //sm.addParent(x2,x1);

        int[] sem1 = new int[] {0,0,1};
        int[][] sem2 = new int[][] {{1,1,0,0,0},{1,0,0,1,0}};
        double[][] sem22 = new double[][] {{1,1,0,0,0},{1,0,0,1,0}};

        SeparateHalfspaceFactor px1 = new SeparateHalfspaceFactor(sm.getDomain(x1), sm.getDomain(u1));
        SeparateHalfspaceFactor ku1 = new SeparateHalfspaceFactor(sm.getDomain(u1), Strides.empty());
        SeparateHalfspaceFactor px2 = new SeparateHalfspaceFactor(sm.getDomain(x2), sm.getDomain(x1,u2));
        SeparateHalfspaceFactor ku2 = new SeparateHalfspaceFactor(sm.getDomain(u2), Strides.empty());

        // Positivity and normalization
        for(int k=0; k<3; k++){
            for(int j=0; j<2; j++){
                px1.addConstraint(indicator(j,2), Relationship.GEQ, 0, k);}
            px1.addConstraint(indicator(-1,2), Relationship.EQ, 1, k);}
        for(int k=0; k<3; k++){
            px1.addConstraint(indicator(sem1[k],3), Relationship.EQ, 1, k);
        }

        // Positivity
        for(int k=0; k<3; k++){
            ku1.addConstraint(indicator(k,3), Relationship.GEQ, 0);}
        ku1.addConstraint(indicator(-1,3), Relationship.EQ, 1);
        ku1.addConstraint(new double[]{1,1,0}, Relationship.EQ, p);
        ku1.addConstraint(new double[]{0,0,1}, Relationship.EQ, 1-p);

        // Normalization and constraints
        for(int k=0;k<5;k++){ // U
            for(int j=0; j<2; j++){ // X
                for(int i=0; i<2; i++){
                    px2.addConstraint(indicator(i,2), Relationship.GEQ, 0, j,k);}
                px2.addConstraint(indicator(-1,2), Relationship.EQ, 1, j,k); }}
        for(int i=0;i<2;i++){
            for(int j=0;j<5; j++) {
                px2.addConstraint(indicator(sem2[i][j],2), Relationship.EQ, 1, i,j);}}

        for(int k=0; k<5; k++){
            ku2.addConstraint(indicator(k,5), Relationship.GEQ, 0);}
        ku2.addConstraint(indicator(-1,5), Relationship.EQ, 1);
        ku2.addConstraint(sem22[0], Relationship.EQ, 0.4);
        ku2.addConstraint(sem22[1], Relationship.EQ, 0.3);

        sm.setFactor(x1, px1);
        sm.setFactor(x2, px2);
        sm.setFactor(u1, ku1);
        sm.setFactor(u2, ku2);

        //Run inference
        upperVE = inference.query(sm, x2).getUpper();
        lowerVE = inference.query(sm, x2).getLower();
        upperLP = approxLP2.query(sm, x2).getUpper();
        lowerLP = approxLP2.query(sm, x2).getLower();

        System.out.println(Arrays.toString(upperVE));
        System.out.println(Arrays.toString(lowerVE));
        System.out.println(Arrays.toString(upperLP));
        System.out.println(Arrays.toString(lowerLP));


        int n = 10;
        p = 0.3;

        SparseModel chain = new SparseModel();

        // Structural equations
        int[] equation1 = new int[] {0,0,1};
        double[] equation1bis = Arrays.stream(equation1).asDoubleStream().toArray();

        int[][] equation2 = new int[][] {{1,1,0,0,0},{1,0,0,1,0}};
        double[][] equation2bis = new double[][] {{1,1,0,0,0},{1,0,0,1,0}};

        // n endogenous variables, n exogenous variables
        int[] exogenous = new int[n];
        int[] endogenous = new int[n];

        exogenous[0] = chain.addVariable(3);
        endogenous[0] = chain.addVariable(2);
        for(int k=1; k<n; k++){
            exogenous[k] = chain.addVariable(5);
            endogenous[k] = chain.addVariable(2);}

        chain.addParent(endogenous[0], exogenous[0]);

        // n CPTs for endogenous, n credal sets for exogenous
        SeparateHalfspaceFactor[] cpt = new SeparateHalfspaceFactor[n];
        SeparateHalfspaceFactor[] credalset = new SeparateHalfspaceFactor[n];

        cpt[0] = new SeparateHalfspaceFactor(chain.getDomain(endogenous[0]), chain.getDomain(exogenous[0]));
        credalset[0] = new SeparateHalfspaceFactor(chain.getDomain(exogenous[0]), Strides.empty());

        for (int k = 1; k < n; k++) {
            chain.addParents(endogenous[k], endogenous[k - 1], exogenous[k]);
            cpt[k] = new SeparateHalfspaceFactor(chain.getDomain(endogenous[k]), chain.getDomain(endogenous[k - 1], exogenous[k]));
            credalset[k] = new SeparateHalfspaceFactor(chain.getDomain(exogenous[k]), Strides.empty());}

        // Positivity and normalization
        for (int k = 0; k < 3; k++) {
            cpt[0].addConstraint(indicator(-1,2), Relationship.EQ, 1, k);
            for (int j = 0; j < 2; j++)
                cpt[0].addConstraint(indicator(j, 2), Relationship.GEQ, 0, k); }
        for (int k = 0; k < 3; k++)
            cpt[0].addConstraint(indicator(equation1[k], 3), Relationship.EQ, 1, k);
        chain.setFactor(endogenous[0], cpt[0]);

        // Positivity
        for (int k = 0; k < 3; k++)
            credalset[0].addConstraint(indicator(k, 3), Relationship.GEQ, 0);
        credalset[0].addConstraint(indicator(-1,3), Relationship.EQ, 1);
        credalset[0].addConstraint(equation1bis, Relationship.EQ, p);
        chain.setFactor(exogenous[0], credalset[0]);

        // Normalization and constraints
        for (int rr = 1; rr < n; rr++) {
            for (int k = 0; k < 5; k++) { // U
                for (int j = 0; j < 2; j++) { // X
                    for (int i = 0; i < 2; i++) // X
                        cpt[rr].addConstraint(indicator(i, 2), Relationship.GEQ, 0, j, k);
                    cpt[rr].addConstraint(indicator(-1,2), Relationship.EQ, 1, j, k); }}
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 5; j++)
                    cpt[rr].addConstraint(indicator(equation2[i][j], 2), Relationship.EQ, 1, i, j); }
            chain.setFactor(endogenous[rr], cpt[rr]);

            for (int k = 0; k < 5; k++)
                credalset[rr].addConstraint(indicator(k, 5), Relationship.GEQ, 0);
            credalset[rr].addConstraint(indicator(-1,5), Relationship.EQ, 1);
            credalset[rr].addConstraint(equation2bis[0], Relationship.EQ, 0.4);
            credalset[rr].addConstraint(equation2bis[1], Relationship.EQ, 0.3);
            chain.setFactor(exogenous[rr], credalset[rr]);}

        Inference ve = new Inference();
        long start = System.nanoTime();
        double[] lower = ve.query(chain, endogenous[n-1]).getLower();
        double[] upper = ve.query(chain, endogenous[n-1]).getUpper();
        long finish = System.nanoTime();
        long timeElapsed = (finish - start)/1000000000;
        for(int k=0; k<lower.length; k++)
            System.out.format("P(query=%d|VE)=[%2.4f,%2.4f]\t(T=%d sec)\n",k,lower[k],upper[k],timeElapsed);

        ApproxLP2 approxlp = new ApproxLP2();
        start = System.nanoTime();
        lower = approxlp.query(chain, endogenous[n-1]).getLower();
        upper = approxlp.query(chain, endogenous[n-1]).getUpper();
        finish = System.nanoTime();
        timeElapsed = (finish - start)/1000000000;
        for(int k=0; k<lower.length; k++)
            System.out.format("P(query=%d|LP)=[%2.4f,%2.4f]\t(T=%d sec)\n",k,lower[k],upper[k],timeElapsed);
    }}
