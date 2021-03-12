package ch.idsia.crema.utility;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema
 * Date:    01.12.2020 13:17
 */
public class InvokerWithTimeoutTest {

	private static double[] getFromServer() throws InterruptedException {
		TimeUnit.SECONDS.sleep(5);
		return new double[]{3, 4.3};
	}

	@Test
	public void testInvocationWithTimeout() throws InterruptedException, ExecutionException {
		InvokerWithTimeout<double[]> invoker = new InvokerWithTimeout<>();

		System.out.println("callable task");
		try {
			double[] res = invoker.run(InvokerWithTimeoutTest::getFromServer, 30);
			System.out.println(Arrays.toString(res));

		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

}