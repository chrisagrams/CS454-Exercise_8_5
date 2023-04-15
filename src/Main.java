import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class Account {
    int balance;
    Lock lock;

    Condition preferred;
    boolean isPreferred;

    public Account(int initialBalance) {
        balance = initialBalance;
        lock = new ReentrantLock();
        preferred = lock.newCondition();
        isPreferred = false;
    }

    public void deposit(int k) {
        lock.lock();
        try {
            balance += k;
            preferred.signalAll();
            System.out.println("Deposit " + k + " to account " + Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    public void withdraw(int k) {
        lock.lock();
        try {
            while (balance < k || isPreferred) {
                preferred.await();
            }
            balance -= k;
            preferred.signalAll();
            System.out.println("Withdraw " + k + " from account " + Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    public void preferredWithdraw(int k) {
        lock.lock();
        try {
            isPreferred = true;
            while (balance < k) {
                preferred.await();
            }
            balance -= k;
            isPreferred = false;
            preferred.signalAll();
            System.out.println("Preferred withdraw " + k + " from account " + Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /* For question 3 */
//    public void transfer(int k, Account reserve) {
//        lock.lock();
//        try {
//            reserve.withdraw(k);
//            deposit(k);
//        } finally {
//            lock.unlock();
//        }
//    }
}

public class Main {
    public static void main(String[] args) {
        int numAccounts = 10;
        ArrayList<Account> accounts = new ArrayList<>();
        for (int i = 0; i < numAccounts; i++) {
            accounts.add(new Account(500));
        }

        Runnable test = () -> {
            Random r = new Random();
            int ThreadID = Integer.parseInt(Thread.currentThread().getName());
            Account a = accounts.get(ThreadID);

            for (int i = 0; i < 3; i++)
            {
                int op = r.nextInt(3);
                int amount = r.nextInt(100);

                if (op == 0) { // deposit
                    a.deposit(amount);
                } else if (op == 1) { // withdraw (non-preferred)
                    a.withdraw(amount);
                }
                else { // withdraw (preferred)
                    a.preferredWithdraw(amount);
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numAccounts; i++) {
            threads.add(new Thread(test));
            threads.get(i).setName(Integer.toString(i));
            threads.get(i).start();
        }

        for (int i = 0; i < numAccounts; i++) {
            try {
                threads.get(i).join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Final balances:");
        for (int i = 0; i < numAccounts; i++) {
            System.out.println("Account " + i + ": " + accounts.get(i).balance);
        }
    }
}