package masteringthreads.ch4_applied_threading_techniques.exercise_4_4;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

// DO NOT CHANGE
public class HouseDrawingTest {
    public static void main(String... args) {
        final Phaser phaser = new Phaser(4);

        class SuspendedHouseDrawing extends HouseDrawing {
            public SuspendedHouseDrawing(String title, String colour) {
                super(title, colour);
            }

            @Override
            public void draw() {
                phaser.arriveAndAwaitAdvance();
                super.draw();
            }
        }

        try (ExecutorService pool = Executors.newCachedThreadPool()) {
            pool.submit(() -> new SuspendedHouseDrawing("HomeSweetHome1", "beige").draw());
            pool.submit(() -> new SuspendedHouseDrawing("HomeSweetHome2", "red").draw());
            pool.submit(() -> new SuspendedHouseDrawing("HomeSweetHome3", "green").draw());
            pool.submit(() -> new SuspendedHouseDrawing("HomeSweetHome4", "purple").draw());
        }
    }
}