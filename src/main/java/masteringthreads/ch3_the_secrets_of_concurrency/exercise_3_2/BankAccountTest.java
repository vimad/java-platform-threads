package masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_2;

import masteringthreads.ch3_the_secrets_of_concurrency.solution_3_2.BankAccount;

import java.util.Timer;
import java.util.TimerTask;

public class BankAccountTest {
    public static void main(String... args) {
        // create a BankAccount instance with balance = 1000
        // create a Runnable lambda
        // in the run() method, call repeatedly:
        // account.deposit(100);
        // account.withdraw(100);
        // create two thread instances, both pointing at
        // your Runnable
        // create a timer task to once a second prints
        // out the balance
        //
        // Balance should be 1000, 1100 or 1200, nothing else
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
        timer.schedule(
            new TimerTask() {
                public void run() {
                    System.out.println("account.getBalance() = " + account.getBalance());
                }
            }, 1000, 1000
        );
    }
}
