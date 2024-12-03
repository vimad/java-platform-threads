package masteringthreads.util;

import masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_1.ThreadPoolEx;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// DO NOT CHANGE
public abstract class AbstractThreadPoolExTester extends AbstractThreadPoolTester {
    @Override
    public abstract ThreadPoolEx newThreadPool(int poolSize);

    @Test
    public void testTasksAreRemoved() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            LongAdder counter = new LongAdder();
            for (int i = 0; i < 10; i++) {
                pool.submit(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException consumeAndExit) {
                    }
                    counter.increment();
                });
            }
            Thread.sleep(100);
            List<Runnable> runnables = pool.shutdownNow();
            Thread.sleep(1000);
            assertEquals(1, counter.longValue());
        });
    }

    @Test
    public void testTasksAreStopped() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            var latch = new CountDownLatch(1);
            pool.submit(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            Thread.sleep(1000);
            List<Runnable> unstartedTasks = pool.shutdownNow();
            boolean noTimeout = latch.await(100, TimeUnit.MILLISECONDS);
            assertTrue("timeout occurred - did not shutdown the threads in time?", noTimeout);
            assertTrue(unstartedTasks.isEmpty());
        });
    }

    @Test
    public void testUnstartedTasksAreReturned() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            pool.submit(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException consumeAndExit) {
                }
            });

            Set<Integer> identities = ConcurrentHashMap.newKeySet();
            List<Runnable> extraJobs = IntStream.range(0, 100)
                    .mapToObj(i -> (Runnable) () -> identities.add(i))
                    .toList();
            extraJobs.forEach(pool::submit);
            Thread.sleep(200);
            List<Runnable> unstartedTasks = pool.shutdownNow();
            assertEquals(extraJobs.size(), unstartedTasks.size());
            unstartedTasks.forEach(Runnable::run);
            assertEquals(IntStream.range(0, 100).boxed().collect(Collectors.toSet()), identities);
        });
    }

    @Test
    public void testShutdownAndUnstartedTasksAreReturned() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            pool.submit(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException consumeAndExit) {
                }
            });

            Set<Integer> identities = ConcurrentHashMap.newKeySet();
            List<Runnable> extraJobs = IntStream.range(0, 100)
                    .mapToObj(i -> (Runnable) () -> identities.add(i))
                    .toList();
            extraJobs.forEach(pool::submit);
            Thread.sleep(200);
            pool.shutdown();
            Thread.sleep(200);
            List<Runnable> unstartedTasks = pool.shutdownNow();
            assertEquals(extraJobs.size(), unstartedTasks.size());
            unstartedTasks.forEach(Runnable::run);
            assertEquals(IntStream.range(0, 100).boxed().collect(Collectors.toSet()), identities);
        });
    }

    @Test
    public void testShutdownNowAlwaysInterrupts() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            var latch = new CountDownLatch(5);
            pool.submit(() -> {
                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException consumeAndExit) {
                    } finally {
                        latch.countDown();
                    }
                }
            });

            Thread.sleep(100);

            for (int i = 0; i < 5; i++) {
                assertTrue(pool.shutdownNow().isEmpty());
                Thread.sleep(100);
            }

            boolean noTimeout = latch.await(100, TimeUnit.MILLISECONDS);
            assertTrue("timeout occurred - did not interrupt the threads executing tasks?", noTimeout);
        });
    }
}

