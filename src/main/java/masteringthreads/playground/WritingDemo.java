package masteringthreads.playground;

public class WritingDemo {
    private static volatile int value;

    private static void increment() {
        value++;
    }
    public static void main(String... args) {
        for (int i = 0; i < 100; i++) {
            incrementLots();
        }
    }

    private static void incrementLots() {
        value = 0;
        long time = System.nanoTime();
        try {
            for (int i = 0; i < 1_000_000_000; i++) {
                increment();
            }
        } finally {
            time = System.nanoTime() - time;
            System.out.printf("time = %dms%n", (time / 1_000_000));
        }
    }
}
