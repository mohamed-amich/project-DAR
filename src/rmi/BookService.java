package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import common.Book;

public interface BookService extends Remote {
    boolean addBook(Book book) throws RemoteException;
    Book getBook(String isbn) throws RemoteException;
    List<Book> searchByTitle(String title) throws RemoteException;
    List<Book> searchByAuthor(String author) throws RemoteException;
    List<Book> getAllBooks() throws RemoteException;
    boolean removeBook(String isbn) throws RemoteException;
    boolean borrowBook(String isbn, String userId) throws RemoteException;
    boolean returnBook(String isbn) throws RemoteException;
}
