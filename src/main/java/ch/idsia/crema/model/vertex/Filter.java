package ch.idsia.crema.model.vertex;

public final class Filter implements Collector {
	private final int offset;
	
	public Filter(int stride, int state) {
		offset = stride * state;
	}
	
	@Override
	public double collect(final double[] data, final int source) {
		return data[source + offset];
	}
}