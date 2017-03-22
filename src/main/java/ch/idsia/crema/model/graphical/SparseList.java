package ch.idsia.crema.model.graphical;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.lang3.ArrayUtils;

import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

@XmlAccessorType(XmlAccessType.FIELD)
public class SparseList implements Graph {

	private TIntObjectMap<int[]> parents = new TIntObjectHashMap<int[]>();
	private TIntObjectMap<int[]> children = new TIntObjectHashMap<int[]>();

	@Override
	public SparseList copy() {
		SparseList result = new SparseList();
		result.children = new TIntObjectHashMap<int[]>(children);
		result.parents = new TIntObjectHashMap<int[]>(parents);
		return result;
	}

	/**
	 * In this graph representation we won't need to init anything when a
	 * variable is added to the model
	 */
	@Override
	public void addVariable(int variable, int size) {

	}

	/**
	 * Remove a variable from the Graph.
	 */
	@Override
	public void removeVariable(int variable) {

		if (parents.containsKey(variable)) {
			for (int parent : parents.get(variable)) {
				removeLink(parent, variable);
			}
			parents.remove(variable);
		}

		if (children.containsKey(variable)) {
			for (int child : children.get(variable)) {
				removeLink(variable, child);
			}
			children.remove(variable);
		}

	}

	/**
	 * @param parents
	 *            list of parents for the variable. Will be used directly.
	 */
	@Override
	public void addLink(int from, int to) {
		int[] new_children = ArraysUtil.addToSortedArray(this.children.get(from), to);
		this.children.put(from, new_children);

		int[] new_parents = ArraysUtil.addToSortedArray(this.parents.get(to), from);
		this.parents.put(to, new_parents);
	}

	@Override
	public void removeLink(int from, int to) {
		int[] new_children = ArraysUtil.removeFromSortedArray(this.children.get(from), to);
		if (new_children == null)
			this.children.remove(from);
		else
			this.children.put(from, new_children);

		int[] new_parents = ArraysUtil.removeFromSortedArray(this.parents.get(to), from);
		if (new_parents == null)
			this.parents.remove(to);
		else
			this.parents.put(to, new_parents);
	}

	@Override
	public int[] getParents(int variable) {
		return ArrayUtils.nullToEmpty(this.parents.get(variable));
	}

	@Override
	public int[] getChildren(int variable) {
		return ArrayUtils.nullToEmpty(this.children.get(variable));
	}
}
