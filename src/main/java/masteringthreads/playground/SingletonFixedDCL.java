package masteringthreads.playground;

public class SingletonFixedDCL {
    private static volatile SingletonFixedDCL instance;
    private final boolean initialized;

    private SingletonFixedDCL() {
        this.initialized = true;
        System.out.println(getClass() + " initialized");
    }

    public void testSanity() {
        if (initialized != initialized) throw new AssertionError();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static SingletonFixedDCL getInstance() {
        if (instance == null) {
            synchronized (SingletonFixedDCL.class) {
                if (instance == null) {
                    instance = new SingletonFixedDCL();
                }
            }
        }
        return instance;
    }
}
