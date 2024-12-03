package masteringthreads.ch4_applied_threading_techniques.exercise_4_1;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

// TODO: Replace ArrayDeque with LinkedBlockingQueue and add a volatile boolean
//  "running" to state whether the pool has been shut down or not. The reason
//  that we need a separate field for the state is that we cannot lock the queue
//  for exclusive access. We will not need the ReentrantLock and Condition once
//  we have moved to the LinkedBlockingQueue.
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
    private final CountDownLatch workersLatch;

    // This stays the same
    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1)
            throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        workersLatch = new CountDownLatch(poolSize);
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
        if (task == POISON_PILL) tasks.add(POISON_PILL);
        return task;
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        if (!running.get()) throw new RejectedExecutionException("shut down");
        tasks.add(task);
        // if (!tasks.offer(task)) throw new RejectedExecutionException("capacity full");
        // tasks.put(task);
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
        List<Runnable> unstartedTasks = tasks.stream()
                .filter(IS_NOT_POISON_PILL)
                .toList();
        tasks.removeIf(IS_NOT_POISON_PILL);
        if (tasks.isEmpty()) submit(POISON_PILL);
        workers.forEach(Thread::interrupt);
        return unstartedTasks;
    }

    @Override
    public boolean awaitTermination(long time, TimeUnit unit) throws InterruptedException {
        return workersLatch.await(time, unit);
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
                        workersLatch.countDown();
                        return;
                    } else task.run();
                } catch (InterruptedException consumeAndIgnore) {
                }
            }
        }
    }
}

