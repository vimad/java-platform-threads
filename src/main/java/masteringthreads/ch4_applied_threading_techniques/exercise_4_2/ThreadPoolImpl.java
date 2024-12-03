package masteringthreads.ch4_applied_threading_techniques.exercise_4_2;

import masteringthreads.ch4_applied_threading_techniques.exercise_4_1.ThreadPoolEx2;

import java.util.*;
import java.util.concurrent.*;

// TODO: Replace inner workings of ThreadPool with ExecutorService
public class ThreadPoolImpl implements ThreadPoolEx2 {
    private final ThreadPoolExecutor threadPool;

    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1)
            throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        threadPool = new ThreadPoolExecutor(poolSize, poolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    @Override
    public void submit(Runnable task) {
        threadPool.submit(task);
    }

    @Override
    public int getRunQueueLength() {
        return threadPool.getQueue().size();
    }

    @Override
    public void shutdown() {
        threadPool.shutdown();
    }

    /**
     * Shuts down pool more abruptly, interrupting threads busy with tasks
     * and returning a list of unstarted Runnables.
     *
     * @return a List of Runnables that have not been started yet
     */
    @Override
    public List<Runnable> shutdownNow() {
        return threadPool.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return threadPool.awaitTermination(time, unit);
    }
}
