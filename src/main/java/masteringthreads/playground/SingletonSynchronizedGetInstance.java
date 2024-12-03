package masteringthreads.playground;

public class SingletonSynchronizedGetInstance {
    private static SingletonSynchronizedGetInstance instance;
    private final boolean initialized;

    private SingletonSynchronizedGetInstance() {
        this.initialized = true;
        System.out.println(getClass() + " initialized");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static synchronized SingletonSynchronizedGetInstance getInstance() {
        if (instance == null) {
            instance = new SingletonSynchronizedGetInstance();
        }
        return instance;
    }
}
