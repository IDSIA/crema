package ch.idsia.crema.factor.convert;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.optim.linear.Relationship;

import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Converter;
import ch.idsia.crema.model.Strides;
import ch.javasoft.lang.reflect.Array;
import ch.javasoft.polco.adapter.Options;
import ch.javasoft.polco.adapter.PolcoAdapter;
import ch.javasoft.xml.config.XmlConfigException;

public class VertexToHalfspace implements Converter<VertexFactor, SeparateHalfspaceFactor> {
	/**
	 * Inequalities enlargement eps
	 */
	// private static final double EPS = 0.0000000000000002;
	private PolcoAdapter polco;

	public VertexToHalfspace() {
		try {
			Options opt = new Options();
			opt.setLoglevel(Level.OFF);
			opt.setLogFile(new File("polco.log"));
			this.polco = new PolcoAdapter(opt);
		} catch (XmlConfigException e) {
		}
	}

	private static double[] enlarge(double[] in) {
		double[] out = new double[in.length + 1];
		System.arraycopy(in, 0, out, 1, in.length);
		out[0] = 1;
		return out;
	}

	@Override
	public SeparateHalfspaceFactor apply(VertexFactor input, Integer variable) {

		int combs = input.getSeparatingDomain().getCombinations();
		double[][][] coeffs = new double[combs][][];
		double[][] values = new double[combs][];

		combs = 0;
		for (double[][] vertices : input.getInternalData()) {
			double[][] v = Arrays.stream(vertices).<double[]>map(VertexToHalfspace::enlarge).toArray(double[][]::new);

			double[][] ineq = polco.getDoubleRays(null, v);

			coeffs[combs] = Arrays.stream(ineq).<double[]>map(a -> ArrayUtils.subarray(a, 0, a.length - 1))
					.toArray(double[][]::new);

			values[combs] = Arrays.stream(ineq).mapToDouble(a -> a[a.length - 1]).toArray();
			combs++;
			// warning polco uses B'x <= b
		}
		return new SeparateHalfspaceFactor(input.getDataDomain(), input.getSeparatingDomain(), coeffs, values,
				Relationship.LEQ);
	}

	public static void main(String args[]) {
		System.out.println("Ciao");
		VertexFactor in = new VertexFactor(Strides.as(0, 3), Strides.as(1, 3));
		in.addVertex(new double[] { 0.5, 0.5, 0 }, 0);
		in.addVertex(new double[] { 0, 1, 0 }, 0);
		in.addVertex(new double[] { 0, 0.5, 0.5 }, 0);

		in.addVertex(new double[] { 1, 0, 0 }, 1);
		in.addVertex(new double[] { 0, 1, 0 }, 1);
		in.addVertex(new double[] { 0, 0, 1 }, 1);

		in.addVertex(new double[] { 1, 0, 0 }, 2);
		in.addVertex(new double[] { 0, 1, 0 }, 2);
		in.addVertex(new double[] { 0, 0, 1 }, 2);
		in.addVertex(new double[] { 0.3, 0.4, 0.3 }, 2);

		VertexToHalfspace converter = new VertexToHalfspace();
		SeparateHalfspaceFactor out = converter.apply(in);

		out.printLinearProblem(0);
		System.out.println("test");
		out.printLinearProblem(1);
		System.out.println("test");

		out.printLinearProblem(2);

		HalfspaceToVertex conv2 = new HalfspaceToVertex();
		VertexFactor vf = conv2.apply(out);

		for (int i = 0; i < 3; ++i) {
			double[][] vertices = vf.getVertices(i);
			System.out.println(Arrays.deepToString(vertices));
		}
	

	}

//	private void addSumToOne(HCredalSet set) {
//		// we also have to set that the sum must be 1 and exactly 1
//		Inequality oneupp = CredalSetFactory.eINSTANCE.createInequality();
//		oneupp.setConstantTerm(-1);
//		
//		Inequality onelow = CredalSetFactory.eINSTANCE.createInequality();
//		onelow.setConstantTerm(1);
//		
//		for (State state : set.getNode().getVariable().getStates()) {
//			StateValue svupp = CredalSetFactory.eINSTANCE.createStateValue();
//			svupp.setState(state);
//			svupp.setValue(1);
//			oneupp.getStateValues().add(svupp);
//			
//			StateValue svlow = CredalSetFactory.eINSTANCE.createStateValue();
//			svlow.setState(state);
//			svlow.setValue(-1);
//			onelow.getStateValues().add(svlow);
//		}
//		set.getInequalities().add(oneupp);
//		set.getInequalities().add(onelow);
//	}
//	
	@Override
	public Class<SeparateHalfspaceFactor> getTargetClass() {
		return SeparateHalfspaceFactor.class;
	}

	@Override
	public Class<VertexFactor> getSourceClass() {
		return VertexFactor.class;
	}

}
