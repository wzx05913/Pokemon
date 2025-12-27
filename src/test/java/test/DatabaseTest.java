package test;

import database.DBConnection;
import database.UserDAO;
import database.PetDAO;
import database.BagDAO;
import entity.User;
import entity.Pet;
import entity.Bag;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import static org.junit.Assert.*;

public class DatabaseTest {

    // 初始化数据库连接（所有测试前执行）
    @BeforeClass
    public static void initConnection() {
        try {
            // 验证连接池初始化成功
            Connection conn = DBConnection.getInstance().getConnection();
            assertNotNull("数据库连接失败", conn);
            System.out.println("数据库连接测试成功");
            conn.close(); // 用完关闭（连接池会回收）
        } catch (SQLException e) {
            fail("初始化数据库连接失败：" + e.getMessage());
        }
    }

    // 测试用户表操作
    @Test
    public void testUserDAO() throws SQLException {
        UserDAO userDAO = new UserDAO();

        // 测试创建用户（users表为自增ID，无其他字段）
        User newUser = userDAO.createUser();
        assertNotNull("创建用户失败", newUser);
        int userId = newUser.getUserId();
        assertTrue("用户ID应为正数", userId > 0);

        // 测试查询用户
        User foundUser = userDAO.getUserById(userId);
        assertNotNull("查询用户失败", foundUser);
        assertEquals("用户ID不匹配", userId, foundUser.getUserId());
    }

    // 测试宠物表操作
    @Test
    public void testPetDAO() throws SQLException {
        UserDAO userDAO = new UserDAO();
        PetDAO petDAO = new PetDAO();

        // 先创建一个用户作为关联
        User user = userDAO.createUser();
        int userId = user.getUserId();

        // 测试新增宠物
        Pet pet = new Pet(userId, "Charmander", 1, 22);
        pet.setAlive(true);
        petDAO.createPet(pet);

        // 测试查询用户的宠物
        List<Pet> pets = petDAO.getPetsByUserId(userId);
        assertFalse("用户应至少有一只宠物", pets.isEmpty());

        // 验证宠物信息
        Pet savedPet = pets.get(0);
        assertEquals("宠物名称不匹配", "测试小火龙", savedPet.getName());
        assertEquals("宠物等级不匹配", 1, savedPet.getLevel());

        // 测试更新宠物
        savedPet.setLevel(2);
        petDAO.updatePet(savedPet);
        Pet updatedPet = petDAO.getPetsByUserId(userId).get(0);
        assertEquals("宠物等级更新失败", 2, updatedPet.getLevel());
    }

    // 测试背包表操作
    @Test
    public void testBagDAO() throws SQLException {
        UserDAO userDAO = new UserDAO();
        BagDAO bagDAO = new BagDAO();

        // 创建用户并初始化背包
        User user = userDAO.createUser();
        int userId = user.getUserId();
        Bag bag = new Bag(userId);
        bag.setCoins(100); // 初始100金币
        bagDAO.createBag(bag);

        // 测试查询背包
        Bag userBag = bagDAO.getBagByUserId(userId);
        assertNotNull("查询背包失败", userBag);
        assertEquals("初始金币数不匹配", 100, userBag.getCoins().intValue());

        // 测试更新背包
        userBag.setCoins(200);
        bagDAO.updateBag(userBag);
        Bag updatedBag = bagDAO.getBagByUserId(userId);
        assertEquals("金币数更新失败", 200, updatedBag.getCoins().intValue());
    }

    // 所有测试后关闭连接池
    @AfterClass
    public static void closeConnection() {
        DBConnection.getInstance().close();
        System.out.println("数据库连接池已关闭");
    }
}