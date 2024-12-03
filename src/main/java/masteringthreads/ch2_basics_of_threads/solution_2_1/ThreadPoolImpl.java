package masteringthreads.ch2_basics_of_threads.solution_2_1;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;

/**
 * Our first solution implemented with monitors and wait/notify.
 */
public class ThreadPoolImpl implements ThreadPool {
    /**
     * Constant Runnable that is added to the tasks to indicate that we want to
     * shut down the thread pool.
     */
    private static final Runnable POISON_PILL = () -> {
    };

    // Created a ArrayDeque field containing Runnable. This is our "tasks" queue.
    // Hint: Since ArrayDeque is not thread-safe, we need to synchronize it. In
    // this exercise, we will use monitor locks, i.e. synchronized. Use the
    // ArrayDeque itself as a monitor lock, e.g. synchronized(tasks) {...}
    private final Deque<Runnable> tasks = new ArrayDeque<>();

    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1) throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        // Create as many Worker instances as poolSize and start them.
        // Hint: Worker is an inner class defined at the bottom of this class
        for (int i = 0; i < poolSize; i++) {
            var worker = new Worker("worker-" + i);
            worker.start();
        }
    }

    private Runnable take() throws InterruptedException {
        synchronized (tasks) {
            // if the ArrayDeque is empty, we wait
            while (tasks.isEmpty()) tasks.wait();
            // remove the first task from the ArrayDeque
            //   if it is the POISON_PILL, add it back into ArrayDeque
            // return the task
            Runnable task = tasks.removeFirst();
            if (task == POISON_PILL) tasks.addLast(POISON_PILL);
            return task;
        }
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        // If the last task in the ArrayDeque is the POISON_PILL, throw a
        //  RejectedExecutionException, otherwise add the task and notifyAll()
        synchronized (tasks) {
            if (tasks.peekLast() == POISON_PILL)
                throw new RejectedExecutionException("Pool shut down");
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    @Override
    public int getRunQueueLength() {
        // return the length of the ArrayDeque, excluding the POISON_PILL
        // remember to also synchronize!
        synchronized (tasks) {
            return (int) tasks.stream()
                    .filter(task -> task != POISON_PILL)
                    .count();
        }
    }

    @Override
    public void shutdown() {
        // If the last task in the ArrayDeque is not already the POISON_PILL,
        // we want to submit it
        synchronized (tasks) {
            if (tasks.peekLast() != POISON_PILL) submit(POISON_PILL);
        }
    }

    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        public void run() {
            // we run in an infinite loop:
            while (true) {
                // remove the next task from the ArrayDeque using take()
                // if it is our POISON_PILL, we return; otherwise we call run()
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
