package foodOreder.feedme;

public class User {
    private String Password;
    private String Name;

    public User(){}


    public User(String Password, String name) {
        this.Password = Password;
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public String getName() {
        return Name;
    }

    public void setPassword(String password) {
        this.Password = password;
    }

    public void setName(String name) {
        Name = name;
    }
}
