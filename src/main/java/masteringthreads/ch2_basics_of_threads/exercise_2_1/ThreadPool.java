package masteringthreads.ch2_basics_of_threads.exercise_2_1;

public interface ThreadPool {
    /**
     * Submits a task to the thread pool, where it will be put in a queue and
     * then executed by the next available idle thread.
     *
     * @param task the Runnable to execute
     * @throws java.util.concurrent.RejectedExecutionException if the pool has been shut down
     * @throws NullPointerException                            if the task is null
     */
    void submit(Runnable task);

    /**
     * {@return the number of unstarted tasks}
     */
    int getRunQueueLength();

    /**
     * Shuts down the thread pool, allowing the tasks that have already been submitted to complete.
     * No further tasks can be submitted to the pool. Once all the jobs have been completed, the
     * worker threads need to terminate themselves.
     */
    void shutdown();
}
