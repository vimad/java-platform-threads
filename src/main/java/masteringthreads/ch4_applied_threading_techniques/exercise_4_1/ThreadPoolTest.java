package masteringthreads.ch4_applied_threading_techniques.exercise_4_1;

import masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_1.ThreadPoolEx;
import masteringthreads.util.AbstractThreadPoolEx2Tester;

// DO NOT CHANGE
public class ThreadPoolTest extends AbstractThreadPoolEx2Tester {
    @Override
    public ThreadPoolEx2 newThreadPool(int poolSize) {
        return new ThreadPoolImpl(poolSize);
    }
}
