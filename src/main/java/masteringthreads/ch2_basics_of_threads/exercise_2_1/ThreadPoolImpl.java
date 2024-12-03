package masteringthreads.ch2_basics_of_threads.exercise_2_1;

import java.util.Objects;

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

    // TODO: Create a ArrayDeque field containing Runnable. This is our "tasks" queue.
    //  Hint: Since ArrayDeque is not thread-safe, we need to synchronize it. In
    //  this exercise, we will use monitor locks, i.e. synchronized. Use the
    //  ArrayDeque itself as a monitor lock, e.g. synchronized(tasks) {...}

    public ThreadPoolImpl(int poolSize) {
        if (poolSize < 1) throw new IllegalArgumentException("Invalid poolSize=" + poolSize);
        // TODO: Create as many Worker instances as poolSize and start them.
        //  Hint: Worker is an inner class defined at the bottom of this class

    }

    private Runnable take() throws InterruptedException {
        // TODO: if the ArrayDeque is empty, we wait
        //  remove the first task from the ArrayDeque
        //   if it is the POISON_PILL, add it back into ArrayDeque
        //  return the task
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void submit(Runnable task) {
        Objects.requireNonNull(task, "task==null");
        // TODO: If the last task in the ArrayDeque is the POISON_PILL, throw a
        //  RejectedExecutionException, otherwise add the task and notifyAll()
    }

    @Override
    public int getRunQueueLength() {
        // TODO: return the length of the ArrayDeque, excluding the POISON_PILL
        //  remember to also synchronize!
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void shutdown() {
        // TODO: If the last task in the ArrayDeque is not already the POISON_PILL,
        //  we want to submit it
    }

    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        public void run() {
            // TODO: we run in an infinite loop:
            //   remove the next task from the ArrayDeque using take()
            //   if it is our POISON_PILL, we return; otherwise we call run()
        }
    }
}
