package masteringthreads.playground;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class ComputeIfAbsentDemo {
    public static void main(String... args) throws InterruptedException {
        long time = System.nanoTime();
        try {
            Map<Boolean, LongAdder> map = new ConcurrentHashMap<>();
            // ThreadLocalRandom.current().ints(1_000_000_000)
            //         .parallel()
            //         .forEach(i ->
            //                 map.computeIfAbsent(i % 2 == 0, k -> new LongAdder())
            //                         .increment());
            Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
            for (int i = 0; i < threads.length; i++) {
                int finalI = i;
                threads[i]= new Thread(() -> {
                     Random rand = ThreadLocalRandom.current();
                     for (int j = 0; j < 100_000_000/12; j++) {
                         map.computeIfAbsent(rand.nextInt() % 2 == 0, k -> new LongAdder())
                                 .increment();
                     }
                 });
                threads[i].start();
            }
            for (Thread thread : threads) thread.join();
            System.out.println("map = " + map);
        } finally {
            time = System.nanoTime() - time;
            System.out.printf("time = %dms%n", (time / 1_000_000));
        }
    }
}
