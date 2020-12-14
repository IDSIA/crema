package ch.idsia.crema.inference.bp.cliques;

import ch.idsia.crema.inference.bp.triangulation.TriangulatedGraph;

import java.util.HashSet;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 17:22
 */
public class CliqueSet extends HashSet<Clique> {

	private TriangulatedGraph model;

	public TriangulatedGraph getModel() {
		return model;
	}

	public void setModel(TriangulatedGraph network) {
		this.model = network;
	}
}
