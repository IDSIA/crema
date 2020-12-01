package ch.idsia.crema.inference;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 14:12
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Pipe<F, R> implements Algorithm<F, R> {

	protected List<Algorithm> stages = new ArrayList<>();
	private Object input;
	private Object output;

	public Pipe() {
	}

	public Pipe(List<Algorithm> stages) {
		this.stages = stages;
	}

	public List<Algorithm> getStages() {
		return stages;
	}

	public void setStages(List<Algorithm> stages) {
		this.stages = stages;
	}

	@Override
	public void setInput(F model) {
		this.input = model;
	}

	@Override
	public R getOutput() {
		return (R) output;
	}

	@Override
	public R exec() {
		if (input == null) throw new IllegalArgumentException("No input available");
		if (stages.isEmpty()) throw new IllegalArgumentException("No stages available");

		output = input;

		for (Algorithm stage : stages) {
			stage.setInput(output);
			output = stage.exec();
		}

		return (R) output;
	}
}
