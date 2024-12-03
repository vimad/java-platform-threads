package masteringthreads.ch4_applied_threading_techniques.exercise_4_1;

import masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_1.ThreadPoolEx;

import java.util.concurrent.*;

public interface ThreadPoolEx2 extends ThreadPoolEx {
    /**
     * Waits until all the threads in the thread pool have exited before returning.
     *
     * @return true if the worker threads shut down within the required time;
     * false otherwise.
     */
    boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException;
}