package masteringthreads.playground;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class JavaThreadStates {
    public static void main(String... args) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().unstarted(() ->
                System.out.println("Shutting down ...")));
        var in = new BufferedReader(new InputStreamReader(System.in));
        var monitor = new Object();
        synchronized (monitor) {
            Thread.ofPlatform().name("thread1").start(() -> {
                synchronized (monitor) {
                    System.out.println("Got the monitor");
                }
            });
            System.out.println("Press Enter to continue.");
            in.readLine();
        }
        var lock = new ReentrantLock(); // fair
        lock.lock();
        try {
            Thread.ofPlatform().name("thread2").start(() -> {
                lock.lock();
                try {
                    System.out.println("Got the ReentrantLock");
                } finally {
                    lock.unlock();
                }
            });
            System.out.println("Press Enter to continue.");
            in.readLine();
        } finally {
            lock.unlock();
        }

        // Thread.sleep(100);
        lock.lock();
        try {
            Thread.ofPlatform().name("thread3").start(() -> {
                try {
                    if (lock.tryLock(1, TimeUnit.DAYS)) {
                        try {
                            System.out.println("Got the timed ReentrantLock");
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    throw new CancellationException();
                }
            });
            System.out.println("Press Enter to continue.");
            in.readLine();
        } finally {
            lock.unlock();
        }
    }
}
