package masteringthreads.ch4_applied_threading_techniques.solution_4_1;

import masteringthreads.ch4_applied_threading_techniques.exercise_4_1.ThreadPoolEx2;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

public class ThreadPoolImpl implements ThreadPoolEx2 {
    /**
     * Constant Runnable that is added to the tasks to indicate that we want to
     * shut down the thread pool.
     */
    private static final Runnable POISON_PILL = () -> {
    };
    private static final Predicate<Runnable> IS_NOT_POISON_PILL =
            task -> task != POISON_PILL;

    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    private final List<Worker> workers;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final CountDownLatch workerCount;

    // This stays the same
    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1)
            throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        workerCount = new CountDownLatch(poolSize);
        List<Worker> tempWorkers = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            var worker = new Worker("worker-" + i);
            worker.start();
            tempWorkers.add(worker);
        }
        workers = List.copyOf(tempWorkers);
    }

    private Runnable take() throws InterruptedException {
        Runnable task = tasks.take();
        if (task == POISON_PILL) tasks.add(task);
        return task;
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        if (!running.get())
            throw new RejectedExecutionException("Pool shut down");
        boolean success = tasks.offer(task);
        if (!success)
            throw new RejectedExecutionException("Pool temporarily overloaded");
        // or simply:
        // tasks.add(task);
    }

    @Override
    public int getRunQueueLength() {
        return (int) tasks.stream()
                .filter(IS_NOT_POISON_PILL)
                .count();
    }

    @Override
    public void shutdown() {
        if (running.compareAndSet(true, false)) {
            tasks.add(POISON_PILL);
        }
    }

    /**
     * Shuts down pool more abruptly, interrupting threads busy with tasks
     * and returning a list of unstarted Runnables.
     *
     * @return a List of Runnables that have not been started yet
     */
    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        List<Runnable> unstarted = new ArrayList<>();
        tasks.drainTo(unstarted);
        tasks.add(POISON_PILL); // might have multiple poison pills on tasks queue
        unstarted.removeIf(IS_NOT_POISON_PILL.negate());
        workers.forEach(Thread::interrupt);
        return List.copyOf(unstarted);
    }

    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return workerCount.await(time, unit);
    }

    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        public void run() {
            while (true) {
                try {
                    Runnable task = take();
                    if (task == POISON_PILL) {
                        workerCount.countDown();
                        return;
                    } else task.run();
                } catch (InterruptedException consumeAndIgnore) {
                }
            }
        }
    }
}
