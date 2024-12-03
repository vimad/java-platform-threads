package masteringthreads.playground;

public class SingletonClassic {
    private static final SingletonClassic instance = new SingletonClassic();
    private final boolean initialized;

    private SingletonClassic() {
        this.initialized = true;
        System.out.println(getClass() + " initialized");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static SingletonClassic getInstance() {
        return instance;
    }
}
