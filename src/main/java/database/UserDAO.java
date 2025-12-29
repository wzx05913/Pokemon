package database;

import entity.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public User createUser() throws SQLException {
        String sql = "INSERT INTO users () VALUES ()";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new User(rs.getInt(1));
                }
            }
        }
        throw new SQLException("创建用户失败");
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT UserID FROM users WHERE UserID = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("UserID"));
                }
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID FROM users ORDER BY UserID";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(rs.getInt("UserID")));
            }
        }
        return users;
    }

    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE UserID = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public void ensureUserExistsWithId(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("ensureUserExistsWithId: 非法的 userId=" + userId);
        }
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM users WHERE UserID = ?")) {
                check.setInt(1, userId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        return; // 已存在
                    }
                }
            }
            try (PreparedStatement insert = conn.prepareStatement("INSERT INTO users (UserID) VALUES (?)")) {
                insert.setInt(1, userId);
                insert.executeUpdate();
            }
        }
    }
}