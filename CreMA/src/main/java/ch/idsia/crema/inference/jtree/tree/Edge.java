package ch.idsia.crema.inference.jtree.tree;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: CreMA
 * Date:    06.02.2018 14:30
 */
public class Edge {

	Node start;
	Node end;

	public Edge(Node start, Node end) {
		this.start = start;
		this.end = end;
	}
}
