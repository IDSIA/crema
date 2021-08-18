package ch.idsia.crema.factor.convert;

import ch.idsia.crema.factor.Converter;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.separate.SeparateHalfspaceFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;

public class HalfspaceToRandomBayesianFactor implements Converter<SeparateHalfspaceFactor, BayesianFactor> {

	private boolean log = false;

	public boolean isLog() {
		return log;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	@Override
	public BayesianFactor apply(SeparateHalfspaceFactor s, Integer var) {
		final VertexFactor v = new HalfspaceToVertex().apply(s, var);
		final VertexToRandomBayesian vtrb = new VertexToRandomBayesian();
		vtrb.setLog(log);
		return vtrb.apply(v, var);
	}

	@Override
	public Class<BayesianFactor> getTargetClass() {
		return BayesianFactor.class;
	}

	@Override
	public Class<SeparateHalfspaceFactor> getSourceClass() {
		return SeparateHalfspaceFactor.class;
	}

}
