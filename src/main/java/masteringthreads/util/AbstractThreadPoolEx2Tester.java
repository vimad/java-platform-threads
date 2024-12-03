package masteringthreads.util;

import masteringthreads.ch4_applied_threading_techniques.exercise_4_1.ThreadPoolEx2;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static org.junit.Assert.*;

// DO NOT CHANGE
public abstract class AbstractThreadPoolEx2Tester extends AbstractThreadPoolExTester {
    @Override
    public abstract ThreadPoolEx2 newThreadPool(int poolSize);

    @Test
    public void testShutdownDone() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(10);
            for (int i = 0; i < 10; i++) {
                int finalI = i;
                pool.submit(() -> {
                    try {
                        Thread.sleep(finalI * 100);
                    } catch (InterruptedException consumeAndExit) {}
                });
            }
            Thread.sleep(200);
            pool.shutdown();
            assertFalse(pool.awaitTermination(100, TimeUnit.MILLISECONDS));
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(100, TimeUnit.MILLISECONDS));
        });
    }
}

