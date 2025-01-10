import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

enum UserType {
    ADMIN,
    STUDENT
}

class Subject {
    int subjectNumber;
    int attended;
    int missed;

    public Subject(int subjectNumber) {
        this.subjectNumber = subjectNumber;
    }

    public double calculateAttendancePercentage() {
        int totalLectures = attended + missed;
        return (double) attended / totalLectures * 100;
    }

    public void setAttendance(int attended, int missed) {
        this.attended = attended;
        this.missed = missed;
    }

    public boolean isAttendanceBelowThreshold() {
        double attendancePercentage = calculateAttendancePercentage();
        return attendancePercentage < 80;
    }
}

abstract class User {
    String name;

    public User(String name) {
        this.name = name;
    }

    public abstract void displayAttendanceSummary();
}

class Student extends User {
    List<Subject> subjects;

    public Student(String name) {
        super(name);
        this.subjects = new ArrayList<>();
    }

    public void addSubject(Subject subject) {
        subjects.add(subject);
    }

    @Override
    public void displayAttendanceSummary() {
        for (Subject subject : subjects) {
            int attended = subject.attended;
            int missed = subject.missed;
            double attendancePercentage = subject.calculateAttendancePercentage();
            System.out.println("Attendance Summary for " + name + " in Subject " + subject.subjectNumber + ":");
            System.out.println("Total Lectures Attended: " + attended);
            System.out.println("Total Lectures Missed: " + missed);
            System.out.println("Attendance Percentage: " + attendancePercentage + "%");

            if (subject.isAttendanceBelowThreshold()) {
                System.out.println("This student is a defaulter in this subject.");
            }
        }
    }
}

class Admin extends User {
    List<Student> students;
    String adminName;

    public Admin(String name) {
        super(name);
        this.students = new ArrayList<>();
        this.adminName = name;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public boolean checkStudentExists(String studentName) {
        for (Student student : students) {
            if (student.name.equals(studentName)) {
                return true;
            }
        }
        return false;
    }

    public Student getStudentByName(String studentName) {
        for (Student student : students) {
            if (student.name.equals(studentName)) {
                return student;
            }
        }
        return null;
    }

    @Override
    public void displayAttendanceSummary() {
        for (Student student : students) {
            student.displayAttendanceSummary();
        }
    }

    public void saveToFile(String fileName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        for (Student student : students) {
            writer.write(student.name);
            writer.newLine();

            for (Subject subject : student.subjects) {
                writer.write(subject.subjectNumber + "," + subject.attended + "," + subject.missed);
                writer.newLine();
            }

            writer.write("---");
            writer.newLine();
        }

        writer.close();
    }
public void loadFromFile(String fileName) throws IOException {
    students.clear();

    BufferedReader reader = new BufferedReader(new FileReader(fileName));

    String line;
    while ((line = reader.readLine()) != null) {
        if (line.equals("---")) {
            continue;
        }

        if (line.contains(",")) {
            String[] parts = line.split(",");
            String studentName = parts[0];
            Student student = new Student(studentName);

            int subjectNumber = Integer.parseInt(parts[1]);
            Subject subject = new Subject(subjectNumber);
            subject.setAttendance(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            student.addSubject(subject);

            students.add(student);
        } else {
            students.add(new Student(line));
        }
    }

    reader.close();
}
}

public class AttendanceManagementSystem {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Attendance Management System");

        String continueInput = "yes";
        String adminName = "";
        List<Student> adminStudents = new ArrayList<>();
        String fileName = "students.txt";

        while (continueInput.equalsIgnoreCase("yes")) {
            System.out.print("Are you an Admin or a Student? (Enter 'admin' or 'student'): ");
            String userTypeInput = scanner.nextLine().toLowerCase();

            UserType userType = UserType.valueOf(userTypeInput.toUpperCase());

            if (userType == UserType.ADMIN) {
                if (adminName.isEmpty()) {
                    System.out.print("Enter Admin name: ");
                    adminName = scanner.nextLine();
                }
                Admin admin = new Admin(adminName);

                System.out.print("Enter the number of students: ");
                int totalStudents = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                for (int i = 1; i <= totalStudents; i++) {
                    System.out.print("Enter student " + i + "'s name: ");
                    String studentName = scanner.nextLine();

                    Student student = new Student(studentName);

                    System.out.print("Enter total number of subjects for " + studentName + ": ");
                    int totalSubjects = scanner.nextInt();
                    scanner.nextLine(); // Consume newline character

                    for (int j = 1; j <= totalSubjects; j++) {
                        Subject subject = new Subject(j);
                        System.out.print("Enter number of lectures attended for Subject " + subject.subjectNumber + ": ");
                        int attended = scanner.nextInt();
                        System.out.print("Enter number of lectures missed for Subject " + subject.subjectNumber + ": ");
                        int missed = scanner.nextInt();
                        scanner.nextLine(); // Consume newline character

                        subject.setAttendance(attended, missed);
                        student.addSubject(subject);
                    }

                    admin.addStudent(student);
                }

                adminStudents = admin.students; // Store the admin's students for later use

                System.out.println("\nAdmin View:");
                admin.displayAttendanceSummary();

                try {
                    admin.saveToFile(fileName);
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }

            } else if (userType == UserType.STUDENT) {
                System.out.print("Enter Student name: ");
                String studentName = scanner.nextLine();

                Admin admin = new Admin(adminName); // Create a temporary admin instance to access student data

                try {
                    admin.loadFromFile(fileName);
                } catch (IOException e) {
                    System.err.println("Error reading from file: " + e.getMessage());
                }

                if (admin.checkStudentExists(studentName)) {
                    Student student = admin.getStudentByName(studentName);
                    student.displayAttendanceSummary();
                } else {
                    System.out.println("Student name not found. Please enter a valid name.");
                }
            } else {
                System.out.println("Invalid user type. Please restart the program.");
            }

            System.out.print("Do you want to continue? (Enter 'yes' or 'no'): ");
            continueInput = scanner.nextLine();
        }

        if (!adminName.isEmpty()) {
            Admin admin = new Admin(adminName);
            admin.students = adminStudents; // Restore the admin's students for the student view
            System.out.println("\nAdmin View:");
            admin.displayAttendanceSummary();

            System.out.println("\nStudent View:");
            System.out.print("Enter Student name: ");
            String studentName = scanner.nextLine();

            Admin adminForStudentView = new Admin(adminName); // Create a temporary admin instance to access student data

            try {
                adminForStudentView.loadFromFile(fileName);
            } catch (IOException e) {
                System.err.println("Error reading from file: " + e.getMessage());
            }

            if (adminForStudentView.checkStudentExists(studentName)) {
                Student student = adminForStudentView.getStudentByName(studentName);
                student.displayAttendanceSummary();
            } else {
                System.out.println("Student name not found. Please enter a valid name.");
            }
        }

        System.out.println("Thank you for using the Attendance Management System.");
        scanner.close();
    }
}