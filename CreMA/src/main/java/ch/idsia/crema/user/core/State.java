package ch.idsia.crema.user.core;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="state")
public class State {
	private String name;
	private String description;
	
	public State() {
	}
	
	public State(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
