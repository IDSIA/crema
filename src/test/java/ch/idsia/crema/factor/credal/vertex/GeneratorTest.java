package ch.idsia.crema.factor.credal.vertex;

import org.junit.Test;

import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;

public class GeneratorTest {

	@Test
	public void test3d() {
		CNGenerator generator = new CNGenerator();
		double[][] matrix = generator.generate(3, 100, 2);
		
	}
}
