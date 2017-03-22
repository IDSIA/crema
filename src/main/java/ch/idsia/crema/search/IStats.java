package ch.idsia.crema.search;

public interface IStats<M> {
	public void open(double score);
	public void improvement(long iter, double value);
	public void reset(long iteration);
	public void move(M move);
	public void close();
}
