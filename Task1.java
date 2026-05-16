import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Task1 {
    public static final int NACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 10000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Оберіть метод управління потоками:");
        System.out.println("1 - Використання ключового слова synchronized");
        System.out.println("2 - Використання явного блокування ReentrantLock");
        System.out.println("3 - Використання блокування читання/запису ReentrantReadWriteLock");
        System.out.println("4 - Без синхронізації ");
        System.out.print("Ваш вибір (1, 2, 3 або 4): ");

        int choice = 0;
        if (scanner.hasNextInt()) {
            choice = scanner.nextInt();
        } else {
            System.out.println("Помилка вводу. Завершення роботи.");
            return;
        }

        Bank b;
        switch (choice) {
            case 1:
                System.out.println("Запуск із synchronized");
                b = new SyncBank(NACCOUNTS, INITIAL_BALANCE);
                break;
            case 2:
                System.out.println("Запуск із ReentrantLock");
                b = new LockBank(NACCOUNTS, INITIAL_BALANCE);
                break;
            case 3:
                System.out.println("Запуск із ReentrantReadWriteLock");
                b = new RWLockBank(NACCOUNTS, INITIAL_BALANCE);
                break;
            case 4:
                System.out.println("Запуск БЕЗ синхронізації");
                b = new UnsyncBank(NACCOUNTS, INITIAL_BALANCE);
                break;
            default:
                System.out.println("Невірний вибір. Завершення роботи.");
                return;
        }

        for (int i = 0; i < NACCOUNTS; i++) {
            TransferThread t = new TransferThread(b, i, INITIAL_BALANCE);
            t.setPriority(Thread.NORM_PRIORITY + i % 2);
            t.start();
        }

        scanner.close();
    }
}

interface Bank {
    void transfer(int from, int to, int amount);
    void test();
    int size();
}

// ВАРІАНТ 4: Без синхронізації
class UnsyncBank implements Bank {
    public static final int NTEST = 10000;
    private final int[] accounts;
    private long ntransacts = 0;

    public UnsyncBank(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++) accounts[i] = initialBalance;
    }

    @Override
    public void transfer(int from, int to, int amount) {
        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;
        if (ntransacts % NTEST == 0) test();
    }

    @Override
    public void test() {
        int sum = 0;
        for (int account : accounts) sum += account;
        System.out.println("Transactions: " + ntransacts + " Sum: " + sum);
    }

    @Override
    public int size() { return accounts.length; }
}

// ВАРІАНТ 1: synchronized
class SyncBank implements Bank {
    public static final int NTEST = 10000;
    private final int[] accounts;
    private long ntransacts = 0;

    public SyncBank(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++) accounts[i] = initialBalance;
    }

    @Override
    public synchronized void transfer(int from, int to, int amount) {
        accounts[from] -= amount;
        accounts[to] += amount;
        ntransacts++;
        if (ntransacts % NTEST == 0) test();
    }

    @Override
    public synchronized void test() {
        int sum = 0;
        for (int account : accounts) sum += account;
        System.out.println("Transactions: " + ntransacts + " Sum: " + sum);
    }

    @Override
    public int size() { return accounts.length; }
}

// ВАРІАНТ 2: ReentrantLock
class LockBank implements Bank {
    public static final int NTEST = 10000;
    private final int[] accounts;
    private long ntransacts = 0;
    private final Lock bankLock = new ReentrantLock();

    public LockBank(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++) accounts[i] = initialBalance;
    }

    @Override
    public void transfer(int from, int to, int amount) {
        bankLock.lock();
        try {
            accounts[from] -= amount;
            accounts[to] += amount;
            ntransacts++;
            if (ntransacts % NTEST == 0) test();
        } finally {
            bankLock.unlock();
        }
    }

    @Override
    public void test() {
        bankLock.lock();
        try {
            int sum = 0;
            for (int account : accounts) sum += account;
            System.out.println("Transactions: " + ntransacts + " Sum: " + sum);
        } finally {
            bankLock.unlock();
        }
    }

    @Override
    public int size() { return accounts.length; }
}

// ВАРІАНТ 3: ReentrantReadWriteLock
class RWLockBank implements Bank {
    public static final int NTEST = 10000;
    private final int[] accounts;
    private long ntransacts = 0;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public RWLockBank(int n, int initialBalance) {
        accounts = new int[n];
        for (int i = 0; i < accounts.length; i++) accounts[i] = initialBalance;
    }

    @Override
    public void transfer(int from, int to, int amount) {
        rwLock.writeLock().lock();
        try {
            accounts[from] -= amount;
            accounts[to] += amount;
            ntransacts++;
            if (ntransacts % NTEST == 0) test();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void test() {
        rwLock.readLock().lock();
        try {
            int sum = 0;
            for (int account : accounts) sum += account;
            System.out.println("Transactions: " + ntransacts + " Sum: " + sum);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public int size() { return accounts.length; }
}

class TransferThread extends Thread {
    private Bank bank;
    private int fromAccount;
    private int maxAmount;
    private static final int REPS = 1000;

    public TransferThread(Bank b, int from, int max) {
        bank = b;
        fromAccount = from;
        maxAmount = max;
    }

    @Override
    public void run() {
        while (true) {
            for (int i = 0; i < REPS; i++) {
                int toAccount = (int) (bank.size() * Math.random());
                int amount = (int) (maxAmount * Math.random() / REPS);
                bank.transfer(fromAccount, toAccount, amount);
            }
        }
    }
}