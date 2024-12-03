package masteringthreads.ch2_basics_of_threads.exercise_2_2;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;

/**
 * Our second exercise is to refactor this code to use ReentrantLock and Condition.
 * In our submit() method, we can send a single signal(), except when we submit our
 * POISON_PILL, in which case we need signalAll().
 */
public class ThreadPoolImpl implements ThreadPool {
    /**
     * Constant Runnable that is added to the tasks to indicate that we want to
     * shut down the thread pool.
     */
    private static final Runnable POISON_PILL = () -> {
    };

    // TODO: Instead of monitor locks (synchronized), use owned locks (ReentrantLock)
    //  for thread safety and Condition await()/signal() for inter-thread communication.
    private final Deque<Runnable> tasks = new ArrayDeque<>();

    {
        if (true)
            throw new UnsupportedOperationException("TODO: Implement with owned locks");
    }

    // This stays the same
    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1) throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        for (int i = 0; i < poolSize; i++) {
            var worker = new Worker("worker-" + i);
            worker.start();
        }
    }

    private Runnable take() throws InterruptedException {
        // TODO: Replace locking with an owned lock. Remember to use
        //  try/finally to unlock.
        synchronized (tasks) {
            while (tasks.isEmpty()) tasks.wait();
            Runnable task = tasks.remove();
            if (task == POISON_PILL) tasks.add(POISON_PILL);
            return task;
        }
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        // TODO: Replace locking with an owned lock. We can use signal(),
        //  as opposed to signalAll(), for all except our POISON_PILL tasks.
        //  Reason is that we want all our Worker threads to wake up when the
        //  POISON_PILL is added.
        synchronized (tasks) {
            if (tasks.peekLast() == POISON_PILL)
                throw new RejectedExecutionException("Pool shut down");
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    @Override
    public int getRunQueueLength() {
        // TODO: Replace locking with an owned lock.
        synchronized (tasks) {
            return (int) tasks.stream()
                    .filter(task -> task != POISON_PILL)
                    .count();
        }
    }

    @Override
    public void shutdown() {
        // TODO: Replace locking with an owned lock.
        synchronized (tasks) {
            if (tasks.peekLast() != POISON_PILL) submit(POISON_PILL);
        }
    }

    // This stays the same
    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        public void run() {
            while (true) {
                try {
                    Runnable task = take();
                    if (task == POISON_PILL) return;
                    else task.run();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
