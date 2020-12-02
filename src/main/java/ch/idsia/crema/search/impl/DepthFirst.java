package ch.idsia.crema.search.impl;

import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.search.SearchOperation;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class DepthFirst {

	private final GraphicalModel<?> model;
	private SearchOperation controller;

	private final TIntSet open;

	public DepthFirst(GraphicalModel<?> model) {
		this.open = new TIntHashSet();
		this.model = model;
	}

	public void visit(int... nodes) {
		visitAll(nodes, -1);
	}

	private void visitAll(int[] nodes, int from) {
		for (int node : nodes) visit(node, from);
	}

	private void visit(int node, int from) {
		if (open.contains(node) || !controller.canVisit(node, from)) return;

		controller.opening(node, from);
		open.add(node);

		if (controller.visitChildren(node)) visitAll(model.getChildren(node), node);
		if (controller.visitParents(node)) visitAll(model.getParents(node), node);

		controller.closing(node, from);
	}

	public void setController(SearchOperation controller) {
		this.controller = controller;
	}

}
