package masteringthreads.ch2_basics_of_threads.solution_2_1;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;
import masteringthreads.util.AbstractThreadPoolTester;

// DO NOT CHANGE
public class ThreadPoolTest extends AbstractThreadPoolTester {
    @Override
    public ThreadPool newThreadPool(int poolSize) {
        return new ThreadPoolImpl(poolSize);
    }
}