package masteringthreads.ch5_threading_problems.exercise_5_1;

public class PrinterClass {
    private static final boolean OUTPUT_TO_SCREEN = false;
    private boolean printingEnabled = OUTPUT_TO_SCREEN;

    public synchronized boolean isPrintingEnabled() {
        return printingEnabled;
    }
    public void print(String s) {
        print(this, s);
    }
    private synchronized static void print(PrinterClass pc, String s) {
        if (pc.isPrintingEnabled())
            System.out.println("Printing: " + s);
    }
    public synchronized void setPrintingEnabled(boolean printingEnabled) {
        if (!printingEnabled)
            print(this, "Printing turned off!");
        this.printingEnabled = printingEnabled;
    }
}
