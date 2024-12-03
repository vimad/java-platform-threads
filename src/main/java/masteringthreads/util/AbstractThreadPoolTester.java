package masteringthreads.util;

import masteringthreads.ch2_basics_of_threads.exercise_2_1.ThreadPool;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

import static org.junit.Assert.*;

// DO NOT CHANGE
public abstract class AbstractThreadPoolTester {
    public abstract ThreadPool newThreadPool(int poolSize);

    @Test
    public void testThatAllTasksAreExecuted() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            int tasks = 1_000_000;
            var latch = new CountDownLatch(tasks);
            var pool = newThreadPool(10);
            for (int i = 0; i < tasks; i++) {
                pool.submit(latch::countDown);
            }
            pool.shutdown();
            assertTrue(latch.await(500, TimeUnit.MILLISECONDS));
        });
    }

    @Test
    public void testIdleThreadsShutdown() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            int tasks = 1_000;
            var latch = new CountDownLatch(tasks);
            var pool = newThreadPool(10);
            for (int i = 0; i < tasks; i++) {
                pool.submit(latch::countDown);
            }
            assertTrue(latch.await(500, TimeUnit.MILLISECONDS));

            Thread.sleep(100);

            // call shutdown when all the threads are idle
            pool.shutdown();
        });
    }

    //@Test
    public void testTasksAreStopped() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            var latch = new CountDownLatch(1);
            var time = System.currentTimeMillis();
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
            pool.shutdown();
            boolean noTimeout = latch.await(100, TimeUnit.MILLISECONDS);
            assertTrue("timeout occurred - did not shutdown the threads in time?", noTimeout);
            time = System.currentTimeMillis() - time;
        });
    }

    @Test
    public void testThatRunnablesAreExecutedConcurrently() throws InterruptedException {
        checkAllThreadsAreDone(() -> checkStandardThreadPoolFunctionality(newThreadPool(10)));
    }

    @Test
    public void testSynchronizingOnListObject() throws ReflectiveOperationException, InterruptedException {
        checkAllThreadsAreDone(() -> {
            if (checkListAndBlockingQueue()) return;

            var pool = newThreadPool(10);
            Phaser phaser = new Phaser(3);

            Consumer<Runnable> locker = findFieldValue(pool, ReentrantLock.class)
                    .<Consumer<Runnable>>map(lck -> runnable -> {
                        lck.lock();
                        try {
                            runnable.run();
                        } finally {
                            lck.unlock();
                        }
                        phaser.arriveAndDeregister();
                    }).orElseGet(() -> runnable -> {
                        var list = findFieldValue(pool, ArrayDeque.class)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Field of type ArrayDeque not found"));
                        synchronized (list) {
                            runnable.run();
                        }
                        phaser.arriveAndDeregister();
                    });

            locker.accept(() -> {
                var thread = Thread.ofPlatform()
                        .name("submitThread")
                        .start(() ->
                                pool.submit(() -> System.out.println("submit worked")));
                try {
                    thread.join(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                assertTrue("In submit(), we expected the pool to be synchronizing list access using the list object as a monitor lock or to use a ReentrantLock", thread.isAlive());
            });
            locker.accept(() -> {
                var thread = Thread.ofPlatform()
                        .name("runQueueThread")
                        .start(() ->
                                System.out.println("pool.getRunQueueLength() = " + pool.getRunQueueLength()));
                try {
                    thread.join(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                assertTrue("In getRunQueueLength(), we expected the pool to be synchronizing list access using the list object as a monitor lock or to use a ReentrantLock", thread.isAlive());
            });
            phaser.arriveAndAwaitAdvance();
            Thread.sleep(100);
            pool.shutdown();
        });
    }

    @Test
    public void testSpuriousWakeupsAreHandledCorrectly() throws InterruptedException, IllegalAccessException {
        checkAllThreadsAreDone(() -> {
            if (checkListAndBlockingQueue()) return;

            var pool = newThreadPool(10);
            Thread.sleep(100);

            Runnable notifier = findFieldValue(pool, ReentrantLock.class)
                    .<Runnable>map(lck -> () -> {
                        Condition condition = findFieldValue(pool, Condition.class)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Field of type Condition should be paired with ReentrantLock"
                                ));
                        lck.lock();
                        try {
                            condition.signalAll();
                        } finally {
                            lck.unlock();
                        }
                    }).orElseGet(() -> () -> {
                        var list = findFieldValue(pool, ArrayDeque.class)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Field of type ArrayDeque not found"));
                        synchronized (list) {
                            list.notifyAll();
                        }
                    });

            for (int i = 0; i < 20; i++) {
                notifier.run();
            }
            checkStandardThreadPoolFunctionality(pool);
        });
    }

    private boolean checkListAndBlockingQueue() {
        ThreadPool sample = newThreadPool(1);
        try {
            var foundArrayDequeField = false;
            var foundBlockingQueueField = false;
            for (var field : sample.getClass().getDeclaredFields()) {
                if (Executor.class.isAssignableFrom(field.getType())) {
                    return true;
                } else if (Collection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object collection = field.get(sample);
                    if (collection instanceof ArrayDeque)
                        foundArrayDequeField = true;
                    else if (collection instanceof BlockingQueue)
                        foundBlockingQueueField = true;
                }
            }
            if (foundBlockingQueueField && !foundArrayDequeField) return true;
            if (foundBlockingQueueField && foundArrayDequeField)
                fail("We don't need a ArrayDeque for the tasks if we use a BlockingQueue");
            if (!foundArrayDequeField)
                fail("We need a ArrayDeque field for the tasks");
            return false;
        } catch (IllegalAccessException e) {
            fail(e.toString());
            throw new AssertionError(e);
        } finally {
            sample.shutdown();
        }
    }

    private void checkStandardThreadPoolFunctionality(ThreadPool pool) throws InterruptedException {
        var latch = new CountDownLatch(19);
        var time = System.currentTimeMillis();
        for (int i = 0; i < 19; i++) {
            pool.submit(() -> {
                try {
                    Thread.sleep(1000);
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        boolean noTimeout = latch.await(3, TimeUnit.SECONDS);
        assertTrue("timeout occurred - did you start your threads?", noTimeout);
        time = System.currentTimeMillis() - time;
        pool.shutdown();
        if (pool.getRunQueueLength() != 0) {
            throw new AssertionError("Queue was not empty: "
                    + pool.getRunQueueLength());
        }
        assertTrue("Total time exceeded limits", time < 2400);
        assertFalse("Faster than expected", time < 1900);
    }

    private <E> Optional<E> findFieldValue(ThreadPool pool, Class<E> fieldType) {
        try {
            for (var field : pool.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (fieldType.isInstance(field.get(pool)))
                    return Optional.of(fieldType.cast(field.get(pool)));
            }
            return Optional.empty();
        } catch (IllegalAccessException e) {
            return Optional.empty();
        }
    }

    private Thread interrupted = null;

    //@Test
    public void testForBackupBoolean() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var latch = new CountDownLatch(8);
            var pool = newThreadPool(10);
            for (int i = 0; i < 12; i++) {
                pool.submit(() -> {
                    try {
                        Thread.sleep(1000);
                        latch.countDown();
                    } catch (InterruptedException e) {
                        interrupted = Thread.currentThread();
                    }
                });
            }
            boolean noTimeout = latch.await(2, TimeUnit.SECONDS);
            assertTrue("timeout occurred - did you start your threads?", noTimeout);
            pool.shutdown();
            Thread.sleep(100);
            assertTrue("Did you have a backup boolean?",
                    interrupted == null || !interrupted.isAlive());
        });
    }

    @Test
    public void testExceptions() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            assertThrows(NullPointerException.class, () -> pool.submit(null));
            assertThrows(IllegalArgumentException.class, () -> newThreadPool(0));
            assertThrows(IllegalArgumentException.class, () -> newThreadPool(-10));
            pool.shutdown();
        });
    }

    protected interface InterruptibleRunnable {
        void run() throws InterruptedException;
    }

    protected final void checkAllThreadsAreDone(InterruptibleRunnable task) throws InterruptedException {
        ThreadGroup group = new ThreadGroup("testgroup");
        ThreadFactory factory = Thread.ofPlatform().group(group)
                .name("checkAllThreadsAreDone-worker").factory();
        try (var pool = Executors.newCachedThreadPool(factory)) {
            Future<?> future = pool.submit(() -> {
                        var activeCountBeforeStart = group.activeCount();
                        Thread[] threadsBefore = new Thread[activeCountBeforeStart + 10];
                        group.enumerate(threadsBefore);
                        task.run();
                        Thread.sleep(100);
                        var activeCountAfterShutdown = group.activeCount();
                        Thread[] threadsAfter = new Thread[activeCountAfterShutdown + 10];
                        group.enumerate(threadsAfter);
                        assertEquals("Threads before: " +
                                        threads(threadsBefore) + ", threads after: " +
                                        threads(threadsAfter),
                                activeCountBeforeStart, activeCountAfterShutdown);
                        return null;
                    }
            );
            future.get();
        } catch (ExecutionException e) {
            try {
                throw e.getCause();
            } catch (RuntimeException | Error err) {
                throw err;
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
        }
    }

    private static String threads(Thread[] threads) {
        return Stream.of(threads)
                .takeWhile(Objects::nonNull)
                .map(Thread::getName)
                .toList()
                .toString();
    }

    @Test
    public void testWeCanCallShutdownSeveralTimes() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            pool.shutdown();
            pool.shutdown();
            pool.shutdown();
        });
        checkAllThreadsAreDone(() -> { // test the test
        });
    }

    @Test
    public void testRunQueueLengthStartsAtZero() throws InterruptedException {
        checkAllThreadsAreDone(() -> {
            var pool = newThreadPool(1);
            assertEquals(0, pool.getRunQueueLength());
            pool.shutdown();
        });
    }
}

