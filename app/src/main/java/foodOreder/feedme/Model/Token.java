package foodOreder.feedme.Model;

public class Token {
    private String token;
    private boolean isServerToken;

    public Token(String token, boolean isServerToken) {
        this.token = token;
        this.isServerToken = isServerToken;
    }

    public Token() {
    }

    public String getToken() {
        return token;
    }

    public boolean isServerToken() {
        return isServerToken;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setServerToken(boolean serverToken) {
        isServerToken = serverToken;
    }
}
