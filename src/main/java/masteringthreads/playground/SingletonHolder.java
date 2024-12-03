package masteringthreads.playground;

public class SingletonHolder {
    private final boolean initialized;

    private SingletonHolder() {
        this.initialized = true;
        System.out.println(getClass() + " initialized");
    }

    private static class Holder {
        private static final SingletonHolder instance = new SingletonHolder();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public static SingletonHolder getInstance() {
        return Holder.instance;
    }
}
