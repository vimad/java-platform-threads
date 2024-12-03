package masteringthreads.ch3_the_secrets_of_concurrency.solution_3_2;

import java.util.*;

public class BankAccountTest {
    public static void main(String... args) {
        var account = new BankAccount(1000);
        Runnable depositWithdraw = () -> {
            while (true) {
                account.deposit(100);
                account.withdraw(100);
            }
        };

        var thread1 = Thread.ofPlatform().start(depositWithdraw);
        var thread2 = Thread.ofPlatform().start(depositWithdraw);

        var timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                System.out.println("account.getBalance() = " + account.getBalance());
            }
        }, 1000, 1000);
    }
}
