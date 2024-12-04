package masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_1;

import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPoolImpl implements ThreadPoolEx {
    /**
     * Constant Runnable that is added to the tasks to indicate that we want to
     * shut down the thread pool.
     */
    private static final Runnable POISON_PILL = () -> {
    };

    private final Lock tasksLock = new ReentrantLock();
    private final Condition tasksNotEmpty = tasksLock.newCondition();
    private final Deque<Runnable> tasks = new ArrayDeque<>();

    private final List<Worker> workers;

    // This stays the same
    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1) throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        List<Worker> tempWorkers = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            var worker = new Worker("worker-" + i);
            worker.start();
            tempWorkers.add(worker);
        }
        workers = List.copyOf(tempWorkers);
    }

    // This stays the same
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

    // This stays the same
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

    // This stays the same
    @Override
    public int getRunQueueLength() {
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
        tasksLock.lock();
        try {
            if (tasks.peekLast() != POISON_PILL) submit(POISON_PILL);
        } finally {
            tasksLock.unlock();
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
        tasksLock.lock();
        try {
            List<Runnable> unstartedTasks = tasks.stream()
                .filter(task -> task != POISON_PILL)
                .toList();
            tasks.removeIf(task -> task != POISON_PILL);
            if (tasks.isEmpty()) submit(POISON_PILL);
            workers.forEach(Thread::interrupt);
            return unstartedTasks;
        } finally {
            tasksLock.unlock();
        }
    }


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
                } catch (InterruptedException consumeAndIgnore) {
                }
            }
        }
    }
}