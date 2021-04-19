package ch.idsia.crema.factor.bayesian;

import ch.idsia.crema.core.Domain;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.model.vertex.Filter;
import ch.idsia.crema.model.vertex.LogMarginal;
import ch.idsia.crema.utility.ArraysUtil;
import ch.idsia.crema.utility.IndexIterator;
import org.apache.commons.math3.util.FastMath;

import java.util.Arrays;
import java.util.function.ToDoubleBiFunction;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    15.04.2021 18:17
 */
public class BayesianLogFactor extends BayesianDefaultFactor {

	protected final ToDoubleBiFunction<BayesianFactor, Integer> direct = (f, i) -> ((BayesianLogFactor) f).data[i];
	protected final ToDoubleBiFunction<Double, Double> addInLogSpace = (x, y) -> {
		if (x > y)
			return x + FastMath.log1p(FastMath.exp(y - x));
		return y + FastMath.log1p(FastMath.exp(x - y));
	};

	public BayesianLogFactor(Domain domain, double[] data) {
		super(domain, data);
	}

	public BayesianLogFactor(int[] domain, int[] sizes, double[] data) {
		super(domain, sizes, data);
	}

	public BayesianLogFactor(Strides stride, double[] data) {
		super(stride, data);
	}

	@Override
	public BayesianLogFactor copy() {
		return new BayesianLogFactor(domain, data.clone());
	}

	@Override
	public void setData(double[] data) {
		for (int index = 0; index < data.length; ++index) {
			setValueAt(data[index], index);
		}
	}

	public double[] getData() {
		double[] data = new double[this.data.length];
		for (int index = 0; index < data.length; ++index) {
			data[index] = FastMath.exp(this.data[index]);
		}
		return data;
	}

	@Override
	public void setValueAt(double value, int index) {
		data[index] = FastMath.log(value);
	}

	@Override
	public double getValueAt(int index) {
		return FastMath.exp(data[index]);
	}

	@Override
	public BayesianLogFactor filter(int variable, int state) {
		int offset = domain.indexOf(variable);
		return collect(offset, new Filter(domain.getStrideAt(offset), state), BayesianLogFactor::new);
	}

	@Override
	public BayesianLogFactor marginalize(int variable) {
		int offset = domain.indexOf(variable);
		if (offset == -1) return this;
		return collect(offset, new LogMarginal(domain.getSizeAt(offset), domain.getStrideAt(offset)), BayesianLogFactor::new);
	}

	@Override
	public BayesianLogFactor combineIterator(BayesianDefaultFactor cpt) {
		Strides target = domain.union(cpt.domain);

		IndexIterator i1 = getDomain().getIterator(target);
		IndexIterator i2 = cpt.getDomain().getIterator(target);

		double[] result = new double[target.getCombinations()];

		for (int i = 0; i < result.length; ++i) {
			result[i] = data[i1.next()] + cpt.data[i2.next()];
		}

		return new BayesianLogFactor(target, result);
	}

	@Override
	public BayesianLogFactor combine(BayesianFactor factor) {
		if (factor instanceof BayesianLogFactor)
			return combine(factor, BayesianLogFactor::new, direct, direct, Double::sum);

		return combine(factor, BayesianLogFactor::new, direct, BayesianFactor::getValueAt, Double::sum);
	}

	@Override
	public BayesianLogFactor addition(BayesianFactor factor) {
		if (factor instanceof BayesianLogFactor)
			return combine(factor, BayesianLogFactor::new, direct, direct, addInLogSpace);

		return combine(factor, BayesianLogFactor::new, direct, BayesianFactor::getValueAt, addInLogSpace);
	}

	@Override
	public BayesianLogFactor divide(BayesianFactor factor) {
		if (factor instanceof BayesianLogFactor)
			return divide(factor, BayesianLogFactor::new, direct, direct, (a, b) -> a - b);

		return divide(factor, BayesianLogFactor::new, direct, BayesianFactor::getValueAt, (a, b) -> a - b);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BayesianLogFactor)) return false;

		BayesianLogFactor other = (BayesianLogFactor) obj;
		if (!Arrays.equals(domain.getVariables(), other.getDomain().getVariables())) return false;

		return ArraysUtil.almostEquals(data, other.data, 1.0E-08);
	}

	@Override
	public void replaceInplace(double value, double replacement) {
		for (int i = 0; i < getData().length; i++)
			if (getData()[i] == value)
				setValueAt(replacement, i);
	}

	@Override
	public BayesianLogFactor replace(double value, double replacement) {
		BayesianLogFactor f = this.copy();
		f.replaceInplace(value, replacement);
		return f;
	}

	@Override
	public BayesianLogFactor replaceNaN(double replacement) {
		BayesianLogFactor f = this.copy();
		for (int i = 0; i < f.getData().length; i++)
			if (Double.isNaN(f.getData()[i]))
				setValueAt(replacement, i);
		return f;
	}


}
