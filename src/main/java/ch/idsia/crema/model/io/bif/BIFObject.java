package ch.idsia.crema.model.io.bif;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.BayesianNetwork;

import java.util.HashMap;
import java.util.Map;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    25.01.2021 09:38
 */
public class BIFObject {

	public String name;
	public BayesianNetwork network = new BayesianNetwork();
	public BayesianFactor[] factors = null;

	public Map<String, Integer> variableName = new HashMap<>();
	public Map<String, Integer> variableStates = new HashMap<>();
	public Map<String, BayesianFactor> variableFactors = new HashMap<>();

}
