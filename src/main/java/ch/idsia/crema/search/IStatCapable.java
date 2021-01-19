package ch.idsia.crema.search;

public interface IStatCapable<M> {

	void setStats(IStats<M> stats);

	IStats<M> getStats();

}
