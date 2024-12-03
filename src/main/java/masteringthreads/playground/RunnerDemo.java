package masteringthreads.playground;

public class RunnerDemo {
    public static void main(String... args) throws InterruptedException {
        var runner = new RunnerVolatile();

        Thread.ofPlatform().start(() -> {
            runner.doJob();
            System.out.println("Job is done!");
        });

        Thread.sleep(1000);
        runner.shutdown();
    }
}
