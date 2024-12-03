package masteringthreads.playground;

import java.util.stream.*;

public class SingletonDemo {
    public static void main(String... args) {
        for (int i = 0; i < 10; i++) {
            test();
        }
    }

    private static void test() {
        long time = System.nanoTime();
        try {
            IntStream.range(0, 100_000_000)
                    .parallel()
                    .forEach(i -> SingletonHolder.getInstance()); // 0ms - Effective Java
                    // .forEach(i -> SingletonFixedDCL.getInstance()); // 7ms
                    // .forEach(i -> SingletonBrokenDCL.getInstance()); // 0ms
                    // .forEach(i -> SingletonSynchronizedGetInstance.getInstance()); // 4.2s
                    // .forEach(i -> SingletonClassic.getInstance()); // 0ms
        } finally {
            time = System.nanoTime() - time;
            System.out.printf("time = %dms%n", (time / 1_000_000));
        }
    }
}
