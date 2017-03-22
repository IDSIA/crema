package ch.idsia.crema.user.core;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "var")
@XmlAccessorType(XmlAccessType.FIELD)
public class Variable {

	private String name;
	private String description;
	
	private Collection<State> states;
	
	private int id;

	public Variable() {
	}
	
	public Variable(int id, int cardinality) {
		this.id = id;
		this.states = new ArrayList<>(cardinality);
		for (int i = 0; i < cardinality; ++i) {
			this.states.add(new State("State " + (i+1)));
		}
	}
	
	public Variable(int id, String name, String... states) {
		this.id = id;
		this.states = new ArrayList<>(states.length);
		
		for (String sname : states) {
			this.states.add(new State(sname));
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
