package entity;


public class User {
    private int userId; // 对应UserID（自增主键）

    public User() {}

    public User(int userId) {
        this.userId = userId;
    }

    // Getter和Setter
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}