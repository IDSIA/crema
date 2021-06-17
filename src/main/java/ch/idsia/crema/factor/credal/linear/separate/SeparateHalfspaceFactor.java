package ch.idsia.crema.factor.credal.linear.separate;

import gnu.trove.map.TIntObjectMap;
import org.apache.commons.math3.optim.linear.LinearConstraint;

import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    26.04.2021 17:13
 */
public interface SeparateHalfspaceFactor extends SeparateLinearFactor<SeparateHalfspaceFactor> {

	double[] getRandomVertex(int... states);

	TIntObjectMap<List<LinearConstraint>> getData();

	SeparateHalfspaceDefaultFactor getPerturbedZeroConstraints(double eps);

	SeparateHalfspaceDefaultFactor removeNormConstraints();

	SeparateHalfspaceDefaultFactor removeNonNegativeConstraints();

	SeparateHalfspaceDefaultFactor mergeCompatible();

	SeparateHalfspaceDefaultFactor sortParents();

}
