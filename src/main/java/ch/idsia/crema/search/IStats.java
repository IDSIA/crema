package ch.idsia.crema.search;

public interface IStats<M> {

	void open(double score);

	void improvement(long iter, double value);

	void reset(long iteration);

	void move(M move);

	void close();

}
