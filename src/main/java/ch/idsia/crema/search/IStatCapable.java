package ch.idsia.crema.search;

public interface IStatCapable <M> {
	public void setStats(IStats<M> stats);
	public IStats<M> getStats();
}
