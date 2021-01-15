package ch.idsia.crema.utility;

import java.util.concurrent.*;

public class InvokerWithTimeout<R> {

	public R run(Callable<R> task, long seconds) throws TimeoutException, InterruptedException, ExecutionException {

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		// Do the call in a separate thread, get a Future back
		Future<R> future = executorService.submit(task);
		R result = null;
		try {
			// System.out.println("set timeout "+seconds+" s.");
			result = future.get(seconds, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			executorService.shutdownNow();
			throw new TimeoutException();
		} catch (Exception e) {
			// e.printStackTrace();
			executorService.shutdownNow();
			throw e;
		}
		executorService.shutdownNow();
		return result;
	}

}
