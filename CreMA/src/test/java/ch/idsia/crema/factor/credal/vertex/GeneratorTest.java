package ch.idsia.crema.factor.credal.vertex;

import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;
import org.junit.Test;

public class GeneratorTest {

	@Test
	public void test3d() {
		CNGenerator generator = new CNGenerator();
		double[][] matrix = generator.generate(3, 100, 2);
		
	}
}
