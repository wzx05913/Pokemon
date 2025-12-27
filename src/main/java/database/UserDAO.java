package database;

import entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户表数据访问对象
 */
public class UserDAO {
    /**
     * 创建新用户（对应users表插入）
     * @return 新用户实体（包含自动生成的UserID）
     */
    public User createUser() throws SQLException {
        String sql = "INSERT INTO users () VALUES ()"; // users表只有自增ID，无需插入其他字段
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.executeUpdate();

            // 获取自动生成的UserID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return new User(rs.getInt(1));
                }
            }
        }
        throw new SQLException("创建用户失败");
    }

    /**
     * 根据ID查询用户
     * @param userId 用户ID
     * @return 用户实体（不存在则返回null）
     */
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

    /**
     * 删除用户
     */
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE UserID = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}