package masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_1;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;

import java.util.List;

public interface ThreadPoolEx extends ThreadPool {
    /**
     * Shuts down the thread pool abruptly, interrupting the worker threads
     * currently executing tasks, and returning a list of unstarted tasks.
     */
    List<Runnable> shutdownNow();
}