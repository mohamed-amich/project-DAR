package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import common.Book;

public class BookServiceImpl extends UnicastRemoteObject implements BookService {
    private static final long serialVersionUID = 1L;
    private Map<String, Book> books;
    private Map<String, String> borrowedBooks;

    public BookServiceImpl() throws RemoteException {
        super();
        books = new HashMap<>();
        borrowedBooks = new HashMap<>();
        initializeSampleBooks();
    }

    private void initializeSampleBooks() {
        books.put("978-0-13-468599-1", new Book("978-0-13-468599-1", "Distributed Systems: Concepts and Design", "George Coulouris", 2011));
        books.put("978-0-13-235088-4", new Book("978-0-13-235088-4", "Computer Networking: A Top-Down Approach", "James Kurose", 2016));
        books.put("978-0-59-651798-8", new Book("978-0-59-651798-8", "Head First Design Patterns", "Eric Freeman", 2004));
        books.put("978-0-13-468747-6", new Book("978-0-13-468747-6", "Java: The Complete Reference", "Herbert Schildt", 2018));
        books.put("978-1-49-195016-0", new Book("978-1-49-195016-0", "Building Microservices", "Sam Newman", 2021));
    }

    @Override
    public synchronized boolean addBook(Book book) throws RemoteException {
        if (books.containsKey(book.getIsbn())) {
            System.out.println("[RMI Server] Book with ISBN " + book.getIsbn() + " already exists");
            return false;
        }
        books.put(book.getIsbn(), book);
        System.out.println("[RMI Server] Added book: " + book.getTitle());
        return true;
    }

    @Override
    public Book getBook(String isbn) throws RemoteException {
        System.out.println("[RMI Server] Getting book with ISBN: " + isbn);
        return books.get(isbn);
    }

    @Override
    public List<Book> searchByTitle(String title) throws RemoteException {
        System.out.println("[RMI Server] Searching books by title: " + title);
        return books.values().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> searchByAuthor(String author) throws RemoteException {
        System.out.println("[RMI Server] Searching books by author: " + author);
        return books.values().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> getAllBooks() throws RemoteException {
        System.out.println("[RMI Server] Getting all books");
        return new ArrayList<>(books.values());
    }

    @Override
    public synchronized boolean removeBook(String isbn) throws RemoteException {
        if (books.containsKey(isbn)) {
            books.remove(isbn);
            borrowedBooks.remove(isbn);
            System.out.println("[RMI Server] Removed book with ISBN: " + isbn);
            return true;
        }
        System.out.println("[RMI Server] Book with ISBN " + isbn + " not found");
        return false;
    }

    @Override
    public synchronized boolean borrowBook(String isbn, String userId) throws RemoteException {
        Book book = books.get(isbn);
        if (book == null) {
            System.out.println("[RMI Server] Book not found: " + isbn);
            return false;
        }
        if (!book.isAvailable()) {
            System.out.println("[RMI Server] Book not available: " + isbn);
            return false;
        }
        book.setAvailable(false);
        borrowedBooks.put(isbn, userId);
        System.out.println("[RMI Server] Book borrowed by user " + userId + ": " + book.getTitle());
        return true;
    }

    @Override
    public synchronized boolean returnBook(String isbn) throws RemoteException {
        Book book = books.get(isbn);
        if (book == null) {
            System.out.println("[RMI Server] Book not found: " + isbn);
            return false;
        }
        if (book.isAvailable()) {
            System.out.println("[RMI Server] Book was not borrowed: " + isbn);
            return false;
        }
        book.setAvailable(true);
        borrowedBooks.remove(isbn);
        System.out.println("[RMI Server] Book returned: " + book.getTitle());
        return true;
    }
}
