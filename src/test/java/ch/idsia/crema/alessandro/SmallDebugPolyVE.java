package ch.idsia.crema.alessandro;

import java.util.Arrays;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.inference.sepolyve.SePolyVE;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.preprocess.RemoveBarren;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class SmallDebugPolyVE {

	public static void main(String[] args) {
		SmallDebugPolyVE myTest = new SmallDebugPolyVE();
		myTest.miniTest();
		System.out.println("Done!");}

	// -----------------------------
	// -----------------------------

	public void miniTest() {

		DAGModel<VertexFactor> model = new DAGModel<>();		
		int[] nodes = new int[2];
		Strides[] domain = new Strides[2];
		for(int i=0;i<2;i++){
			nodes[i] = model.addVariable(2);
			domain[i] = Strides.as(nodes[i],2);}
		VertexFactor[] K = new VertexFactor[2];
		K[0] = new VertexFactor(domain[0],Strides.EMPTY);
		K[1] = new VertexFactor(domain[1],domain[0]);
		K[0].addVertex(new double[] {.1,.9});
		K[0].addVertex(new double[] {.2,.8});
		K[1].addVertex(new double[] {.8,.2},0);
		K[1].addVertex(new double[] {.9,.1},0);
		K[1].addVertex(new double[] {.2,.8},1);
		K[1].addVertex(new double[] {.3,.7},1);				
		for(int i=0;i<2;i++){
			model.setFactor(nodes[i],K[i]);}
		TIntIntMap evidence = new TIntIntHashMap();
		evidence.put(nodes[1],0);
		DAGModel<VertexFactor> model2 = new RemoveBarren().execute(model,nodes[0],evidence);
		//System.out.println(new DotSerialize().run(model2));		
		SePolyVE ve = new SePolyVE();
		VertexFactor factor = ve.run(model2,nodes[0],evidence);
		for (double[] v : factor.getVertices()) {
			System.out.println("vertex: "+Arrays.toString(v));}}}
