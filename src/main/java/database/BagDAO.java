package database;

import entity.Bag;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 背包表数据访问对象
 */
public class BagDAO {
    /**
     * 为用户创建背包
     */
    public void createBag(Bag bag) throws SQLException {
        String sql = "INSERT INTO bag (UserID, EggCount, RiceCount, SoapCount, Coins) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bag.getUserId());
            stmt.setInt(2, bag.getEggCount());
            stmt.setInt(3, bag.getRiceCount());
            stmt.setInt(4, bag.getSoapCount());
            stmt.setInt(5, bag.getCoins());

            stmt.executeUpdate();
        }
    }

    /**
     * 查询用户的背包
     */
    public Bag getBagByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM bag WHERE UserID = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Bag bag = new Bag();
                    bag.setBagId(rs.getInt("BagID"));
                    bag.setUserId(rs.getInt("UserID"));
                    bag.setEggCount(rs.getInt("EggCount"));
                    bag.setRiceCount(rs.getInt("RiceCount"));
                    bag.setSoapCount(rs.getInt("SoapCount"));
                    bag.setCoins(rs.getInt("Coins"));
                    return bag;
                }
            }
        }
        return null;
    }

    /**
     * 更新背包物品数量
     */
    public void updateBag(Bag bag) throws SQLException {
        String sql = "UPDATE bag SET EggCount=?, RiceCount=?, SoapCount=?, Coins=? WHERE BagID=?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bag.getEggCount());
            stmt.setInt(2, bag.getRiceCount());
            stmt.setInt(3, bag.getSoapCount());
            stmt.setInt(4, bag.getCoins());
            stmt.setInt(5, bag.getBagId());

            stmt.executeUpdate();
        }
    }
}