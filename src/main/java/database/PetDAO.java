package database;

import entity.Pet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 宠物表数据访问对象
 */
public class PetDAO {
    /**
     * 新增宠物
     */
    public void createPet(Pet pet) throws SQLException {
        String sql = "INSERT INTO pet (UserID, Name, Type, Level, Attack, Clean, Experience, IsAlive) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pet.getUserId());
            stmt.setString(2, pet.getName());
            stmt.setString(3, pet.getType());
            stmt.setInt(4, pet.getLevel());
            stmt.setInt(5, pet.getAttack());
            stmt.setObject(6, pet.getClean()); // 允许null
            stmt.setObject(7, pet.getExperience()); // 允许null
            stmt.setObject(8, pet.getAlive() ? 1 : 0); // tinyint对应1/0

            stmt.executeUpdate();
        }
    }

    /**
     * 查询用户的所有宠物
     */
    public List<Pet> getPetsByUserId(int userId) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM pet WHERE UserID = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pet pet = new Pet();
                    pet.setPetId(rs.getInt("PetID"));
                    pet.setUserId(rs.getInt("UserID"));
                    pet.setName(rs.getString("Name"));
                    pet.setType(rs.getString("Type"));
                    pet.setLevel(rs.getInt("Level"));
                    pet.setAttack(rs.getInt("Attack"));
                    pet.setClean(rs.getInt("Clean"));
                    pet.setExperience(rs.getInt("Experience"));
                    pet.setAlive(rs.getBoolean("IsAlive"));
                    pets.add(pet);
                }
            }
        }
        return pets;
    }

    /**
     * 更新宠物信息（如升级、状态变化等）
     */
    public void updatePet(Pet pet) throws SQLException {
        String sql = "UPDATE pet SET Name=?, Type=?, Level=?, Attack=?, Clean=?, Experience=?, IsAlive=? " +
                     "WHERE PetID=?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getType());
            stmt.setInt(3, pet.getLevel());
            stmt.setInt(4, pet.getAttack());
            stmt.setObject(5, pet.getClean());
            stmt.setObject(6, pet.getExperience());
            stmt.setObject(7, pet.getAlive() ? 1 : 0);
            stmt.setInt(8, pet.getPetId());

            stmt.executeUpdate();
        }
    }
}