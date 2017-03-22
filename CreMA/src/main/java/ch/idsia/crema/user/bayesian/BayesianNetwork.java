package ch.idsia.crema.user.bayesian;

import javax.xml.bind.annotation.XmlTransient;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;

public class BayesianNetwork {
	@XmlTransient
	private SparseModel<BayesianFactor> innermodel;
	
}
