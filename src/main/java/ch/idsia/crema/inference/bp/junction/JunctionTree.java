package ch.idsia.crema.inference.bp.junction;

import ch.idsia.crema.factor.Factor;
import ch.idsia.crema.inference.bp.cliques.Clique;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    13.11.2020 17:36
 */
public class JunctionTree<F extends Factor<F>> extends SimpleWeightedGraph<Clique, Separator<F>> {

	public JunctionTree() {
		super(Separator::new);
	}

	public boolean addEdge(Separator<F> s) {
		return super.addEdge(s.getSource(), s.getTarget(), s);
	}
}
