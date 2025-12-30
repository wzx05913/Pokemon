package database;

import entity.Pet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PetDAO {
    public void deletePetsByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM pet WHERE UserID = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    public void deletePet(int petId) throws SQLException {
        String sql = "DELETE FROM pet WHERE PetID = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, petId);
            stmt.executeUpdate();
        }
    }
    public void createPet(Pet pet) throws SQLException {
        String sql = "INSERT INTO pet (UserID, Type, Level, Attack, Clean, Experience, IsAlive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 确保Type不为空
            String petType = pet.getType();

            stmt.setInt(1, pet.getUserId());
            stmt.setString(2, petType);  // 使用处理后的Type
            stmt.setInt(3, pet.getLevel());
            stmt.setInt(4, pet.getAttack());
            stmt.setObject(5, pet.getClean());
            stmt.setObject(6, pet.getExperience());
            stmt.setObject(7, pet.getAlive() ? 1 : 0);

            stmt.executeUpdate();
        }
    }

    public List<Pet> getPetsByUserId(int userId) throws SQLException {
        List<Pet> pets = new ArrayList<>();
        //移除了Name字段
        String sql = "SELECT * FROM pet WHERE UserID = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pet pet = new Pet();
                    pet.setPetId(rs.getInt("PetID"));
                    pet.setUserId(rs.getInt("UserID"));
                    pet.setType(rs.getString("Type"));  // Type就是名称
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

    public void updatePet(Pet pet) throws SQLException {
        // 移除了Name字段
        String sql = "UPDATE pet SET Type=?, Level=?, Attack=?, Clean=?, Experience=?, IsAlive=? " +
                "WHERE PetID=?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pet.getType());  //Type就是名称
            stmt.setInt(2, pet.getLevel());
            stmt.setInt(3, pet.getAttack());
            stmt.setObject(4, pet.getClean());
            stmt.setObject(5, pet.getExperience());
            stmt.setObject(6, pet.getAlive() ? 1 : 0);
            stmt.setInt(7, pet.getPetId());

            stmt.executeUpdate();
        }
    }
}