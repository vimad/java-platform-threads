package masteringthreads.ch2_basics_of_threads.exercise_2_2;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private final Deque<Runnable> tasks = new ArrayDeque<>();

    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    // This stays the same
    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1) throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        for (int i = 0; i < poolSize; i++) {
            var worker = new Worker("worker-" + i);
            worker.start();
        }
    }

    private Runnable take() throws InterruptedException {

        lock.lock();
        try {
            while (tasks.isEmpty()) notEmpty.awaitUninterruptibly();
            Runnable task = tasks.remove();
            if (task == POISON_PILL) {
                tasks.add(POISON_PILL);
                notEmpty.signal();
            }
            return task;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        lock.lock();
        try {
            if (tasks.peekLast() == POISON_PILL)
                throw new RejectedExecutionException("Pool shut down");
            tasks.add(task);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getRunQueueLength() {
        lock.lock();
        try {
            return (int) tasks.stream()
                    .filter(task -> task != POISON_PILL)
                    .count();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            if (tasks.peekLast() != POISON_PILL) submit(POISON_PILL);
        } finally {
            lock.unlock();
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
