package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static final int RMI_PORT = 1099;
    public static final String SERVICE_NAME = "BookService";

    public static void main(String[] args) {
        try {
            System.out.println("==============================================");
            System.out.println("         RMI Book Service Server");
            System.out.println("==============================================");
            
            BookServiceImpl bookService = new BookServiceImpl();
            
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind(SERVICE_NAME, bookService);
            
            System.out.println("[RMI Server] Server started successfully!");
            System.out.println("[RMI Server] Service Name: " + SERVICE_NAME);
            System.out.println("[RMI Server] Port: " + RMI_PORT);
            System.out.println("[RMI Server] Waiting for client connections...");
            System.out.println("==============================================");
            
            synchronized (RMIServer.class) {
                RMIServer.class.wait();
            }
            
        } catch (Exception e) {
            System.err.println("[RMI Server] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
