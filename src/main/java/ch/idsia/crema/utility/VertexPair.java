package ch.idsia.crema.utility;

import java.util.Objects;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    12.02.2018 14:21
 * <p>
 * Based on https://www.programcreek.com/java-api-examples/index.php?source_dir=SerialFIM-master/org/jgrapht/util/VertexPair.java
 */
public class VertexPair<V> {

	private final V first;
	private final V second;

	public VertexPair(V first, V second) {
		this.first = first;
		this.second = second;
	}

	public V getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}

	public boolean hasVertex(V v) {
		return first.equals(v) || second.equals(v);
	}

	@Override
	public String toString() {
		return first + ", " + second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VertexPair<?> that = (VertexPair<?>) o;
		return Objects.equals(first, that.first) &&
				Objects.equals(second, that.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}
