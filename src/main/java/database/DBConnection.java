package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接管理类（单例模式）
 * 确保全局只有一个连接池实例，避免重复建立连接
 */
public class DBConnection {
    private static DBConnection instance;
    private final HikariDataSource dataSource;

    // 私有构造函数，防止外部实例化
    private DBConnection() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/pokpet?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        config.setUsername("trainer");
        config.setPassword("Sysu@edu123");
        config.setMaximumPoolSize(10); // 最大连接数
        config.setMinimumIdle(2);      // 最小空闲连接
        config.setIdleTimeout(300000); // 空闲超时（5分钟）
        dataSource = new HikariDataSource(config);
    }

    // 获取单例实例
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // 获取数据库连接
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // 关闭连接池（程序退出时调用）
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}