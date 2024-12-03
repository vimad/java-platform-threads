package masteringthreads.ch3_the_secrets_of_concurrency.solution_3_1;

import masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_1.ThreadPoolEx;
import masteringthreads.util.AbstractThreadPoolExTester;

// DO NOT CHANGE
public class ThreadPoolTest extends AbstractThreadPoolExTester {
    @Override
    public ThreadPoolEx newThreadPool(int poolSize) {
        return new ThreadPoolImpl(poolSize);
    }
}