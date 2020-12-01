package ch.idsia.crema.factor.convert;

import ch.idsia.crema.core.Converter;
import ch.idsia.crema.factor.credal.linear.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.set.HCredalSet;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.javasoft.polco.adapter.Options;
import ch.javasoft.polco.adapter.PolcoAdapter;
import ch.javasoft.xml.config.XmlConfigException;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class HalfspaceToVertex implements Converter<SeparateHalfspaceFactor, VertexFactor> {

//	/**
//	 * Inequalities enlargement eps
//	 */
//	private static final double EPS = 0.0000000000000002;

	private PolcoAdapter polco;

	@Override
	public VertexFactor apply(SeparateHalfspaceFactor s, Integer var) {
		VertexFactor vfactor = new VertexFactor(s.getDataDomain(), s.getSeparatingDomain());

		// polco will convert to VCredalSet
		for (int comb = 0; comb < s.getSeparatingDomain().getCombinations(); ++comb) {
			Collection<LinearConstraint> set = s.getLinearProblemAt(comb).getConstraints();
			double[][] inequalities = toDoubleArrays(set, s.getDataDomain().getCombinations());
			double[][] vertices = polcoToVertices(inequalities);

			for (double[] v : vertices)
				vfactor.addVertex(v, comb);
		}

		return vfactor;
	}

	@Override
	public Class<VertexFactor> getTargetClass() {
		return VertexFactor.class;
	}

	@Override
	public Class<SeparateHalfspaceFactor> getSourceClass() {
		return SeparateHalfspaceFactor.class;
	}

	public HalfspaceToVertex() {
		try {
			Options opt = new Options();
			opt.setLoglevel(Level.OFF);
			opt.setLogFile(new File("polco.log"));
			this.polco = new PolcoAdapter(opt);
		} catch (XmlConfigException ignored) {
			// TODO: ignore?
		}
	}

	private double[][] polcoToVertices(double[][] inequalities) {
		double[][] correctedVertices = polco.getDoubleRays(null, inequalities);

		if (correctedVertices.length == 0)
			return correctedVertices;

		List<double[]> vertices = new ArrayList<>(correctedVertices[0].length - 1);
		// double[][] vertices = new double[correctedVertices.length][correctedVertices[0].length - 1];
		for (double[] correctedVertex : correctedVertices) {
			double[] row = new double[correctedVertices[0].length - 1];
			boolean origin = true;
			for (int j = 0; j < row.length; j++) {
				origin &= correctedVertex[j + 1] == 0.0;
				if (correctedVertex[0] > 0) {
					row[j] = correctedVertex[j + 1] / correctedVertex[0];
				} else {
					row[j] = correctedVertex[j + 1];
				}
			}
			if (!origin)
				vertices.add(row);
			else
				System.out.println("Removed 000");
		}

		return vertices.toArray(new double[0][]);
	}

	/**
	 * Convert the provided H-rep credal set's inequalities to a double matrix
	 * of values.
	 * <p>
	 * The method uses an internal ordering but will NOT change the order of
	 * arrays in the model.
	 * <p>
	 * WARNING: polco uses >= 0
	 *
	 * @param input the {@link HCredalSet} to be converted
	 * @return the data matrix as a double[][]
	 */
	private double[][] toDoubleArrays(Collection<LinearConstraint> input, int states) {
		List<double[]> doubleInequalities = new ArrayList<>();

		// lets use an iterator on the list to avoid continuous access if list is not random
		for (LinearConstraint constraint : input) {
			if (constraint.getRelationship() == Relationship.GEQ || constraint.getRelationship() == Relationship.EQ) {
				double[] data = new double[states + 1];
				double[] v = constraint.getCoefficients().toArray();
				data[0] = -constraint.getValue();
				System.arraycopy(v, 0, data, 1, v.length);
				doubleInequalities.add(data);
			}

			if (constraint.getRelationship() == Relationship.LEQ || constraint.getRelationship() == Relationship.EQ) {
				double[] data = new double[states + 1];
				double[] v = constraint.getCoefficients().toArray();
				data[0] = constraint.getValue();
				for (int i = 0; i < v.length; ++i) {
					data[i + 1] = -v[i];
				}
				doubleInequalities.add(data);
			}
		}
		return doubleInequalities.toArray(new double[0][]);
	}

}
