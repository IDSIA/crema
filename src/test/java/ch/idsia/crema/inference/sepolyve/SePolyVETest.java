package ch.idsia.crema.inference.sepolyve;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.convert.VertexToInterval;
import ch.idsia.crema.factor.credal.linear.interval.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactor;
import ch.idsia.crema.factor.credal.vertex.separate.VertexFactorFactory;
import ch.idsia.crema.model.graphical.DAGModel;
import ch.idsia.crema.preprocess.RemoveBarren;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SePolyVETest {

	@Test
	public void testRun() {
		DAGModel<VertexFactor> model = new DAGModel<>();

		int first = model.addVariable(3);
		VertexFactor f1 = VertexFactorFactory.factory().domain(model.getDomain(first), Strides.empty())
				.addVertex(new double[]{0.1, 0.1, 0.8})
				.addVertex(new double[]{0.1, 0.3, 0.6})
				.addVertex(new double[]{0.3, 0.2, 0.5})
				.get();

		model.setFactor(first, f1);

		int second = model.addVariable(3);
		int third = model.addVariable(3);

		VertexFactor f2 = VertexFactorFactory.factory().domain(model.getDomain(second), model.getDomain(first, third))
				.addVertex(new double[]{0.2, 0.1, 0.7}, 0, 0)
				.addVertex(new double[]{0.2, 0.5, 0.3}, 0, 0)
				.addVertex(new double[]{0.4, 0.2, 0.4}, 0, 0)

				.addVertex(new double[]{0.1, 0.1, 0.8}, 0, 1)
				.addVertex(new double[]{0.2, 0.1, 0.7}, 0, 1)
				.addVertex(new double[]{0.1, 0.2, 0.7}, 0, 1)

				.addVertex(new double[]{0.5, 0.1, 0.4}, 0, 2)
				.addVertex(new double[]{0.5, 0.2, 0.3}, 0, 2)
				.addVertex(new double[]{0.2, 0.3, 0.5}, 0, 2)

				.addVertex(new double[]{0.7, 0.1, 0.2}, 1, 0)
				.addVertex(new double[]{0.3, 0.4, 0.3}, 1, 0)
				.addVertex(new double[]{0.6, 0.2, 0.2}, 1, 0)

				.addVertex(new double[]{0.3, 0.3, 0.4}, 1, 1)
				.addVertex(new double[]{0.2, 0.7, 0.1}, 1, 1)
				.addVertex(new double[]{0.1, 0.8, 0.1}, 1, 1)

				.addVertex(new double[]{0.3, 0.5, 0.2}, 1, 2)
				.addVertex(new double[]{0.2, 0.5, 0.3}, 1, 2)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 1, 2)

				.addVertex(new double[]{0.3, 0.1, 0.6}, 2, 0)
				.addVertex(new double[]{0.2, 0.4, 0.4}, 2, 0)
				.addVertex(new double[]{0.1, 0.7, 0.2}, 2, 0)

				.addVertex(new double[]{0.4, 0.2, 0.4}, 2, 1)
				.addVertex(new double[]{0.4, 0.4, 0.2}, 2, 1)
				.addVertex(new double[]{0.1, 0.2, 0.7}, 2, 1)

				.addVertex(new double[]{0.2, 0.1, 0.7}, 2, 2)
				.addVertex(new double[]{0.1, 0.5, 0.3}, 2, 2)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 2, 2)

				.get();
		model.setFactor(second, f2);

		int fourth = model.addVariable(3);
		VertexFactor f4 = VertexFactorFactory.factory().domain(model.getDomain(fourth), model.getDomain(second))
				.addVertex(new double[]{0.4, 0.4, 0.2}, 0)
				.addVertex(new double[]{0.5, 0.3, 0.2}, 0)
				.addVertex(new double[]{0.5, 0.1, 0.4}, 0)

				.addVertex(new double[]{0.4, 0.4, 0.2}, 1)
				.addVertex(new double[]{0.5, 0.3, 0.2}, 1)
				.addVertex(new double[]{0.5, 0.1, 0.4}, 1)

				.addVertex(new double[]{0.4, 0.2, 0.4}, 2)
				.addVertex(new double[]{0.3, 0.4, 0.3}, 2)
				.addVertex(new double[]{0.5, 0.2, 0.3}, 2)

				.get();
		model.setFactor(fourth, f4);

		int fifth = model.addVariable(3);
		VertexFactor f5 = VertexFactorFactory.factory().domain(model.getDomain(fifth), Strides.empty())
				.addVertex(new double[]{0.1, 0.1, 0.8})
				.addVertex(new double[]{0.1, 0.3, 0.6})
				.addVertex(new double[]{0.3, 0.2, 0.5})
				.get();
		model.setFactor(fifth, f5);

		VertexFactor f3 = VertexFactorFactory.factory().domain(model.getDomain(third), model.getDomain(fifth))
				.addVertex(new double[]{0.1, 0.2, 0.7}, 0)
				.addVertex(new double[]{0.1, 0.5, 0.4}, 0)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 0)

				.addVertex(new double[]{0.2, 0.3, 0.5}, 1)
				.addVertex(new double[]{0.3, 0.4, 0.3}, 1)
				.addVertex(new double[]{0.4, 0.3, 0.3}, 1)

				.addVertex(new double[]{0.3, 0.2, 0.5}, 2)
				.addVertex(new double[]{0.2, 0.4, 0.4}, 2)
				.addVertex(new double[]{0.6, 0.3, 0.1}, 2)

				.get();
		model.setFactor(third, f3);

		int sixth = model.addVariable(3);
		VertexFactor f6 = VertexFactorFactory.factory().domain(model.getDomain(sixth), model.getDomain(third))
				.addVertex(new double[]{0.1, 0.2, 0.7}, 0)
				.addVertex(new double[]{0.1, 0.5, 0.4}, 0)
				.addVertex(new double[]{0.3, 0.3, 0.4}, 0)

				.addVertex(new double[]{0.2, 0.3, 0.5}, 1)
				.addVertex(new double[]{0.3, 0.4, 0.3}, 1)
				.addVertex(new double[]{0.4, 0.3, 0.3}, 1)

				.addVertex(new double[]{0.3, 0.2, 0.5}, 2)
				.addVertex(new double[]{0.2, 0.4, 0.4}, 2)
				.addVertex(new double[]{0.6, 0.3, 0.1}, 2)

				.get();
		model.setFactor(sixth, f6);

		final Int2IntMap evidence = new Int2IntOpenHashMap();
		evidence.put(fourth, 1);

		SePolyVE polu = new SePolyVE(0.0001);

		VertexFactor vd = polu.query(model, evidence, sixth);

		System.out.println(vd.getDomain());
		System.out.println(vd.getVertices().length);

		Assertions.assertEquals(11, vd.getVertices().length);

		VertexToInterval converter = new VertexToInterval();
		DAGModel<IntervalFactor> model2 = model.convert(converter);

		final RemoveBarren<IntervalFactor> rb = new RemoveBarren<>();

		rb.executeInPlace(model2, evidence, sixth);

		// TODO: and then?
	}

}
