package ch.idsia.crema.factor.credal.linear.interval;

import ch.idsia.crema.core.Strides;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    22.04.2021 17:38
 */
public class IntervalFactorFactory {

	private Strides dataDomain = Strides.empty(); // content
	private Strides groupDomain = Strides.empty(); // separation

	TIntObjectMap<double[]> lowers = new TIntObjectHashMap<>();
	TIntObjectMap<double[]> uppers = new TIntObjectHashMap<>();

	private IntervalFactorFactory() {
	}

	public static IntervalFactorFactory factory() {
		return new IntervalFactorFactory();
	}

	public IntervalFactorFactory separation(Strides separation) {
		return groupDomain(separation);
	}

	public IntervalFactorFactory groupDomain(Strides groupDomain) {
		this.groupDomain = groupDomain;
		return this;
	}

	public IntervalFactorFactory content(Strides content) {
		return dataDomain(content);
	}

	public IntervalFactorFactory dataDomain(Strides dataDomain) {
		this.dataDomain = dataDomain;
		return this;
	}

	public IntervalFactorFactory domain(Strides content, Strides separation) {
		this.groupDomain = content;
		this.dataDomain = separation;
		return this;
	}

	public IntervalFactorFactory set(double[] lowers, double[] uppers, int... states) {
		int offset = groupDomain.getOffset(states);
		this.lowers.put(offset, lowers);
		this.uppers.put(offset, uppers);
		return this;
	}

	public IntervalFactorFactory lower(double[] lower, int... states) {
		int offset = groupDomain.getOffset(states);
		this.lowers.put(offset, lower);
		return this;
	}

	public IntervalFactorFactory upper(double[] upper, int... states) {
		int offset = groupDomain.getOffset(states);
		this.uppers.put(offset, upper);
		return this;
	}

	public IntervalFactorFactory bounds(double lower, double upper, int dataOffset, int... given) {
		int offset = groupDomain.getOffset(given);

		if (!lowers.containsKey(offset))
			lowers.put(offset, new double[dataDomain.getCombinations()]);
		lowers.get(offset)[dataOffset] = lower;

		if (!uppers.containsKey(offset))
			uppers.put(offset, new double[dataDomain.getCombinations()]);
		uppers.get(offset)[dataOffset] = upper;

		return this;
	}

	public IntervalFactor build() {
		double[][] lower = new double[groupDomain.getCombinations()][dataDomain.getCombinations()];
		double[][] upper = new double[groupDomain.getCombinations()][dataDomain.getCombinations()];

		for (int i = 0; i < groupDomain.getCombinations(); ++i)
			Arrays.fill(upper[i], 1.0);

		for (int v : lowers.keys())
			lower[v] = lowers.get(v);
		for (int v : uppers.keys())
			lower[v] = uppers.get(v);

		return new IntervalDefaultFactor(groupDomain, dataDomain, lower, upper);
	}

}
