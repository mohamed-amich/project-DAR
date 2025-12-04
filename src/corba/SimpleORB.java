package corba;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class SimpleORB {
    private ServerSocket serverSocket;
    private final Map<String, Object> servants = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running = false;
    private int port;
    
    public void init(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.running = true;
    }
    
    public int getPort() {
        return port;
    }
    
    public void registerServant(String name, Object servant) {
        servants.put(name, servant);
    }
    
    public void run() {
        System.out.println("[SimpleORB] ORB running on port " + port);
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("[SimpleORB] Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
            String servantName = (String) in.readObject();
            String methodName = (String) in.readObject();
            Object[] args = (Object[]) in.readObject();
            
            Object servant = servants.get(servantName);
            if (servant == null) {
                out.writeObject(new Exception("Servant not found: " + servantName));
                return;
            }
            
            Object result = invokeMethod(servant, methodName, args);
            out.writeObject(result);
            out.flush();
            
        } catch (Exception e) {
            System.err.println("[SimpleORB] Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
            }
        }
    }
    
    private Object invokeMethod(Object servant, String methodName, Object[] args) {
        try {
            if (args == null) {
                args = new Object[0];
            }
            
            for (java.lang.reflect.Method method : servant.getClass().getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    boolean compatible = true;
                    
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] == null) {
                            if (paramTypes[i].isPrimitive()) {
                                compatible = false;
                                break;
                            }
                        } else if (!isAssignableFrom(paramTypes[i], args[i].getClass())) {
                            compatible = false;
                            break;
                        }
                    }
                    
                    if (compatible) {
                        return method.invoke(servant, args);
                    }
                }
            }
            return new Exception("Method not found or incompatible parameters: " + methodName);
        } catch (Exception e) {
            return e;
        }
    }
    
    private boolean isAssignableFrom(Class<?> target, Class<?> source) {
        if (target.isAssignableFrom(source)) {
            return true;
        }
        if (target.isPrimitive()) {
            if (target == boolean.class && source == Boolean.class) return true;
            if (target == int.class && source == Integer.class) return true;
            if (target == long.class && source == Long.class) return true;
            if (target == double.class && source == Double.class) return true;
            if (target == float.class && source == Float.class) return true;
            if (target == byte.class && source == Byte.class) return true;
            if (target == short.class && source == Short.class) return true;
            if (target == char.class && source == Character.class) return true;
        }
        return false;
    }
    
    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
        }
    }
    
    public static class Stub {
        private String host;
        private int port;
        private String servantName;
        
        public Stub(String host, int port, String servantName) {
            this.host = host;
            this.port = port;
            this.servantName = servantName;
        }
        
        @SuppressWarnings("unchecked")
        public <T> T invoke(String methodName, Object... args) throws Exception {
            try (Socket socket = new Socket(host, port)) {
                socket.setSoTimeout(30000);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                
                out.writeObject(servantName);
                out.writeObject(methodName);
                out.writeObject(args != null ? args : new Object[0]);
                out.flush();
                
                Object result = in.readObject();
                if (result instanceof Exception) {
                    throw (Exception) result;
                }
                return (T) result;
            }
        }
    }
}
