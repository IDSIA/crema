package ch.idsia.crema.model.io;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.Strides;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.user.credal.VertexSet;

public class XMLTest {
	@Test
	public void test() throws JAXBException {
/*	todo: fix this test

JAXBContext context = JAXBContext.newInstance(VertexSet.class);
		Marshaller marshall = context.createMarshaller();
marshall.setProperty(Marshaller.JAXB_ENCODING, "application/json");
		SparseModel<VertexFactor> model = new SparseModel<>();

		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);
		model.addVariable(2);

		model.addParent(2, 0); // A3 <-- A1
		model.addParent(2, 1); // A3 <-- A2
		model.addParent(3, 1);
		model.addParent(0, 1);

		Strides v = model.getDomain(3);
		Strides c = model.getDomain(1);

//		VertexSet vs = new VertexSet(new double[][][] { { { 1, 2 }, { 2, 1 } }, { { 2, 4 }, { 4, 5 } }, { { 1, 2 }, { 2, 1 } },
//				{ { 2, 4 }, { 4, 5 } } });

//		marshall.marshal(vs, System.out);


 */
	}
	
	
	
}
