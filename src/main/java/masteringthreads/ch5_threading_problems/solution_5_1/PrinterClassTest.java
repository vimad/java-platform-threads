package masteringthreads.ch5_threading_problems.solution_5_1;

import masteringthreads.ch5_threading_problems.exercise_5_1.PrinterClass;

public class PrinterClassTest {
    public static void main(String... args) {
        var pc = new PrinterClass();
        new Thread() {
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    pc.print("testing");
                }
            }
        }.start();
        new Thread() {
            public void run() {
                for (int i = 0; i < 10000000; i++) {
                    pc.setPrintingEnabled(false);
                }
            }
        }.start();
    }
}
