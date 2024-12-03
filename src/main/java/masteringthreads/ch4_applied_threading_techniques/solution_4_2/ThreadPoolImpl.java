package masteringthreads.ch4_applied_threading_techniques.solution_4_2;

import masteringthreads.ch4_applied_threading_techniques.exercise_4_1.ThreadPoolEx2;

import java.util.*;
import java.util.concurrent.*;

public class ThreadPoolImpl implements ThreadPoolEx2 {
    private final ExecutorService service;

    public ThreadPoolImpl(int poolSize) {
        service = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public void submit(Runnable task) {
        service.submit(task);
    }

    @Override
    public int getRunQueueLength() {
        return ((ThreadPoolExecutor) service).getQueue().size();
    }

    @Override
    public void shutdown() {
        service.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return service.shutdownNow();
    }

    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return service.awaitTermination(time, unit);
    }
}