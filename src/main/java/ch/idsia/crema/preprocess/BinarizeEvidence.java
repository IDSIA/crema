package ch.idsia.crema.preprocess;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.graphical.GraphicalModel;
import ch.idsia.crema.model.graphical.MixedModel;
import ch.idsia.crema.preprocess.creators.CreateFactor;
import ch.idsia.crema.preprocess.creators.CreateFactorBayesian;
import ch.idsia.crema.preprocess.creators.Instance;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

import java.util.ArrayList;

/**
 * Multiple evidences can be reduced to a single binary variable by adding
 * deterministic nodes as children of the observed nodes.
 *
 * <p>
 * This is usefull for algorithms that do not filter the observed evidences out
 * of the factors until the end.
 * </p>
 * <p>
 * Currently we are using {@link BayesianFactor} only.
 *
 * @author david
 */
public class BinarizeEvidence<F extends GenericFactor> implements ConverterEvidence<F, GenericFactor, GraphicalModel<F>, MixedModel> {

	private int evidenceNode;
	private int size = 2;

	private CreateFactor creator;

	public BinarizeEvidence() {
		setCreator(new CreateFactorBayesian());
	}

	public BinarizeEvidence(CreateFactor creator) {
		setCreator(creator);
	}

	public BinarizeEvidence(int size) {
		this();
		setSize(size);
	}

	public BinarizeEvidence(CreateFactor creator, int size) {
		setCreator(creator);
		setSize(size);
	}

	/**
	 * @param size the size of the binarized evidence
	 */
	// TODO: what is this? It is just the size of evidence?
	public void setSize(int size) {
		this.size = size;
	}

	public void setCreator(CreateFactor creator) {
		this.creator = creator;
	}

	public int getEvidenceNode() {
		return evidenceNode;
	}

	/**
	 * Execute the binarization and return a new Factor
	 *
	 * @param original the model to be processed
	 * @param evidence the observed variable as a map of variable-states
	 * @return a mixed model where a new node of type {@link BayesianFactor} is added to collect all the evidence
	 */
	@Override
	public MixedModel execute(GraphicalModel<F> original, Int2IntMap evidence) {
		MixedModel model = new MixedModel((DAGModel<GenericFactor>) original.copy());

		// TODO: do we need to sort the keys????

		int new_var = -1;

		ArrayList<Instance> parents = new ArrayList<>();
		for (int var : evidence.keySet()) {
			int var_size = model.getSize(var);
			int var_obs = evidence.get(var);

			parents.add(new Instance(var, var_size, var_obs));
			if (parents.size() == size) {
				new_var = creator.create(model, parents);
				parents.clear();
				parents.add(new Instance(new_var, 2, 1));
			}
		}

		// when no dummy has been created we must create it
		// when we did create dummies, the parents array will contain the last
		// created dummy and all vars not yet dummied
		if (new_var == -1 || parents.size() > 1) {
			new_var = creator.create(model, parents);
		}
		evidenceNode = new_var;

		return model;
	}

}