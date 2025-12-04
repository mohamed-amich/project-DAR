package corba;

import java.io.*;

public class CORBAServer {
    public static final int CORBA_PORT = 1100;
    public static final String SERVICE_NAME = "UserService";

    public static void main(String[] args) {
        try {
            System.out.println("==============================================");
            System.out.println("       CORBA User Service Server");
            System.out.println("   (Using SimpleORB - CORBA-like implementation)");
            System.out.println("==============================================");

            SimpleORB orb = new SimpleORB();
            orb.init(CORBA_PORT);
            
            UserServiceServant userService = new UserServiceServant();
            orb.registerServant(SERVICE_NAME, userService);
            
            PrintWriter out = new PrintWriter(new FileWriter("UserService.ref"));
            out.println("localhost:" + CORBA_PORT + ":" + SERVICE_NAME);
            out.close();

            System.out.println("[CORBA Server] Server started successfully!");
            System.out.println("[CORBA Server] Service Name: " + SERVICE_NAME);
            System.out.println("[CORBA Server] Port: " + CORBA_PORT);
            System.out.println("[CORBA Server] Reference file created: UserService.ref");
            System.out.println("[CORBA Server] Waiting for client connections...");
            System.out.println("==============================================");

            orb.run();

        } catch (Exception e) {
            System.err.println("[CORBA Server] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
