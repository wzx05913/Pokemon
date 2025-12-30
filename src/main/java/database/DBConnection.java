package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;


//数据库连接管理类（单例模式）

public class DBConnection {
    private static DBConnection instance;
    private final HikariDataSource dataSource;

    private DBConnection() {
        HikariConfig config = new HikariConfig();
        //添加字符集设置
        config.setJdbcUrl("jdbc:mysql://localhost:3306/pokpet?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8&useUnicode=true");
        config.setUsername("trainer");
        config.setPassword("Sysu@edu123");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        dataSource = new HikariDataSource(config);
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}