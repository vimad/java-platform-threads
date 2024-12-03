package masteringthreads.playground;

import java.util.concurrent.*;

public class WhichThreadNotify {
    public static void main(String... args) throws InterruptedException {
        var monitor = new Object();

        for (int i = 0; i < 1000; i++) {
            Thread.ofPlatform().name("thread-" + (i + 1))
                    .start(() -> {
                        synchronized (monitor) {
                            try {
                                monitor.wait();
                            } catch (InterruptedException e) {
                                throw new CancellationException("interrupted");
                            }
                            System.out.println("Woke up " + Thread.currentThread()
                                    .getName());
                        }
                    });
            Thread.sleep(1);
        }

        Thread.sleep(2000);

        for (int i = 0; i < 1000; i++) {
            synchronized (monitor) {
                monitor.notify();
            }
            Thread.sleep(1);
        }

        // synchronized (monitor) {
        //     monitor.notifyAll();
        // }
    }
}
