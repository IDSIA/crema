package ch.idsia.crema.factor.credal.linear;

import ch.idsia.crema.factor.FilterableFactor;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.SeparatelySpecified;
import ch.idsia.crema.model.Strides;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * An abstract implementation of the {@link SeparateFactor} interface.
 * 
 * @author david
 * @param <F>
 *            the managed type. This is usually the type itself.
 */
public abstract class SeparateFactor<F extends SeparateFactor<F>> implements FilterableFactor<F>, GenericFactor, SeparatelySpecified<F> {

	protected Strides dataDomain;

	protected Strides groupDomain;

	public SeparateFactor() {
	}

	
	public SeparateFactor(Strides dataDomain, Strides groupDomain) {
		setConditioningDomain(groupDomain);
		setDataDomain(dataDomain);
	}
	
	@XmlAttribute(name="domain")
	public void setDataDomain(Strides dataDomain) {
		this.dataDomain = dataDomain;
	}

	public void setConditioningDomain(Strides groupDomain) {
		if (groupDomain == null) {
			groupDomain = new Strides(new int[0], new int[0]);
		}
		this.groupDomain = groupDomain;
	}

	@Override
	public Strides getDataDomain() {
		return dataDomain;
	}

	@Override
	public Strides getSeparatingDomain() {
		return groupDomain;
	}

	@Override
	public Strides getDomain() {
		return dataDomain.union(groupDomain);
	}

	@Override
	public abstract F copy();
}
