package masteringthreads.playground;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class InterruptedDemo {
    public static void main(String... args) throws InterruptedException {
        var thread1 = Thread.ofPlatform().name("thread1")
                .start(() -> {
                    try {
                        printInterruptStatus();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        printInterruptStatus();
                    }
                });
        Thread.sleep(100);
        thread1.interrupt();

        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            var thread2 = Thread.ofPlatform().name("thread2")
                    .start(() -> {
                        try {
                            printInterruptStatus();
                            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                                lock.unlock();
                            }
                        } catch (InterruptedException e) {
                            printInterruptStatus();
                        }
                    });
            Thread.sleep(100);
            thread2.interrupt();
        } finally {
            lock.unlock();
        }

        var condition = lock.newCondition();
        var thread3 = Thread.ofPlatform().name("thread3")
                .start(() -> {
                    printInterruptStatus();
                    lock.lock();
                    try {
                        condition.awaitUninterruptibly();
                    } finally {
                        lock.unlock();
                    }
                    printInterruptStatus();
                    Thread.interrupted(); // resets the interrupt state
                    try {
                        lock.lockInterruptibly();
                        try {
                            // do something
                        } finally {
                            lock.unlock();
                        }
                    } catch(InterruptedException e){
                    }
                    printInterruptStatus();
                });
        Thread.sleep(100);
        System.out.println("interrupting thread3");
        thread3.interrupt();
        Thread.sleep(100);
        System.out.println("signalling condition");
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    private static void printInterruptStatus() {
        System.out.println(Thread.currentThread().getName() +
                " interrupted=" + Thread.currentThread().isInterrupted());
    }
}
