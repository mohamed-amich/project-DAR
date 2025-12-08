package corba;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceServant {
    private final Map<String, InternalUser> users;

    private static class InternalUser {
        volatile String id;
        volatile String name;
        volatile String email;
        volatile String password;
        volatile String role;
        volatile boolean active;

        InternalUser(String id, String name, String email, String password, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.active = true;
        }
        
        synchronized UserData toUserData() {
            return new UserData(id, name, email, role, active);
        }
    }

    public UserServiceServant() {
        this.users = new ConcurrentHashMap<>();
        initializeSampleUsers();
    }

    private void initializeSampleUsers() {
        users.put("admin", new InternalUser("admin", "Administrator", "admin@library.com", "admin123", "admin"));
        users.put("user1", new InternalUser("user1", "Amich", "amich@example.com", "pass123", "user"));
        users.put("user2", new InternalUser("user2", "dali", "dali@example.com", "pass456", "user"));
        users.put("librarian", new InternalUser("librarian", "ahmed", "staff@library.com", "lib123", "librarian"));
    }

    public Boolean registerUser(String id, String name, String email, String password) {
        InternalUser newUser = new InternalUser(id, name, email, password, "user");
        InternalUser existing = users.putIfAbsent(id, newUser);
        if (existing != null) {
            System.out.println("[CORBA Server] User already exists: " + id);
            return false;
        }
        System.out.println("[CORBA Server] Registered new user: " + id);
        return true;
    }

    public Boolean authenticate(String id, String password) {
        InternalUser user = users.get(id);
        if (user == null) {
            System.out.println("[CORBA Server] Authentication failed - user not found: " + id);
            return false;
        }
        synchronized (user) {
            if (!user.active) {
                System.out.println("[CORBA Server] Authentication failed - user inactive: " + id);
                return false;
            }
            boolean success = user.password.equals(password);
            System.out.println("[CORBA Server] Authentication " + (success ? "successful" : "failed") + " for user: " + id);
            return success;
        }
    }

    public UserData getUserInfo(String id) {
        System.out.println("[CORBA Server] Getting user info for: " + id);
        InternalUser user = users.get(id);
        if (user == null) {
            return new UserData("", "", "", "", false);
        }
        return user.toUserData();
    }

    public UserData[] getAllUsers() {
        System.out.println("[CORBA Server] Getting all users");
        List<UserData> result = new ArrayList<>();
        for (InternalUser user : users.values()) {
            result.add(user.toUserData());
        }
        return result.toArray(new UserData[0]);
    }

    public Boolean updateUser(String id, String name, String email) {
        InternalUser user = users.get(id);
        if (user == null) {
            System.out.println("[CORBA Server] User not found: " + id);
            return false;
        }
        synchronized (user) {
            user.name = name;
            user.email = email;
        }
        System.out.println("[CORBA Server] Updated user: " + id);
        return true;
    }

    public Boolean deleteUser(String id) {
        InternalUser user = users.get(id);
        if (user == null) {
            System.out.println("[CORBA Server] User not found: " + id);
            return false;
        }
        synchronized (user) {
            user.active = false;
        }
        System.out.println("[CORBA Server] Deactivated user: " + id);
        return true;
    }

    public Boolean changePassword(String id, String oldPassword, String newPassword) {
        InternalUser user = users.get(id);
        if (user == null) {
            System.out.println("[CORBA Server] User not found: " + id);
            return false;
        }
        synchronized (user) {
            if (!user.password.equals(oldPassword)) {
                System.out.println("[CORBA Server] Password change failed - wrong old password for: " + id);
                return false;
            }
            user.password = newPassword;
        }
        System.out.println("[CORBA Server] Password changed for user: " + id);
        return true;
    }

    public Boolean isAdmin(String id) {
        InternalUser user = users.get(id);
        if (user == null) {
            return false;
        }
        synchronized (user) {
            return "admin".equals(user.role);
        }
    }
}
