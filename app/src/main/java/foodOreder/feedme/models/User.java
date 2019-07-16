package foodOreder.feedme.models;

public class User {
    private String Password;
    private String Name;

    public User() {
    }


    public User(String Password, String Name) {
        this.Password = Password;
        this.Name = Name;
    }

    public String getPassword() {
        return Password;
    }

    public String getName() {
        return Name;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public void setName(String Name) {
        Name = Name;
    }
}
