package ch.idsia.crema.model.io;

import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.GraphicalModel;
import ch.idsia.crema.model.graphical.SparseModel;
import ch.idsia.crema.model.io.bif.XMLBIFParser;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;

public class TestBIFXML {

	@Test
	public void testLoad() {
		XMLBIFParser parser = new XMLBIFParser();
		SparseModel<IntervalFactor> approxModel = new SparseModel<>();
		try(FileInputStream fio = new FileInputStream("/Users/sandro/Dropbox/Software/CreMA/src/test/resources/xmlbif/cn5_1Lower.xml")) {
			GraphicalModel<BayesianFactor> model = parser.parse(fio);
			
			System.out.println(Arrays.toString(model.getParents(0)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
