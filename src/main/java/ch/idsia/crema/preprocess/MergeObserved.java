package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.FilterableFactor;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.preprocess.mergers.MergeFactors;
import ch.idsia.crema.utility.ArraysUtil;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    14.04.2021 09:31
 */
public class MergeObserved<F extends FilterableFactor<F>> implements ConverterEvidence<F, F, GraphicalModel<F>, GraphicalModel<F>> {

	MergeFactors<F> mergeFactors;

	public MergeObserved(MergeFactors<F> mergeFactors) {
		this.mergeFactors = mergeFactors;
	}

	@Override
	public GraphicalModel<F> execute(GraphicalModel<F> original, TIntIntMap evidence) {
		final GraphicalModel<F> model = original.copy();

		// consider the observed nodes (that are now leaf and binary) which have only one parent.
		final TIntObjectMap<TIntLinkedList> valid = new TIntObjectHashMap<>();
		final TIntHashSet evToRemove = new TIntHashSet();

		for (int key : evidence.keys()) {
			if (!ArraysUtil.contains(key, model.getVariables())) {
				evToRemove.add(key);
				continue;
			}

			final int[] parents = model.getParents(key);

			if (parents.length > 1)
				continue;

			final int parent = parents[0];

			if (!valid.containsKey(parent))
				valid.put(parent, new TIntLinkedList());

			valid.get(parent).add(key);
		}

		for (int v : evToRemove.toArray())
			evidence.remove(v);

		// if between these nodes there are two that have the same parent, we replace them with a unique binary node
		for (int y : valid.keys()) {
			final TIntLinkedList children = valid.get(y);
			if (children.size() < 2)
				continue;

			while (children.size() > 1) {
				int x1 = children.get(0);
				int x2 = children.get(1);

				// add new variable, merge of the observed, with binary state: observed or not
				final int x = model.addVariable(2);
				model.addParent(x, y);

				final F f = mergeFactors.merge(model, evidence, x1, x2, x, y);

				model.setFactor(x, f);

				// remove observed variables
				model.removeVariable(x1);
				model.removeVariable(x2);

				// update evidence map
				evidence.remove(x1);
				evidence.remove(x2);
				evidence.put(x, 0);

				// update slack array
				children.remove(x1);
				children.remove(x2);
				children.add(x);
			}
		}

		return model;
	}

}
