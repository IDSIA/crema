import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.math3.optim.linear.Relationship;

public class ExampleInferenceApproxLP {

    public static void logger(IntervalFactor f){
        double[] upper = f.getUpper();
        double[] lower = f.getLower();
        for(int k=0; k<upper.length; k++)
            System.out.format("P(X=%d) = %2.4f - %2.4f\n",k,lower[k],upper[k]);
    }

    public static void main(String[] args) throws InterruptedException {

        SparseModel<GenericFactor> model = new SparseModel<>();
        int a = model.addVariable(2);
        int b = model.addVariable(2);
        int c = model.addVariable(2);
        model.addParent(b,a);
        model.addParent(c,b);

        // P(A=0) = 0.3
        SeparateHalfspaceFactor pa = new SeparateHalfspaceFactor(model.getDomain(a), Strides.empty());
        pa.addConstraint(new double[]{1,0}, Relationship.GEQ, 0);
        pa.addConstraint(new double[]{0,1}, Relationship.GEQ, 0);
        pa.addConstraint(new double[]{1,1}, Relationship.EQ, 1);
        pa.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.3);
        pa.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.7);
        model.setFactor(a, pa);

        // P(B=0|A=0) = 0.2
        // P(B=0|A=1) = 0.1
        SeparateHalfspaceFactor pb = new SeparateHalfspaceFactor(model.getDomain(b),model.getDomain(a));
        pb.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 0);
        pb.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 0);
        pb.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 0);
        pb.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.2,0);
        pb.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.8,0);
        pb.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 1);
        pb.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 1);
        pb.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 1);
        pb.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.1,1);
        pb.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.8,1);
        model.setFactor(b, pb);

        // P(C=0|B=0) = 0.2
        // P(C=0|B=1) = 0.1
        SeparateHalfspaceFactor pc = new SeparateHalfspaceFactor(model.getDomain(c),model.getDomain(b));
        pc.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 0);
        pc.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 0);
        pc.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 0);
        pc.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.2,0);
        pc.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.8,0);
        pc.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 1);
        pc.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 1);
        pc.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 1);
        pc.addConstraint(new double[]{1,0}, Relationship.EQ, 0.1,1);
        //pc.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.8,1);
        model.setFactor(c, pc);

        TIntIntMap evidence = new TIntIntHashMap();
        evidence.put(b, 0);
        BinarizeEvidence bin = new BinarizeEvidence();
        int ev = bin.executeInline(model, evidence, 2, false);
        Inference inference = new Inference();
        IntervalFactor factor = inference.query(model, c, ev);
        logger(factor);
        evidence.put(b, 1);
        evidence.put(a, 1);
        ev = bin.executeInline(model, evidence, 2, false);
        factor = inference.query(model, c, ev);
        logger(factor);

        Inference approxlp = new Inference();
        double[] lower = approxlp.query(model, c).getLower();
        double[] upper = approxlp.query(model, c).getUpper();
        for(int k=0; k<upper.length; k++)
            System.out.format("P(X=%d) = %2.4f - %2.4f\n",k,lower[k],upper[k]);

        // -------------------------

        SparseModel<GenericFactor> model2 = new SparseModel<>();
        int a2 = model2.addVariable(2);
        int b2 = model2.addVariable(2);
        int c2 = model2.addVariable(2);
        model2.addParent(c2,a2);
        model2.addParent(c2,b2);

        // P(A=0) = 0.3
        SeparateHalfspaceFactor pa2 = new SeparateHalfspaceFactor(model2.getDomain(a2), Strides.empty());
        pa2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0);
        pa2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0);
        pa2.addConstraint(new double[]{1,1}, Relationship.EQ, 1);
        pa2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.3);
        pa2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.7);
        model2.setFactor(a2, pa2);

        // P(B=0) = 0.3
        SeparateHalfspaceFactor pb2 = new SeparateHalfspaceFactor(model2.getDomain(b2), Strides.empty());
        pb2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0);
        pb2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0);
        pb2.addConstraint(new double[]{1,1}, Relationship.EQ, 1);
        pb2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0.3);
        pb2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0.7);
        model2.setFactor(b2, pb2);

        // P(C=0) = 0.3
        SeparateHalfspaceFactor pc2 = new SeparateHalfspaceFactor(model2.getDomain(c2), model2.getDomain(a2,b2));
        pc2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 0);
        pc2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 0);
        pc2.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 0);

        pc2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 1);
        pc2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 1);
        pc2.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 1);

        pc2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 2);
        pc2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 2);
        pc2.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 2);

        pc2.addConstraint(new double[]{1,0}, Relationship.GEQ, 0, 3);
        pc2.addConstraint(new double[]{0,1}, Relationship.GEQ, 0, 3);
        pc2.addConstraint(new double[]{1,1}, Relationship.EQ, 1, 3);

        pc2.addConstraint(new double[]{1,0}, Relationship.EQ, 0.6, 0 );
        pc2.addConstraint(new double[]{1,0}, Relationship.EQ, 0.5, 1 );
        pc2.addConstraint(new double[]{1,0}, Relationship.EQ, 0.5, 2 );
        pc2.addConstraint(new double[]{1,0}, Relationship.EQ, 0.5, 3 );
        model2.setFactor(c2, pc2);

        System.out.println("aa");
        TIntIntMap evidence2 = new TIntIntHashMap();
        evidence2.put(b2, 0);
        evidence2.put(a2, 0);
        BinarizeEvidence bin2 = new BinarizeEvidence();
        int ev2 = bin2.executeInline(model2, evidence2, 2, false);
        Inference inference2 = new Inference();
        IntervalFactor factor2 = inference2.query(model2, c2, ev2);
        logger(factor2);
        //evidence2.put(b2, 1);
        //evidence2.put(a2, 1);
        //ev2 = bin2.executeInline(model2, evidence2, 2, false);
        //factor2 = inference.query(model2, c2, ev2);
        //logger(factor2);

        //Inference approxlp2 = new Inference();
        //double[] lower2 = approxlp2.query(model2, c2).getLower();
        //double[] upper2 = approxlp2.query(model2, c2).getUpper();
        //for(int k=0; k<upper.length; k++)
        //    System.out.format("P(X=%d) = %2.4f - %2.4f\n",k,lower2[k],upper2[k]);




    }}
