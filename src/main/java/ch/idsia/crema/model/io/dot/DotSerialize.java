package ch.idsia.crema.model.io.dot;

import ch.idsia.crema.model.GraphicalModel;

public class DotSerialize {
	public String run(GraphicalModel<?> gm) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("digraph model {\n");
		for (int i : gm.getVariables()) {
			builder.append("   node" + i + " [label=\"" + i + " (" + gm.getSize(i) +")\"];\n");
		}
		
		for (int i : gm.getVariables()) {
			for (int child : gm.getChildren(i)) {
				builder.append("   node").append(i).append(" -> node").append(child).append(";\n");
			}
		}
		builder.append("}");
		return builder.toString();
	}
}
