import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.convert.VertexToInterval;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.approxlp.Inference;
import ch.idsia.crema.inference.sepolyve.SePolyVE;
import ch.idsia.crema.inference.ve.FactorVariableElimination;
import ch.idsia.crema.inference.ve.VariableElimination;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.preprocess.BinarizeEvidence;
import ch.idsia.crema.preprocess.CutObserved;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.math3.optim.linear.Relationship;
import org.junit.Test;

import java.util.Arrays;

public class ExampleInferenceVE {

	@Test
	public void testRun() {

		SparseModel<VertexFactor> model = new SparseModel<>();

		int a = model.addVariable(2);
		int b = model.addVariable(2);
		int c = model.addVariable(2);

		int q_vars = a;
		int[] o_vars = new int[]{c};
		int[] o_states = new int[]{1};

		model.addParent(b,a);
		model.addParent(c,b);

		// P(A=0) = 0.3
		VertexFactor pa = new VertexFactor(model.getDomain(a), Strides.empty());
		model.setFactor(a, pa);
		pa.addVertex(new double[] { 0.3, 0.7});
		pa.addVertex(new double[] { 0.4, 0.6});

		VertexFactor pb = new VertexFactor(model.getDomain(b), model.getDomain(a));
		pb.addVertex(new double[] { 0.2, 0.8}, 0);
		pb.addVertex(new double[] { 0.3, 0.7}, 0);
		pb.addVertex(new double[] { 0.1, 0.9}, 1);
		pb.addVertex(new double[] { 0.15, 0.85}, 1);
		model.setFactor(b, pb);

		VertexFactor pc = new VertexFactor(model.getDomain(c), model.getDomain(b));
		pc.addVertex(new double[] { 0.2, 0.8}, 0);
		pc.addVertex(new double[] { 0.3, 0.7}, 0);
		pc.addVertex(new double[] { 0.1, 0.9}, 1);
		pc.addVertex(new double[] { 0.15, 0.85}, 1);
		model.setFactor(c, pc);

		VariableElimination ve = new FactorVariableElimination(model.getVariables());
		//VariableElimination ve = new FactorVariableElimination(new int[]{a,b,c});
		TIntIntMap evidence = new TIntIntHashMap();
		for(int i=0; i<o_vars.length; i++)
			evidence.put(o_vars[i], o_states[i]);
		ve.setEvidence(evidence);
		ve.setNormalize(false);
		ve.setFactors(model.getFactors());
		VertexFactor result = (VertexFactor)ve.run(q_vars);
		VertexFactor result2 = result.normalize();
		System.out.println(result2);

		CutObserved preprocesser = new CutObserved();
		//SparseModel model2 = preprocesser.execute(model,evidence)
		//for(int k=0; k<vd.getVertices().length;k++)
		//	System.out.println(Arrays.toString(vd.getVertices()[k]));
		//System.out.println(vd.getVertices()[0][0]);
		//System.out.println(vd.getVertices()[0][1]);
		//VertexToInterval converter = new VertexToInterval();
		//SparseModel<IntervalFactor> model2 = model.convert(converter);
		//new RemoveBarren().executeInline(model2, new int[] {b}, new TIntIntHashMap() {{
		//	put(a, 1);
		//}});
		//BinarizeEvidence bin = new BinarizeEvidence();
		
	}

}
