package ch.idsia.crema.factor.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;

import ch.idsia.crema.factor.credal.linear.ExtensiveHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.ExtensiveVertexFactor;
import ch.javasoft.polco.adapter.PolcoAdapter;
import ch.javasoft.xml.config.XmlConfigException;

@Deprecated
public class ExtensiveVectorToExtensiveHalfspace {
	private PolcoAdapter polco;

	public ExtensiveVectorToExtensiveHalfspace() throws XmlConfigException {
		polco = new PolcoAdapter();
	}

//	private SeparateLinearToHalfspaceFactor toHCredalSet(ExtensiveVertexFactor vCredalSet) {
//
//		if (vCredalSet.getInternalVertices().size() == 1) {
//			// double[][] vert = toDoubleArrays(vCredalSet);
//			// double[][] ineq = toInequalities(vert);
//			// only one point
//			return toPreciseHCredalSet(vCredalSet);
//		} else if (vCredalSet.getNode() != null && vCredalSet.getNode().getVariable().getStates().size() == 2) {
//			RCredalSet rset = toRCredalSet(vCredalSet);
//			return (HCredalSet) Platform.getAdapterManager().getAdapter(rset, HCredalSet.class);
//		} else {
//
//			double[][] vert = toDoubleArrays(vCredalSet);
//			double[][] ineq = toInequalities(vert);
//			return toHCredalSet(ineq, vCredalSet);
//		}
//	}
//
//	/**
//	 * Convert the provided matrix of values to a Linear constraints Set 
//	 * Note that polco uses B'x <= b 
//	 */
//	private ExtensiveHalfspaceFactor toHCredalSet(double[][] ineq, ExtensiveVertexFactor input) {
//		
//		List<LinearConstraint> contraints = Arrays.stream(ineq).map(
//			inequalityArray -> new LinearConstraint(
//				/* coefficients */ ArrayUtils.subarray(inequalityArray, 1, Integer.MAX_VALUE), 
//				/* relationship */ Relationship.LEQ, 
//				/* value */        inequalityArray[0])
//			).collect(Collectors.toList());
//
//		
//		return output;
//	}
//
//	private Collection<> sumToOne(ExtensiveVertexFactor input) {
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
//	/**
//	 * Convert the provided H-rep credal set's inequalities to a double matrix of
//	 * values.
//	 * <p>
//	 * The method uses an internal ordering but will NOT change the order of arrays
//	 * in the model.
//	 * 
//	 * @param input the {@link HCredalSet} to be converted
//	 * @return the data matrix as a double[][]
//	 */
//	private double[][] toDoubleArrays(VCredalSet input) {
//		List<Point> points = input.getPoints();
//		Iterator<Point> pointsIterator = points.iterator();
//
//		List<State> nodeStates = new ArrayList<State>(input.getNode().getVariable().getStates());
//		Collections.sort(nodeStates, statesComparator);
//
//		double[][] doubleVertices = new double[points.size()][];
//
//		// lets use an iterator on the list to avoid contiuous access if list is nor
//		// random
//		for (int pointIndex = 0; pointIndex < points.size(); ++pointIndex) {
//			Point point = pointsIterator.next();
//
//			// lets work on a copy
//			List<StateValue> stateValues = new ArrayList<StateValue>(point.getStateValues());
//			Collections.sort(stateValues, stateValuesComparator);
//
//			doubleVertices[pointIndex] = new double[nodeStates.size() + 1];
//			doubleVertices[pointIndex][0] = 1;
//
//			// we should at least have one state value in the inequality
//			Iterator<StateValue> stateValuesIterator = stateValues.iterator();
//			StateValue stateValue = stateValuesIterator.next();
//
//			// copy the data into the array
//			int stateValueIndex = 0;
//			for (State state : nodeStates) {
//				if (state == stateValue.getState()) {
//					doubleVertices[pointIndex][stateValueIndex + 1] = stateValue.getValue();
//					if (stateValuesIterator.hasNext()) {
//						// statevalues are sorted by state name so is the nodeStates array
//						stateValue = stateValuesIterator.next();
//					} else {
//						// no need to continue
//						break;
//					}
//				}
//				stateValueIndex++; // go to next
//			}
//		}
//		return doubleVertices;
//	}
//
//	private double[][] toInequalities(double[][] vertices) {
////		double[][] correctedVertices = new double[vertices.length][vertices[0].length + 1];
////		for (int i = 0; i < correctedVertices.length; i++) {
////			correctedVertices[i][0] = 1.0;
////			System.arraycopy(vertices[i], 0, correctedVertices[i], 1, vertices[i].length);
////		}
//
//		return polco.getDoubleRays(null, vertices);
//	}

}
