package ch.idsia.crema.search;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ProgressEvent extends EventObject {
	
	private double progress;
	
	public ProgressEvent(Object source, double progress) {
		super(source);
		this.progress = progress;
	}
	
	public double getProgress() { 
		return progress;
	}
}
