public class Task4 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Асинхронне виведення");

        Thread t1 = new Thread(() -> printUnsync('|'));
        Thread t2 = new Thread(() -> printUnsync('\\'));
        Thread t3 = new Thread(() -> printUnsync('/'));

        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();

        System.out.println("\n\nСинхронне виведення");
        SyncController controller = new SyncController();

        Thread st1 = new Thread(() -> controller.print(0, '|'));
        Thread st2 = new Thread(() -> controller.print(1, '\\'));
        Thread st3 = new Thread(() -> controller.print(2, '/'));

        st1.start();
        st2.start();
        st3.start();
        st1.join();
        st2.join();
        st3.join();
        System.out.println();
    }

    static void printUnsync(char c) {
        for (int i = 0; i < 90; i++) {
            System.out.print(c);
        }
    }
}

//0 '|'
//1 '\'
//2 '/'
class SyncController {

    private int turn = 0;

    public synchronized void print(int expectedTurn, char c) {
        for (int i = 0; i < 90; i++) {
            while (turn != expectedTurn) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.print(c);

            turn = (turn + 1) % 3;

            notifyAll();
        }
    }
}