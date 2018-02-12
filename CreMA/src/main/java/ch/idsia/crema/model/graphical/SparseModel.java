package ch.idsia.crema.model.graphical;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.GraphicalModel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A graphical model that will not update indices when a variable is deleted.
 * This is the way to go!!!!
 *
 * @author david
 */
@XmlRootElement(name = "model")
@XmlAccessorType(XmlAccessType.FIELD)
public class SparseModel<F extends GenericFactor> extends GenericSparseModel<F, SparseList> implements GraphicalModel<F> {

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
}
