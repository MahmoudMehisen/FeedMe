package foodOreder.feedme.Model;

public class User {
    private String name,password,phone,isStaff, SecureCode;

    public String getSecureCode() {
        return SecureCode;
    }

    public void setSecureCode(String secureCode) {
        SecureCode = secureCode;
    }

    public User(String name, String password, String SecureCode) {
        this.name = name;
        this.password = password;
        this.isStaff= "false";
        this.SecureCode = SecureCode;
    }

    public User() {
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getIsStaff() {
        return isStaff;
    }
}
