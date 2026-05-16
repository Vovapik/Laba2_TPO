import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Task3 {

    public static void main(String[] args) throws InterruptedException {
        Journal journal = new Journal();

        String[] groups = {"Група 31", "Група 32", "Група 33"};
        for (String group : groups) {
            for (int i = 1; i <= 3; i++) {
                journal.addStudent(new Student("Студент_" + i + "_" + group.substring(6), group));
            }
        }

        int weeks = 4;
        List<Thread> threads = new ArrayList<>();

        Thread lecturer = new Thread(new LecturerTask(journal, weeks, "Лектор"));
        threads.add(lecturer);

        for (int i = 0; i < 3; i++) {
            Thread assistant = new Thread(new AssistantTask(journal, groups[i], weeks, "Асистент " + (i + 1)));
            threads.add(assistant);
        }

        System.out.println("ПОЧАТОК НАВЧАЛЬНОГО СЕМЕСТРУ");

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("\nНАВЧАЛЬНИЙ СЕМЕСТР ЗАВЕРШЕНО");

        journal.printJournal();
    }
}

class Student {
    private final String name;
    private final String group;
    private final List<Integer> grades = new CopyOnWriteArrayList<>();

    public Student(String name, String group) {
        this.name = name;
        this.group = group;
    }

    public String getName() { return name; }
    public String getGroup() { return group; }
    public List<Integer> getGrades() { return grades; }

    public void addGrade(int grade) {
        grades.add(grade);
    }
}

class Journal {
    private final List<Student> students = new ArrayList<>();

    public void addStudent(Student student) {
        students.add(student);
    }

    public List<Student> getStudentsByGroup(String group) {
        List<Student> groupStudents = new ArrayList<>();
        for (Student s : students) {
            if (s.getGroup().equals(group)) {
                groupStudents.add(s);
            }
        }
        return groupStudents;
    }

    public List<Student> getAllStudents() {
        return students;
    }

    public void printJournal() {
        System.out.println("\nЕКЗАМЕНАЦІЙНА ВІДОМІСТЬ:");
        String currentGroup = "";
        for (Student s : students) {
            if (!s.getGroup().equals(currentGroup)) {
                currentGroup = s.getGroup();
                System.out.println("\n" + currentGroup);
            }
            System.out.printf("%-15s | Оцінки: %s%n", s.getName(), s.getGrades());
        }
    }
}

class LecturerTask implements Runnable {
    private final Journal journal;
    private final int weeks;
    private final String name;
    private final Random random = new Random();

    public LecturerTask(Journal journal, int weeks, String name) {
        this.journal = journal;
        this.weeks = weeks;
        this.name = name;
    }

    @Override
    public void run() {
        for (int w = 1; w <= weeks; w++) {
            System.out.println("Тиждень " + w + ": " + name + " проводить лекцію...");

            String[] groups = {"Група 31", "Група 32", "Група 33"};
            for (String group : groups) {
                List<Student> studs = journal.getStudentsByGroup(group);
                if (!studs.isEmpty()) {
                    Student luckyStudent = studs.get(random.nextInt(studs.size()));
                    int grade = random.nextInt(101);
                    luckyStudent.addGrade(grade);
                }
            }

        }
    }
}

class AssistantTask implements Runnable {
    private final Journal journal;
    private final String targetGroup;
    private final int weeks;
    private final String name;
    private final Random random = new Random();

    public AssistantTask(Journal journal, String targetGroup, int weeks, String name) {
        this.journal = journal;
        this.targetGroup = targetGroup;
        this.weeks = weeks;
        this.name = name;
    }

    @Override
    public void run() {
        for (int w = 1; w <= weeks; w++) {
            List<Student> studs = journal.getStudentsByGroup(targetGroup);
            for (Student s : studs) {
                int grade = 50 + random.nextInt(51);
                s.addGrade(grade);
            }
            System.out.println("Тиждень " + w + ": " + name + " виставив оцінки за практику для " + targetGroup);

        }
    }
}