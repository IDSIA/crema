package ch.idsia.crema.search;

public interface SearchOperation {

	default void opening(int node, int from) {
	}

	default void closing(int node, int from) {
	}

	default boolean canVisit(int node, int from) {
		return true;
	}

	default boolean visitChildren(int node) {
		return true;
	}

	default boolean visitParents(int node) {
		return true;
	}

}
