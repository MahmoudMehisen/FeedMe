package foodOreder.feedme.Model;

public class User {
    private String name,password,phone,isStaff, SecureCode,homeLat,homeLng;
    private double balance;

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
        this.homeLat = "0";
        this.homeLng = "0";
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getHomeLat() {
        return homeLat;
    }

    public void setHomeLat(String homeLat) {
        this.homeLat = homeLat;
    }

    public String getHomeLng() {
        return homeLng;
    }

    public void setHomeLng(String homeLng) {
        this.homeLng = homeLng;
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
