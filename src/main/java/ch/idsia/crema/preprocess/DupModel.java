package ch.idsia.crema.preprocess;

import ch.idsia.crema.model.Model;

public class DupModel {

	@SuppressWarnings("unchecked")
	public <T extends Model<?>> T execute(T model) {
		return (T) model.copy();
	}
}
