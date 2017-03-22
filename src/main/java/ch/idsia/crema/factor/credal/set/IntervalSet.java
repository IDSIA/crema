package ch.idsia.crema.factor.credal.set;

import javax.xml.bind.annotation.XmlTransient;

import ch.idsia.crema.model.Strides;

public class IntervalSet extends AbstractSet {
	private double[] lowers;
	private double[] uppers;

	public IntervalSet() {
	}
	
	/**
	 * Creates an interval based set over the specified domain. Intervals are 
	 * left undefined (null).
	 * 
	 * @param domain
	 */
	public IntervalSet(Strides domain) {
		super(domain);
	}
	
	/**
	 * Initialize the Set with all the data.
	 * @param domain
	 * @param lowers
	 * @param uppers
	 */
	public IntervalSet(Strides domain, double[] lowers, double[] uppers) {
		super(domain);
		this.lowers = lowers;
		this.uppers = uppers;
	}
	
	public void setLowers(double[] lowers) {
		this.lowers = lowers;
	}
	
	public void setUppers(double[] uppers) {
		this.uppers = uppers;
	}
	
	public double[] getLowers() {
		return lowers;
	}
	
	public double[] getUppers() {
		return uppers;
	}

	
	@XmlTransient
	public BoundsHelper boundsFor(int... states) {		
		return new BoundsHelper(states);
	}

	/** 
	 * Helper class to allow the access to the bounds of a set through:
	 * 
	 * <code>credalSet.boundsFor(state1).set(0.2, 0.4);</code>
	 * 
	 * @author huber
	 */
	public class BoundsHelper {
		private int offset;
		public BoundsHelper(int... states) {
			offset = IntervalSet.this.domain.getOffset(states);
		}
		
		public void set(double lower, double upper) {
			if (IntervalSet.this.uppers == null) {
				IntervalSet.this.uppers = new double[domain.getCombinations()];
			}
			
			if (IntervalSet.this.lowers == null) {
				IntervalSet.this.lowers = new double[domain.getCombinations()];
			}
			
			IntervalSet.this.lowers[offset] = lower;
			IntervalSet.this.uppers[offset] = upper;
		}
	}

	@Override
	public IntervalSet copy() {
		IntervalSet set = new IntervalSet(getDomain());
		if (lowers != null) set.lowers = lowers.clone();
		if (uppers != null) set.uppers = uppers.clone();
		return set;
	}

}
