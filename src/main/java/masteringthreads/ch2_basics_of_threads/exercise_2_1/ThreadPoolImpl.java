package masteringthreads.ch2_basics_of_threads.exercise_2_1;

import java.util.*;
import java.util.concurrent.*;

/**
 * Our first exercise is to implement a task queue using wait/notify and monitor
 * locks (synchronized) within our ThreadPool implementation. Our task queue should
 * be a simple ArrayDeque.
 */
public class ThreadPoolImpl implements ThreadPool {
    /**
     * Constant Runnable that is added to the tasks to indicate that we want to
     * shut down the thread pool.
     */
    private static final Runnable POISON_PILL = () -> {
    };

    private final Deque<Runnable> tasks = new ArrayDeque<>();

    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1)
            throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        for (int i = 0; i < poolSize; i++) {
            new Worker("worker-" + i).start();
        }
    }

    private Runnable take() throws InterruptedException {
        synchronized (tasks) {
            while (tasks.isEmpty()) tasks.wait();
            var task = tasks.removeFirst();
            if (task == POISON_PILL) tasks.addLast(POISON_PILL);
            return task;
        }
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        synchronized (tasks) {
            if (tasks.peekLast() == POISON_PILL)
                throw new RejectedExecutionException("shut down");
            tasks.addLast(task);
            tasks.notifyAll();
        }
    }

    @Override
    public int getRunQueueLength() {
        synchronized (tasks) {
            return (int) tasks.stream()
                    .filter(task -> task != POISON_PILL)
                    .count();
        }
    }

    @Override
    public void shutdown() {
        synchronized (tasks) {
            if (tasks.peekLast() != POISON_PILL) {
                submit(POISON_PILL);
                // tasks.addLast(POISON_PILL);
                // tasks.notifyAll();
            }
        }
    }

    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        public void run() {
            while (true) {
                try {
                    var task = take();
                    if (task == POISON_PILL) {
                        System.out.println("FOund a poison pill");
                        break;
                    }
                    task.run();
                } catch (InterruptedException e) {
                    // ???
                }
            }
            System.out.println("Exiting thread " + getName());
        }
    }

    public static void main(String... args) throws InterruptedException {
        var pool = new ThreadPoolImpl(10);
        pool.submit(() -> System.out.println("Test1"));
        pool.submit(() -> System.out.println("Test2"));
        pool.submit(() -> System.out.println("Test3"));
        Thread.sleep(100);
        pool.shutdown();
    }
}
