package com.mycompany.library_management_system;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

class Campus implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Catalog catalog;
    private ArrayList<Officer> officers;
    private ArrayList<Student> students;

    public Campus(String name) {
        this.name = name;
        this.catalog = new Catalog();
        this.officers = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    public String getName() { return name; }
    public Catalog getCatalog() { return catalog; }
    public ArrayList<Officer> getOfficers() { return officers; }
    public ArrayList<Student> getStudents() { return students; }
}

class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String author;
    private String category;
    private boolean available;
    private String campus;

    public Book(String id, String title, String author, String category, String campus) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.campus = campus;
        this.available = true;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public String getCampus() { return campus; }
    
    public void setAvailable(boolean available) { this.available = available; }
    public void updateDetails(String title, String author, String category) {
        this.title = title;
        this.author = author;
        this.category = category;
    }

    @Override
    public String toString() {
        return String.format("id=%s, title=%s, author=%s, category=%s - %s",
            id, title, author, category, available ? "Available" : "Borrowed");
    }
}

class Catalog implements Serializable {
    private static final long serialVersionUID = 1L;
    private ArrayList<Book> books = new ArrayList<>();

    public void addBook(Book book) { books.add(book); }
    public boolean removeBook(String id) { return books.removeIf(b -> b.getId().equals(id)); }
    public Book searchBook(String id) { 
        return books.stream()
            .filter(b -> b.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    public ArrayList<Book> getAllBooks() { return books; }
    public void updateBook(String id, String title, String author, String category) {
        Book book = searchBook(id);
        if(book != null) book.updateDetails(title, author, category);
    }
}

abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String id;
    protected String name;
    protected String campus;
    protected ArrayList<String> notifications = new ArrayList<>();

    public User(String id, String name, String campus) {
        this.id = id;
        this.name = name;
        this.campus = campus;
    }

    public void addNotification(String message) {
        notifications.add(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + " - " + message);
    }
    
    public void showNotifications() {
        if(notifications.isEmpty()) {
            JOptionPane.showMessageDialog(null, "===== NOTIFICATIONS ====\nNo new notifications\n========================");
            return;
        }
        StringBuilder sb = new StringBuilder("===== NOTIFICATIONS =====\n");
        notifications.forEach(n -> sb.append(n).append("\n"));
        sb.append("========================");
        JOptionPane.showMessageDialog(null, sb.toString());
    }
}

class Student extends User {
    private static final long serialVersionUID = 1L;
    private HashMap<Book, Date[]> borrowedBooks = new HashMap<>();

    public Student(String id, String name, String campus) {
        super(id, name, campus);
    }

    public void borrowBook(Book book, Date dueDate) {
        if(borrowedBooks.size() < 5 && book != null && book.isAvailable()) {
            borrowedBooks.put(book, new Date[]{new Date(), dueDate});
            book.setAvailable(false);
            addNotification("Borrowed book: " + book.getTitle());
        }
    }

    public void returnBook(Book book) {
        Date[] dates = borrowedBooks.remove(book);
        if(dates != null) {
            book.setAvailable(true);
        }
    }

    public HashMap<Book, Date[]> getBorrowedBooks() { return borrowedBooks; }
}

class Officer extends User {
    private static final long serialVersionUID = 1L;
    public Officer(String id, String name, String campus) {
        super(id, name, campus);
    }

    public void sendNotification(Student student, String message) {
        student.addNotification(message);
    }
}

public class LIBRARY_MANAGEMENT_SYSTEM {
    private static ArrayList<Campus> campuses = new ArrayList<>();
    private static final String ADMIN_PASSWORD = "micky";
    private static final String DATA_FILE = "library_data.dat";

    public static void main(String[] args) {
        loadData();
        if (campuses.isEmpty()) initializeCampuses();
        while(true) {
            Campus selectedCampus = selectCampus();
            if (selectedCampus == null) {
                int confirm = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    saveData();
                    System.exit(0);
                }
            } else {
                mainMenu(selectedCampus);
            }
        }
    }

    private static void initializeCampuses() {
        campuses.add(new Campus("Atse Tewodros Campus"));
        campuses.add(new Campus("GC Campus"));
        campuses.add(new Campus("Maraki Campus"));
        campuses.add(new Campus("Fasil Campus"));
        campuses.add(new Campus("Teda Campus"));
    }

    private static void mainMenu(Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "=================MAIN MENU==================\n" +
                "FROM (" + campus.getName() + "):\n" +
                "1. Admin Login\n2. Officer Login\n3. Student Login\n4. Back\n" +
                "==============================================="
            );
            if (choice == null) {
                int confirm = JOptionPane.showConfirmDialog(null, 
                    "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) System.exit(0);
                else continue;
            }
            switch (choice) {
                case "1": adminLogin(campus); break;
                case "2": officerLogin(campus); break;
                case "3": studentLogin(campus); break;
                case "4": return;
                default: showError("Invalid choice!");
            }
        }
    }

    // ================== ADMIN FUNCTIONS ==================
    private static void adminLogin(Campus campus) {
        String password = JOptionPane.showInputDialog("Enter admin password:");
        if (password == null) return;
        if (!ADMIN_PASSWORD.equals(password)) {
            showError("Invalid password!");
            return;
        }
        adminMenu(campus);
    }

    private static void adminMenu(Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "===== ADMINISTRATOR DASHBOARD =====\n" +
                "Campus: " + campus.getName() + "\n" +
                "1. Manage Books\n2. Manage Officers\n3. Manage Students\n4. Exit\n" +
                "==================================="
            );
            if (choice == null) return;
            switch (choice) {
                case "1": manageBooks(campus); break;
                case "2": manageOfficers(campus); break;
                case "3": manageStudents(campus); break;
                case "4": return;
                default: showError("Invalid choice!");
            }
        }
    }

    private static void manageBooks(Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "===== BOOK MANAGEMENT =====" +
                "\nCampus: " + campus.getName() +
                "\n1. Add Book\n2. Remove Book\n3. Update Book" +
                "\n4. Search Book\n5. Show All Books\n6. Back\n" +
                "==========================="
            );
            if (choice == null) return;
            switch (choice) {
                case "1": addBook(campus); break;
                case "2": removeBook(campus); break;
                case "3": updateBook(campus); break;
                case "4": searchBookAdmin(campus); break;
                case "5": showAllBooks(campus); break;
                case "6": return;
                default: showError("Invalid choice!");
            }
        }
    }

    private static void addBook(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter book ID:");
            if (id == null || id.trim().isEmpty()) {
                showError("Book ID cannot be empty!");
                return;
            }
            id = id.trim();
            
            if (campus.getCatalog().searchBook(id) != null) {
                showError("A book with this ID already exists!");
                return;
            }
            
            String title = JOptionPane.showInputDialog("Enter title:");
            if (title == null || title.trim().isEmpty()) {
                showError("Title cannot be empty!");
                return;
            }
            
            String author = JOptionPane.showInputDialog("Enter author:");
            if (author == null || author.trim().isEmpty()) {
                showError("Author cannot be empty!");
                return;
            }
            
            String category = JOptionPane.showInputDialog("Enter category:");
            if (category == null || category.trim().isEmpty()) {
                showError("Category cannot be empty!");
                return;
            }
            
            campus.getCatalog().addBook(new Book(id, title.trim(), author.trim(), category.trim(), campus.getName()));
            saveData();
            JOptionPane.showMessageDialog(null, "Book added successfully!");
        } catch (Exception e) {
            showError("Error adding book: " + e.getMessage());
        }
    }

    private static void removeBook(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter book ID to remove:");
            if (id == null) return;
            
            Book book = campus.getCatalog().searchBook(id);
            if (book == null) {
                showError("Book not found!");
                return;
            }
            
            campus.getCatalog().removeBook(id);
            saveData();
            JOptionPane.showMessageDialog(null, "Book removed successfully!");
        } catch (Exception e) {
            showError("Error removing book: " + e.getMessage());
        }
    }

    private static void updateBook(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter book ID to update:");
            if (id == null) return;
            
            Book book = campus.getCatalog().searchBook(id);
            if (book == null) {
                showError("Book not found!");
                return;
            }
            
            String title = JOptionPane.showInputDialog("New title:", book.getTitle());
            if (title == null || title.trim().isEmpty()) {
                showError("Title cannot be empty!");
                return;
            }
            
            String author = JOptionPane.showInputDialog("New author:", book.getAuthor());
            if (author == null || author.trim().isEmpty()) {
                showError("Author cannot be empty!");
                return;
            }
            
            String category = JOptionPane.showInputDialog("New category:", book.getCategory());
            if (category == null || category.trim().isEmpty()) {
                showError("Category cannot be empty!");
                return;
            }
            
            campus.getCatalog().updateBook(id, title.trim(), author.trim(), category.trim());
            saveData();
            JOptionPane.showMessageDialog(null, "Book updated successfully!");
        } catch (Exception e) {
            showError("Error updating book: " + e.getMessage());
        }
    }

    private static void searchBookAdmin(Campus campus) {
        try {
            String input = JOptionPane.showInputDialog("Enter book ID, title, or category:");
            if (input == null) return;
            
            List<Book> results = campus.getCatalog().getAllBooks().stream()
                .filter(b -> b.getId().equals(input) || 
                           b.getTitle().equalsIgnoreCase(input) || 
                           b.getCategory().equalsIgnoreCase(input))
                .collect(Collectors.toList());
            
            if (results.isEmpty()) {
                showError("No books found!");
            } else {
                StringBuilder sb = new StringBuilder("===== SEARCH RESULTS =====\n");
                results.forEach(b -> sb.append(b.toString()).append("\n"));
                sb.append("========================");
                JOptionPane.showMessageDialog(null, sb.toString());
            }
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
        }
    }

    private static void showAllBooks(Campus campus) {
        try {
            StringBuilder sb = new StringBuilder("===== ALL BOOKS =====\n");
            campus.getCatalog().getAllBooks().forEach(b -> 
                sb.append(b.toString()).append("\n"));
            sb.append("=====================");
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            showError("Error displaying books: " + e.getMessage());
        }
    }

    private static void manageOfficers(Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "===== OFFICER MANAGEMENT =====" +
                "\nCampus: " + campus.getName() +
                "\n1. Hire Officer\n2. Fire Officer\n3. Search Officer\n4. Show All\n5. Back\n" +
                "=============================="
            );
            if (choice == null) return;
            switch (choice) {
                case "1": hireOfficer(campus); break;
                case "2": fireOfficer(campus); break;
                case "3": searchOfficer(campus); break;
                case "4": showAllOfficers(campus); break;
                case "5": return;
                default: showError("Invalid choice!");
            }
        }
    }

    private static void hireOfficer(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter officer ID:");
            if (id == null || id.trim().isEmpty()) {
                showError("Officer ID cannot be empty!");
                return;
            }
            
            String name = JOptionPane.showInputDialog("Enter officer name:");
            if (name == null || name.trim().isEmpty()) {
                showError("Officer name cannot be empty!");
                return;
            }
            
            campus.getOfficers().add(new Officer(id.trim(), name.trim(), campus.getName()));
            saveData();
            JOptionPane.showMessageDialog(null, "Officer hired successfully!");
        } catch (Exception e) {
            showError("Error hiring officer: " + e.getMessage());
        }
    }

    private static void fireOfficer(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter officer ID to remove:");
            if (id == null) return;
            
            boolean removed = campus.getOfficers().removeIf(o -> o.id.equals(id));
            if (removed) {
                saveData();
                JOptionPane.showMessageDialog(null, "Officer removed successfully!");
            } else {
                showError("Officer not found!");
            }
        } catch (Exception e) {
            showError("Error removing officer: " + e.getMessage());
        }
    }

    private static void searchOfficer(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter officer ID:");
            if (id == null) return;
            
            Officer officer = campus.getOfficers().stream()
                .filter(o -> o.id.equals(id))
                .findFirst()
                .orElse(null);
            
            if (officer == null) {
                showError("Officer not found!");
            } else {
                JOptionPane.showMessageDialog(null, 
                    "===== OFFICER DETAILS =====\n" +
                    "ID: " + officer.id + "\nName: " + officer.name + "\n" +
                    "========================");
            }
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
        }
    }

    private static void showAllOfficers(Campus campus) {
        try {
            StringBuilder sb = new StringBuilder("===== ALL OFFICERS =====\n");
            campus.getOfficers().forEach(o -> sb.append(o.id).append(" - ").append(o.name).append("\n"));
            sb.append("========================");
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            showError("Error displaying officers: " + e.getMessage());
        }
    }

    private static void manageStudents(Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "===== STUDENT MANAGEMENT =====" +
                "\nCampus: " + campus.getName() +
                "\n1. Search Student\n2. Show All Students\n3. Back\n" +
                "=============================="
            );
            if (choice == null) return;
            switch (choice) {
                case "1": searchStudent(campus); break;
                case "2": showAllStudents(campus); break;
                case "3": return;
                default: showError("Invalid choice!");
            }
        }
    }

    private static void searchStudent(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter student ID:");
            if (id == null) return;
            
            Student student = campus.getStudents().stream()
                .filter(s -> s.id.equals(id))
                .findFirst()
                .orElse(null);
            
            if (student == null) {
                showError("Student not found!");
            } else {
                JOptionPane.showMessageDialog(null, 
                    "===== STUDENT DETAILS =====\n" +
                    "ID: " + student.id + "\nName: " + student.name + "\n" +
                    "========================");
            }
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
        }
    }

    private static void showAllStudents(Campus campus) {
        try {
            StringBuilder sb = new StringBuilder("===== ALL STUDENTS =====\n");
            campus.getStudents().forEach(s -> sb.append(s.id).append(" - ").append(s.name).append("\n"));
            sb.append("========================");
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            showError("Error displaying students: " + e.getMessage());
        }
    }

    // ================== OFFICER FUNCTIONS ==================
    private static void officerLogin(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter officer ID:");
            if (id == null) return;
            
            Officer officer = campus.getOfficers().stream()
                .filter(o -> o.id.equals(id))
                .findFirst()
                .orElse(null);
            
            if (officer == null) {
                showError("Invalid credentials!");
            } else {
                officerMenu(officer, campus);
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
        }
    }

    private static void officerMenu(Officer officer, Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "===== OFFICER DASHBOARD =====" +
                "\nCampus: " + campus.getName() +
                "\nLogged in as: " + officer.name +
                "\n1. Manage Books\n2. Search Students\n3. Exit" +
                "\n============================"
            );
            if (choice == null) return;
            switch (choice) {
                case "1": manageBooksOfficer(campus); break;
                case "2": searchStudentOfficer(campus); break;
                case "3": return;
                default: showError("Invalid choice!");
            }
        }
    }

    private static void manageBooksOfficer(Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "===== BOOK MANAGEMENT =====" +
                "\nCampus: " + campus.getName() +
                "\n1. Arrange by Category\n2. Show Borrowed Books" +
                "\n3. Show All Books\n4. Search Books\n5. Update Book\n6. Back" +
                "\n=========================="
            );
            if (choice == null) return;
            switch (choice) {
                case "1": arrangeByCategory(campus); break;
                case "2": showBorrowedBooks(campus); break;
                case "3": showAllBooks(campus); break;
                case "4": searchBookOfficer(campus); break;
                case "5": updateBookOfficer(campus); break;
                case "6": return;
                default: showError("Invalid choice!");
            }
        }
    }

    private static void arrangeByCategory(Campus campus) {
        try {
            List<Book> sorted = campus.getCatalog().getAllBooks().stream()
                .sorted(Comparator.comparing(Book::getCategory))
                .collect(Collectors.toList());
            
            StringBuilder sb = new StringBuilder("===== BOOKS BY CATEGORY =====\n");
            sorted.forEach(b -> sb.append(b.getCategory()).append(" - ").append(b.getTitle()).append("\n"));
            sb.append("==============================");
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            showError("Error arranging books: " + e.getMessage());
        }
    }

    private static void showBorrowedBooks(Campus campus) {
        try {
            List<Book> borrowed = campus.getCatalog().getAllBooks().stream()
                .filter(b -> !b.isAvailable())
                .collect(Collectors.toList());
            
            StringBuilder sb = new StringBuilder("===== BORROWED BOOKS =====\n");
            borrowed.forEach(b -> sb.append(b.getId()).append(" - ").append(b.getTitle()).append("\n"));
            sb.append("=========================");
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            showError("Error showing borrowed books: " + e.getMessage());
        }
    }

    private static void searchBookOfficer(Campus campus) {
        try {
            String input = JOptionPane.showInputDialog("Enter category or book ID:");
            if (input == null) return;
            
            List<Book> results = campus.getCatalog().getAllBooks().stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(input) || b.getId().equals(input))
                .collect(Collectors.toList());
            
            if (results.isEmpty()) {
                showError("No books found!");
            } else {
                StringBuilder sb = new StringBuilder("===== SEARCH RESULTS =====\n");
                results.forEach(b -> sb.append(b.toString()).append("\n"));
                sb.append("========================");
                JOptionPane.showMessageDialog(null, sb.toString());
            }
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
        }
    }

    private static void updateBookOfficer(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter book ID to update:");
            if (id == null) return;
            
            Book book = campus.getCatalog().searchBook(id);
            if (book == null) {
                showError("Book not found!");
                return;
            }
            
            String title = JOptionPane.showInputDialog("New title:", book.getTitle());
            if (title == null || title.trim().isEmpty()) {
                showError("Title cannot be empty!");
                return;
            }
            
            String author = JOptionPane.showInputDialog("New author:", book.getAuthor());
            if (author == null || author.trim().isEmpty()) {
                showError("Author cannot be empty!");
                return;
            }
            
            String category = JOptionPane.showInputDialog("New category:", book.getCategory());
            if (category == null || category.trim().isEmpty()) {
                showError("Category cannot be empty!");
                return;
            }
            
            book.updateDetails(title.trim(), author.trim(), category.trim());
            saveData();
            JOptionPane.showMessageDialog(null, "Book updated successfully!");
        } catch (Exception e) {
            showError("Error updating book: " + e.getMessage());
        }
    }

    private static void searchStudentOfficer(Campus campus) {
        searchStudent(campus);
    }

    // ================== STUDENT FUNCTIONS ==================
    private static void studentLogin(Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter student ID:");
            if (id == null) return;
            
            Student student = campus.getStudents().stream()
                .filter(s -> s.id.equals(id))
                .findFirst()
                .orElse(null);
            
            if (student == null) {
                int choice = JOptionPane.showConfirmDialog(null, 
                    "===== REGISTRATION ====\nStudent not found. Register as new student?\n========================",
                    "Registration", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    String name = JOptionPane.showInputDialog("Enter your name:");
                    if (name == null || name.trim().isEmpty()) {
                        showError("Name cannot be empty!");
                        return;
                    }
                    student = new Student(id, name.trim(), campus.getName());
                    campus.getStudents().add(student);
                    saveData();
                    JOptionPane.showMessageDialog(null, "Registration successful!");
                } else {
                    return;
                }
            }
            studentMenu(student, campus);
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
        }
    }

    private static void studentMenu(Student student, Campus campus) {
        while (true) {
            String choice = JOptionPane.showInputDialog(
                "===== STUDENT DASHBOARD =====" +
                "\nCampus: " + campus.getName() +
                "\nLogged in as: " + student.name +
                "\n1. Borrow Book\n2. Return Book\n3. View Available Books" +
                "\n4. Search Books\n5. Notifications\n6. Exit" +
                "\n============================"
            );
            if (choice == null) return;
            switch (choice) {
                case "1": borrowBook(student, campus); break;
                case "2": returnBook(student, campus); break;
                case "3": viewAvailableBooks(campus); break;
                case "4": searchBooksStudent(campus); break;
                case "5": student.showNotifications(); break;
                case "6": return;
                default: showError("Invalid choice!");
            }
        }
    }

    private static void borrowBook(Student student, Campus campus) {
        try {
            String id = JOptionPane.showInputDialog("Enter book ID:");
            if (id == null) return;
            
            Book book = campus.getCatalog().searchBook(id);
            if (book == null || !book.isAvailable()) {
                showError("Book not available!");
                return;
            }
            
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 14);
            Date dueDate = cal.getTime();
            
            student.borrowBook(book, dueDate);
            JOptionPane.showMessageDialog(null, 
                "===== BORROW CONFIRMATION =====" +
                "\nBook borrowed!\nDue Date: " + 
                new SimpleDateFormat("yyyy-MM-dd").format(dueDate) +
                "\n==============================");
            saveData();
        } catch (Exception e) {
            showError("Error borrowing book: " + e.getMessage());
        }
    }

    private static void returnBook(Student student, Campus campus) {
        try {
            if (student.getBorrowedBooks().isEmpty()) {
                showError("No books to return!");
                return;
            }
            
            Book[] books = student.getBorrowedBooks().keySet().toArray(new Book[0]);
            Book selected = (Book) JOptionPane.showInputDialog(
                null, "Select book to return:", "Return Book",
                JOptionPane.QUESTION_MESSAGE, null, books, books[0]
            );
            
            if (selected != null) {
                student.returnBook(selected);
                JOptionPane.showMessageDialog(null, 
                    "===== RETURN CONFIRMATION =====" +
                    "\nBook returned successfully!" +
                    "\n==============================");
                saveData();
            }
        } catch (Exception e) {
            showError("Error returning book: " + e.getMessage());
        }
    }

    private static void viewAvailableBooks(Campus campus) {
        try {
            StringBuilder sb = new StringBuilder("===== AVAILABLE BOOKS =====\n");
            campus.getCatalog().getAllBooks().stream()
                .filter(Book::isAvailable)
                .forEach(b -> sb.append(b.toString()).append("\n"));
            sb.append("==========================");
            JOptionPane.showMessageDialog(null, sb.toString());
        } catch (Exception e) {
            showError("Error displaying books: " + e.getMessage());
        }
    }

    private static void searchBooksStudent(Campus campus) {
        try {
            String input = JOptionPane.showInputDialog("Enter category or book ID:");
            if (input == null) return;
            
            List<Book> results = campus.getCatalog().getAllBooks().stream()
                .filter(b -> b.getCategory().equalsIgnoreCase(input) || b.getId().equals(input))
                .collect(Collectors.toList());
            
            if (results.isEmpty()) {
                showError("No books found!");
            } else {
                StringBuilder sb = new StringBuilder("===== SEARCH RESULTS =====\n");
                results.forEach(b -> sb.append(b.toString()).append("\n"));
                sb.append("========================");
                JOptionPane.showMessageDialog(null, sb.toString());
            }
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
        }
    }

    // ================== UTILITY FUNCTIONS ==================
    private static Campus selectCampus() {
        while (true) {
            try {
                String input = JOptionPane.showInputDialog(
                    "UNIVERSITY OF GONDAR LIBRARY MANAGEMENT SYSTEM " +
                    "\n1. Atse Tewodros Campus\n2. GC Campus" +
                    "\n3. Maraki Campus\n4. Fasil Campus\n5. Teda Campus\n6. Exit" +
                    "\n==========================================="
                );
                if (input == null) {
                    int confirm = JOptionPane.showConfirmDialog(null, 
                        "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) return null;
                    else continue;
                }
                int choice = Integer.parseInt(input);
                if (choice == 6) return null;
                if (choice < 1 || choice > 5) {
                    showError("Please enter a number between 1 and 6");
                    continue;
                }
                return campuses.get(choice - 1);
            } catch (NumberFormatException e) {
                showError("Invalid input. Please enter a number.");
            } catch (Exception e) {
                showError("Error selecting campus: " + e.getMessage());
                return null;
            }
        }
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "===== ERROR =====", JOptionPane.ERROR_MESSAGE);
    }

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(campuses);
        } catch (IOException e) {
            showError("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            campuses = (ArrayList<Campus>) ois.readObject();
        } catch (FileNotFoundException e) {
            // First run, no data file
        } catch (IOException | ClassNotFoundException e) {
            showError("Error loading data: " + e.getMessage());
        }
    }
}