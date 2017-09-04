package ch.idsia.crema.user.bayesian;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.SparseModel;

import javax.xml.bind.annotation.XmlTransient;

public class BayesianNetwork {
	@XmlTransient
	private SparseModel<BayesianFactor> innermodel;
	
}
