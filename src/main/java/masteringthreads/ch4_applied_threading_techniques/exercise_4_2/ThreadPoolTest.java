package masteringthreads.ch4_applied_threading_techniques.exercise_4_2;

import masteringthreads.ch4_applied_threading_techniques.exercise_4_1.ThreadPoolEx2;
import masteringthreads.util.AbstractThreadPoolEx2Tester;

// DO NOT CHANGE
public class ThreadPoolTest extends AbstractThreadPoolEx2Tester {
    @Override
    public ThreadPoolEx2 newThreadPool(int poolSize) {
        return new ThreadPoolImpl(poolSize);
    }
}
