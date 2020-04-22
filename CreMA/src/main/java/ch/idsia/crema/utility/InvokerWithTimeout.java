package ch.idsia.crema.utility;

import java.util.Arrays;
import java.util.concurrent.*;

public class  InvokerWithTimeout<R extends Object>  {
    public static void main(String[] args) throws InterruptedException {

        InvokerWithTimeout<double[]> invoker = new InvokerWithTimeout<>();

        System.out.println("callable task");
        try {
            double[] res = invoker.run(InvokerWithTimeout::getFromServer, 30);
            System.out.println(Arrays.toString(res));

        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }


    public R run(Callable<R> task, long seconds) throws TimeoutException, InterruptedException {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Do the call in a separate thread, get a Future back
        Future<R> future = executorService.submit(task);
        try {
            System.out.println("set timeout "+seconds+" s.");
            return future.get(seconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            executorService.shutdownNow();
            throw new TimeoutException();
        } catch (Exception e) {
            executorService.shutdownNow();
            throw new InterruptedException();
        }

    }

    private static double[] getFromServer() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        return new double[]{3, 4.3};
    }
}
