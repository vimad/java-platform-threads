package masteringthreads.playground;

public class RunnerVolatile implements RunnerIntf {
    private volatile boolean running = true;

    public void doJob() {
        while (running) {
            // do something
        }
    }

    public void shutdown() {
        running = false;
    }
}