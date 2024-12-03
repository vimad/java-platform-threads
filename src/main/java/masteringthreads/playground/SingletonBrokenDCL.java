package masteringthreads.playground;

public class SingletonBrokenDCL {
    private static SingletonBrokenDCL instance;
    private final boolean initialized;

    private SingletonBrokenDCL() {
        this.initialized = true;
        System.out.println(getClass() + " initialized");
    }

    public void testSanity() {
        if (initialized != initialized) throw new AssertionError();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static SingletonBrokenDCL getInstance() {
        if (instance == null) {
            synchronized (SingletonBrokenDCL.class) {
                if (instance == null) {
                    instance = new SingletonBrokenDCL();
                }
            }
        }
        return instance;
    }
}
