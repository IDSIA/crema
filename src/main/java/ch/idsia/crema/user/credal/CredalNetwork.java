package ch.idsia.crema.user.credal;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.idsia.crema.user.core.Variable;

@Deprecated
@XmlRootElement(name="cn")
@XmlAccessorType(XmlAccessType.FIELD)
public class CredalNetwork {
	private Collection<Variable> variables;
	
	public CredalNetwork() {
		variables = new ArrayList<>();
	}
	
	public Variable createVariable(int states) {
		return new Variable();
	}
}
