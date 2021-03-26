package ch.idsia.crema.inference.ve.order;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.model.io.dot.DotSerialize;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class MinFillOrderingTest {

	@Test
	public void testFindOrder() {
		DAGModel<GenericFactor> model = new DAGModel<>();
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);

		model.addParent(2, 0); // A3 <-- A1
		model.addParent(2, 1); // A3 <-- A2
		model.addParent(3, 1);
		model.addParent(5, 2);
		model.addParent(6, 3);
		model.addParent(7, 4);
		model.addParent(7, 5);
		model.addParent(7, 6);


		MinFillOrdering ordering = new MinFillOrdering();
		int[] order = ordering.apply(model);
		System.out.println(Arrays.toString(order));
		DotSerialize ds = new DotSerialize();
		ds.run(model);
	}

	@Test
	public void testFindOrder2() {
		DAGModel<GenericFactor> model = new DAGModel<>();
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);

		model.addParent(1, 0);
		model.addParent(2, 0);
		model.addParent(3, 1);
		model.addParent(4, 1);
		model.addParent(4, 2);
		model.addParent(5, 2);

		MinFillOrdering ordering = new MinFillOrdering();
		ordering.apply(model);
	}

}
