package corba;

import java.io.Serializable;

public class UserData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String id;
    public String name;
    public String email;
    public String role;
    public boolean active;

    public UserData() {
        this.id = "";
        this.name = "";
        this.email = "";
        this.role = "";
        this.active = false;
    }

    public UserData(String id, String name, String email, String role, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
    }
    
    @Override
    public String toString() {
        return String.format("User[id=%s, name=%s, email=%s, role=%s, active=%s]",
                id, name, email, role, active);
    }
}
