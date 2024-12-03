package masteringthreads.playground;

public class Runner implements RunnerIntf {
    private boolean running = true;

    public void doJob() {
        while (running) {
            // do something
        }
    }

    public void shutdown() {
        running = false;
    }
}