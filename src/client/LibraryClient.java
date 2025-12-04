package client;

import rmi.BookService;
import common.Book;
import corba.SimpleORB;
import corba.UserData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class LibraryClient {
    private BookService bookService;
    private SimpleORB.Stub userServiceStub;
    private String currentUser = null;
    private Scanner scanner;

    public LibraryClient() {
        scanner = new Scanner(System.in);
    }

    public boolean connectToRMI() {
        try {
            System.out.println("[Client] Connecting to RMI Book Service...");
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            bookService = (BookService) registry.lookup("BookService");
            System.out.println("[Client] Connected to RMI Book Service successfully!");
            return true;
        } catch (Exception e) {
            System.err.println("[Client] Failed to connect to RMI: " + e.getMessage());
            return false;
        }
    }

    public boolean connectToCORBA() {
        try {
            System.out.println("[Client] Connecting to CORBA User Service...");
            
            java.io.File refFile = new java.io.File("UserService.ref");
            if (!refFile.exists()) {
                System.err.println("[Client] UserService.ref not found. Make sure CORBA server is running.");
                return false;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(refFile));
            String ref = reader.readLine();
            reader.close();
            
            if (ref == null || ref.trim().isEmpty()) {
                System.err.println("[Client] UserService.ref is empty or invalid.");
                return false;
            }
            
            String[] parts = ref.split(":");
            if (parts.length < 3) {
                System.err.println("[Client] UserService.ref has invalid format. Expected host:port:serviceName");
                return false;
            }
            
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            String serviceName = parts[2];
            
            userServiceStub = new SimpleORB.Stub(host, port, serviceName);
            
            UserData testUser = userServiceStub.invoke("getUserInfo", "admin");
            if (testUser != null && !testUser.id.isEmpty()) {
                System.out.println("[Client] Connected to CORBA User Service successfully!");
                return true;
            }
            System.err.println("[Client] CORBA connection test failed - could not retrieve user data.");
            return false;
        } catch (java.net.ConnectException e) {
            System.err.println("[Client] CORBA server not responding. Make sure it's running on the specified port.");
            return false;
        } catch (Exception e) {
            System.err.println("[Client] Failed to connect to CORBA: " + e.getMessage());
            return false;
        }
    }

    public void showMainMenu() {
        while (true) {
            System.out.println("\n==============================================");
            System.out.println("    Distributed Library Management System");
            System.out.println("         (CORBA + RMI Implementation)");
            System.out.println("==============================================");
            
            if (currentUser != null) {
                System.out.println("Logged in as: " + currentUser);
            }
            
            System.out.println("\n--- User Management (CORBA) ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. View User Info");
            System.out.println("4. List All Users");
            
            System.out.println("\n--- Book Management (RMI) ---");
            System.out.println("5. List All Books");
            System.out.println("6. Search Book by Title");
            System.out.println("7. Search Book by Author");
            System.out.println("8. Add New Book");
            System.out.println("9. Borrow Book");
            System.out.println("10. Return Book");
            
            System.out.println("\n--- Other ---");
            System.out.println("11. Logout");
            System.out.println("0. Exit");
            
            System.out.print("\nEnter your choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                processChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void processChoice(int choice) {
        try {
            switch (choice) {
                case 0:
                    System.out.println("Thank you for using the Library System. Goodbye!");
                    System.exit(0);
                    break;
                case 1:
                    login();
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    viewUserInfo();
                    break;
                case 4:
                    listAllUsers();
                    break;
                case 5:
                    listAllBooks();
                    break;
                case 6:
                    searchByTitle();
                    break;
                case 7:
                    searchByAuthor();
                    break;
                case 8:
                    addBook();
                    break;
                case 9:
                    borrowBook();
                    break;
                case 10:
                    returnBook();
                    break;
                case 11:
                    logout();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void login() throws Exception {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.println("\n[CORBA Call] Authenticating user...");
        Boolean result = userServiceStub.invoke("authenticate", userId, password);
        if (result != null && result) {
            currentUser = userId;
            System.out.println("Login successful! Welcome, " + userId);
        } else {
            System.out.println("Login failed. Invalid credentials.");
        }
    }

    private void register() throws Exception {
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine().trim();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.println("\n[CORBA Call] Registering new user...");
        Boolean result = userServiceStub.invoke("registerUser", userId, name, email, password);
        if (result != null && result) {
            System.out.println("Registration successful! You can now login.");
        } else {
            System.out.println("Registration failed. User ID may already exist.");
        }
    }

    private void viewUserInfo() throws Exception {
        System.out.print("Enter User ID to view: ");
        String userId = scanner.nextLine().trim();

        System.out.println("\n[CORBA Call] Fetching user information...");
        UserData info = userServiceStub.invoke("getUserInfo", userId);
        
        if (info == null || info.id.isEmpty()) {
            System.out.println("User not found.");
        } else {
            System.out.println("\n--- User Information ---");
            System.out.println("ID: " + info.id);
            System.out.println("Name: " + info.name);
            System.out.println("Email: " + info.email);
            System.out.println("Role: " + info.role);
            System.out.println("Active: " + (info.active ? "Yes" : "No"));
        }
    }

    private void listAllUsers() throws Exception {
        System.out.println("\n[CORBA Call] Fetching all users...");
        UserData[] users = userServiceStub.invoke("getAllUsers");
        
        System.out.println("\n--- All Users (" + users.length + " total) ---");
        System.out.printf("%-12s %-20s %-25s %-10s %-8s%n", "ID", "Name", "Email", "Role", "Active");
        System.out.println("-".repeat(80));
        
        for (UserData user : users) {
            System.out.printf("%-12s %-20s %-25s %-10s %-8s%n",
                    user.id, user.name, user.email, user.role, user.active ? "Yes" : "No");
        }
    }

    private void listAllBooks() throws Exception {
        System.out.println("\n[RMI Call] Fetching all books...");
        List<Book> books = bookService.getAllBooks();
        
        System.out.println("\n--- All Books (" + books.size() + " total) ---");
        for (Book book : books) {
            System.out.println(book);
        }
    }

    private void searchByTitle() throws Exception {
        System.out.print("Enter title to search: ");
        String title = scanner.nextLine().trim();

        System.out.println("\n[RMI Call] Searching books by title...");
        List<Book> books = bookService.searchByTitle(title);
        
        if (books.isEmpty()) {
            System.out.println("No books found matching '" + title + "'");
        } else {
            System.out.println("\n--- Search Results (" + books.size() + " found) ---");
            for (Book book : books) {
                System.out.println(book);
            }
        }
    }

    private void searchByAuthor() throws Exception {
        System.out.print("Enter author name to search: ");
        String author = scanner.nextLine().trim();

        System.out.println("\n[RMI Call] Searching books by author...");
        List<Book> books = bookService.searchByAuthor(author);
        
        if (books.isEmpty()) {
            System.out.println("No books found by '" + author + "'");
        } else {
            System.out.println("\n--- Search Results (" + books.size() + " found) ---");
            for (Book book : books) {
                System.out.println(book);
            }
        }
    }

    private void addBook() throws Exception {
        if (currentUser == null) {
            System.out.println("Please login first to add books.");
            return;
        }

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Enter Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Enter Year: ");
        int year = Integer.parseInt(scanner.nextLine().trim());

        Book book = new Book(isbn, title, author, year);
        
        System.out.println("\n[RMI Call] Adding new book...");
        if (bookService.addBook(book)) {
            System.out.println("Book added successfully!");
        } else {
            System.out.println("Failed to add book. ISBN may already exist.");
        }
    }

    private void borrowBook() throws Exception {
        if (currentUser == null) {
            System.out.println("Please login first to borrow books.");
            return;
        }

        System.out.print("Enter ISBN of book to borrow: ");
        String isbn = scanner.nextLine().trim();

        System.out.println("\n[RMI Call] Borrowing book...");
        if (bookService.borrowBook(isbn, currentUser)) {
            System.out.println("Book borrowed successfully!");
        } else {
            System.out.println("Failed to borrow book. Book may not exist or is not available.");
        }
    }

    private void returnBook() throws Exception {
        System.out.print("Enter ISBN of book to return: ");
        String isbn = scanner.nextLine().trim();

        System.out.println("\n[RMI Call] Returning book...");
        if (bookService.returnBook(isbn)) {
            System.out.println("Book returned successfully!");
        } else {
            System.out.println("Failed to return book. Book may not exist or was not borrowed.");
        }
    }

    private void logout() {
        if (currentUser != null) {
            System.out.println("Goodbye, " + currentUser + "!");
            currentUser = null;
        } else {
            System.out.println("You are not logged in.");
        }
    }

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("    Starting Library Client Application");
        System.out.println("==============================================");
        
        LibraryClient client = new LibraryClient();
        
        boolean rmiConnected = client.connectToRMI();
        boolean corbaConnected = client.connectToCORBA();
        
        if (!rmiConnected || !corbaConnected) {
            System.err.println("\nWarning: Not all services are connected.");
            System.err.println("Make sure both RMI and CORBA servers are running.");
            System.err.println("Press Enter to continue anyway or Ctrl+C to exit...");
            new Scanner(System.in).nextLine();
        }
        
        client.showMainMenu();
    }
}
