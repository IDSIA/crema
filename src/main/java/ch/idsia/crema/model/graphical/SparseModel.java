package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.change.NullChange;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * A graphical model that will not update indices when a variable is deleted.
 * This is the way to go!!!!
 *
 * @author david
 */
@XmlRootElement(name = "model")
@XmlAccessorType(XmlAccessType.FIELD)
public class SparseModel<F extends GenericFactor> extends GenericGraphicalModel<F, SparseList> implements GraphicalModel<F> {

	/**
	 * Create the directed model using the specified network implementation.
	 *
	 * @param method {@link Graph} implementation to use
	 */
	public SparseModel(SparseList method) {
		super(method);
	}

	/**
	 * Creates the directed model using a {@link SparseList} as implementation of the network.
	 */
	public SparseModel() {
		super(new SparseList());
	}

	public <R extends GenericFactor> SparseModel<R> convert(BiFunction<F, Integer, R> converter) {
		NullChange<R> changer = new NullChange<>();

		SparseList graph = network.copy();
		SparseModel<R> new_model = new SparseModel<>(graph);
		new_model.domainChanger = changer;
		new_model.cardinalityChanger = changer;

		new_model.cardinalities = new TIntIntHashMap(this.cardinalities);
		new_model.factors = new TIntObjectHashMap<>(factors.size());
		new_model.max = this.max;

		TIntObjectIterator<F> iterator = this.factors.iterator();
		while (iterator.hasNext()) {
			iterator.advance();
			R new_factor = converter.apply(iterator.value(), iterator.key());
			new_model.factors.put(iterator.key(), new_factor);
		}

		return new_model;
	}



	@Override
	public SparseModel observe(int var, int state){
		return (SparseModel)super.observe(var, state);
	}


	public BayesianNetwork sampleVertex(){
		BayesianNetwork bnet = new BayesianNetwork();
		bnet.addVariables(this.getVariables());
		for(int v: this.getVariables()){
			bnet.addParents(v,this.getParents(v));
			bnet.setFactor(v,((VertexFactor)this.getFactor(v)).sampleVertex());
		}
		return bnet;
	}

	public BayesianNetwork[] sampleVertex(int num){
		return IntStream.range(0,num).mapToObj(i->this.sampleVertex()).toArray(BayesianNetwork[]::new);
	}




}
