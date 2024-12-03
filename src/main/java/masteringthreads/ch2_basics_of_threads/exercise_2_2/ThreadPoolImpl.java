package masteringthreads.ch2_basics_of_threads.exercise_2_2;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.function.*;

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

    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    private final Deque<Runnable> tasks = new ArrayDeque<>();

    // This stays the same
    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1)
            throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        for (int i = 0; i < poolSize; i++) {
            var worker = new Worker("worker-" + i);
            worker.start();
        }
    }

    private Runnable take() {
        return callLocked(() -> {
            while (tasks.isEmpty()) notEmpty.awaitUninterruptibly();
            Runnable task = tasks.remove();
            if (task == POISON_PILL) {
                tasks.add(POISON_PILL);
                notEmpty.signal();
            }
            return task;
        });
    }

    private <E> E callLocked(Supplier<E> task) {
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

    private void callLocked(Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        callLocked(() -> {
            if (tasks.peekLast() == POISON_PILL)
                throw new RejectedExecutionException("Pool shut down");
            tasks.add(task);
            notEmpty.signal();
        });
    }

    @Override
    public int getRunQueueLength() {
        return callLocked(() -> (int) tasks.stream()
                    .filter(task -> task != POISON_PILL)
                    .count());
    }

    @Override
    public void shutdown() {
        callLocked(() -> {
            if (tasks.peekLast() != POISON_PILL) submit(POISON_PILL);
        });
    }

    // This stays the same
    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        public void run() {
            while (true) {
                Runnable task = take();
                if (task == POISON_PILL) return;
                else task.run();
            }
        }
    }
}
