package masteringthreads.ch2_basics_of_threads.exercise_2_3;

// Note: this experiment only works on Windows
public class HorseRace {
    public static void main(String... args) throws InterruptedException {
        Runnable race = () -> {
            // todo: wait on a common lock object, e.g.
            // HorseRace.class.wait()
            System.out.println("STARTED: " + Thread.currentThread());
            // todo: run through a long loop or do some other exertion
            System.out.println("FINISHED: " + Thread.currentThread());
        };
        // todo: create a new thread for each priority (in a for loop) and start
        // it
        System.out.println("on your marks ...");
        Thread.sleep(1000);
        System.out.println("get set ...");
        Thread.sleep(1000);
        System.out.println("GO!!!");
        // todo: notify all threads waiting on the common lock object
    }
}
