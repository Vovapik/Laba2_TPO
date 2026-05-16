import java.util.Scanner;

public class Task2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Оберіть розмір масиву даних для обробки:");
        System.out.println("1 - 100 елементів");
        System.out.println("2 - 1000 елементів");
        System.out.println("3 - 5000 елементів");
        System.out.print("Ваш вибір: ");

        int size = 0;
        int choice = scanner.nextInt();
        switch (choice) {
            case 1: size = 100; break;
            case 2: size = 1000; break;
            case 3: size = 5000; break;
            default:
                System.out.println("Невірний вибір. Встановлено розмір за замовчуванням: 100.");
                size = 100;
        }

        Drop drop = new Drop();

        System.out.println("\nПочаток передачі " + size + " елементів");
        new Thread(new Producer(drop, size)).start();
        new Thread(new Consumer(drop, size)).start();

        scanner.close();
    }
}

class Drop {
    private int message;

    private boolean empty = true;

    public synchronized int take() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        empty = true;
        notifyAll();
        return message;
    }

    public synchronized void put(int message) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        empty = false;
        this.message = message;
        notifyAll();
    }
}

class Producer implements Runnable {
    private Drop drop;
    private int[] data;

    public Producer(Drop drop, int size) {
        this.drop = drop;
        this.data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i + 1;
        }
    }

    public void run() {
        for (int i = 0; i < data.length; i++) {
            drop.put(data[i]);
        }
        drop.put(-1);
    }
}

class Consumer implements Runnable {
    private Drop drop;
    private int expectedSize;

    public Consumer(Drop drop, int expectedSize) {
        this.drop = drop;
        this.expectedSize = expectedSize;
    }

    public void run() {
        int receivedMessage = 0;
        int count = 0;
        long actualSum = 0;

        while ((receivedMessage = drop.take()) != -1) {
            count++;
            actualSum += receivedMessage;
        }

        long expectedSum = (long) expectedSize * (expectedSize + 1) / 2;

        System.out.println("Статистика Споживача");
        System.out.println("Очікувана кількість: " + expectedSize + " | Отримано: " + count);
        System.out.println("Очікувана сума:      " + expectedSum + " | Фактична сума: " + actualSum);

        if (count == expectedSize && expectedSum == actualSum) {
            System.out.println("РЕЗУЛЬТАТ: Програма працює  коректно. Втрат чи дублювання даних немає.");
        } else {
            System.out.println("РЕЗУЛЬТАТ: Є втрата даних або стан гонитви (race condition).");
        }
    }
}