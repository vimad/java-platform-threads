package masteringthreads.ch2_basics_of_threads.solution_2_2;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Our second solution implemented with Lock and Condition.
 */
public class ThreadPoolImpl implements ThreadPool {
    /**
     * Constant Runnable that is added to the tasks to indicate that we want to
     * shut down the thread pool.
     */
    private static final Runnable POISON_PILL = () -> {
    };

    private final Lock tasksLock = new ReentrantLock();
    private final Condition tasksNotEmpty = tasksLock.newCondition();
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

    private Runnable take() throws InterruptedException {
        tasksLock.lock();
        try {
            while (tasks.isEmpty()) tasksNotEmpty.await();
            Runnable task = tasks.remove();
            if (task == POISON_PILL) tasks.add(POISON_PILL);
            return task;
        } finally {
            tasksLock.unlock();
        }
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        tasksLock.lock();
        try {
            if (tasks.peekLast() == POISON_PILL)
                throw new RejectedExecutionException("Pool shut down");
            tasks.add(task);
            if (task == POISON_PILL) tasksNotEmpty.signalAll();
            else tasksNotEmpty.signal();
        } finally {
            tasksLock.unlock();
        }
    }

    @Override
    public int getRunQueueLength() {
        // return the length of the ArrayDeque, excluding the POISON_PILL
        // remember to also synchronize!
        tasksLock.lock();
        try {
            return (int) tasks.stream()
                    .filter(task -> task != POISON_PILL)
                    .count();
        } finally {
            tasksLock.unlock();
        }
    }

    @Override
    public void shutdown() {
        // If the last task in the ArrayDeque is not already the POISON_PILL,
        // we want to submit it
        tasksLock.lock();
        try {
            if (tasks.peekLast() != POISON_PILL) submit(POISON_PILL);
        } finally {
            tasksLock.unlock();
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
