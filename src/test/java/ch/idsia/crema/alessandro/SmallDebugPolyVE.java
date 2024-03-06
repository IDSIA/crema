package ch.idsia.crema.alessandro;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.inference.sepolyve.SePolyVE;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.RemoveBarren;


import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.Arrays;

public class SmallDebugPolyVE {

	public static void main(String[] args) {
		SmallDebugPolyVE myTest = new SmallDebugPolyVE();
		myTest.miniTest();
		System.out.println("Done!");
	}

	// -----------------------------
	// -----------------------------

	public void miniTest() {
		DAGModel<VertexFactor> model = new DAGModel<>();
		int[] nodes = new int[2];
		Strides[] domain = new Strides[2];
		for (int i = 0; i < 2; i++) {
			nodes[i] = model.addVariable(2);
			domain[i] = Strides.as(nodes[i], 2);
		}
		VertexFactor[] K = new VertexFactor[2];

		K[0] = VertexFactorFactory.factory().domain(domain[0])
				.addVertex(new double[]{.1, .9})
				.addVertex(new double[]{.2, .8})
				.get();

		K[1] = VertexFactorFactory.factory().domain(domain[1], domain[0])
				.addVertex(new double[]{.8, .2}, 0)
				.addVertex(new double[]{.9, .1}, 0)
				.addVertex(new double[]{.2, .8}, 1)
				.addVertex(new double[]{.3, .7}, 1)
				.get();

		for (int i = 0; i < 2; i++) {
			model.setFactor(nodes[i], K[i]);
		}
		Int2IntMap evidence = new Int2IntOpenHashMap();
		evidence.put(nodes[1], 0);
		final RemoveBarren<VertexFactor> removeBarren = new RemoveBarren<>();
		GraphicalModel<VertexFactor> model2 = removeBarren.execute(model, evidence, nodes[0]);
		//System.out.println(new DotSerialize().run(model2));		
		SePolyVE ve = new SePolyVE();
		VertexFactor factor = ve.query(model2, evidence, nodes[0]);
		for (double[] v : factor.getVertices()) {
			System.out.println("vertex: " + Arrays.toString(v));
		}
	}
}
