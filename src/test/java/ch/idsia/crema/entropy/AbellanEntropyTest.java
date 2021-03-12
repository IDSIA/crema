package ch.idsia.crema.entropy;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    18.12.2020 10:36
 */
public class AbellanEntropyTest {

	@Test
	public void testSimpleEntropy1() {
		double[] l = new double[]{0.3, 0.4, 0.0, 0.0, 0.0};
		double[] u = new double[]{1.0, 1.0, 0.04, 1.0, 1.0};

		new AbellanEntropy().getMaxEntropy(l, u);
	}

	@Test
	public void testSimpleEntropy2() {
		double[] l = new double[]{0.08771929824561402, 0.21052631578947367, 0.3508771929824561, 0.3508771929824561};
		double[] u = new double[]{0.08771929824561402, 0.21052631578947367, 0.3508771929824561, 0.3508771929824561};

		new AbellanEntropy().getMaxEntropy(l, u);
	}

	@Disabled // TODO: this test causes an IndexBoundException in AbellanEntropy:71
	@Test
	public void testSimpleEntropy3() {
		double[] l = new double[]{0.3164556962025316, 0.3164556962025316, 0.05063291139240505, 0.3164556962025316};
		double[] u = new double[]{0.31645569620253167, 0.31645569620253167, 0.05063291139240508, 0.31645569620253167};

		new AbellanEntropy().getMaxEntropy(l, u);
	}

}