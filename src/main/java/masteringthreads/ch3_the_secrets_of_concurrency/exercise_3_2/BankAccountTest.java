package masteringthreads.ch3_the_secrets_of_concurrency.exercise_3_2;

import java.util.*;

public class BankAccountTest {
    public static void main(String... args) {
        // create a BankAccount instance with balance = 1000
        var account = new BankAccount(1000);
        // create a Runnable lambda
        // in the run() method, call repeatedly:
        // account.deposit(100);
        // account.withdraw(100);
        Runnable depositWithdraw = () -> {
            while (true) {
                synchronized (account) {
                    account.deposit(100);
                }
                Thread.yield();
                synchronized (account) {
                    account.withdraw(100);
                }
                Thread.yield();
            }
        };
        // create two thread instances, both pointing at
        // your Runnable
        Thread.ofPlatform().start(depositWithdraw);
        Thread.ofPlatform().start(depositWithdraw);
        // create a timer task to once a second prints
        // out the balance
        //
        // Balance should be 1000, 1100 or 1200, nothing else
        var timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (account) {
                    System.out.println(account.getBalance());
                }
            }
        }, 1000, 1000);
    }
}
