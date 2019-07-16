package foodOreder.feedme.Model;

public class User {
    private String Password;
    private String Name;
    private String Phone;

    public User() {
    }


    public User(String Password, String Name, String Phone) {
        this.Password = Password;
        this.Name = Name;
        this.Phone = Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPhone() {
        return Phone;
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
