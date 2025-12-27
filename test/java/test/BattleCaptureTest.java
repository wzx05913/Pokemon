package test;

import battle.BattleManager;
import battle.BattleStepResult;
import database.UserDAO;
import database.PetDAO;
import entity.Bag;
import entity.Pet;
import entity.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import player.UnsupportedPlayerException;
import Player.Player;
import service.GameDataManager;
import pokemon.Pokemon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BattleCaptureTest {

    private UserDAO userDAO = new UserDAO();
    private PetDAO petDAO = new PetDAO();
    private int userId;

    @Before
    public void setup() throws Exception {
        User user = userDAO.createUser();
        userId = user.getUserId();

        // 初始化全局管理器
        GameDataManager.getInstance().clearSession();
        Player player = new Player(100, userId);
        GameDataManager.getInstance().setCurrentPlayer(player);
        Bag bag = new Bag(userId);
        bag.setCoins(100);
        GameDataManager.getInstance().setBag(bag);
    }

    @After
    public void teardown() throws Exception {
        // 清理数据库记录
        petDAO.deletePetsByUserId(userId);
    }

    @Test
    public void testBattleCaptureAndCoins() throws Exception {
        // 创建玩家宠物列表
        List<Pet> pets = new ArrayList<>();
        Pet p = new Pet(userId, "皮卡丘", 5, 50);
        p.setAlive(true);
        p.setClean(100);
        p.setExperience(0);
        pets.add(p);

        BattleManager bm = new BattleManager();
        bm.initBattle(pets);

        // 将敌人血量设为 1，确保下次玩家出手可以击倒
        Pokemon enemy = bm.getCurrentEnemyPokemon();
        Assert.assertNotNull("敌人应存在", enemy);
        enemy.setHp(1);

        // 设置确定性的随机（使捕获必定成功）
        Field rf = BattleManager.class.getDeclaredField("random");
        rf.setAccessible(true);
        rf.set(bm, new java.util.Random() {
            @Override
            public double nextDouble() { return 0.0; }
        });

        // 玩家使用第一个技能，触发击败逻辑
        BattleStepResult step = bm.playerUseMove(0);
        Assert.assertTrue("玩家出招应成功", step.isSuccess());

        // 检查战斗结束与金币增加
        boolean ended = bm.isBattleEnded();
        Assert.assertTrue("战斗应结束", ended);
        Assert.assertEquals("金币应增加30", Integer.valueOf(130), GameDataManager.getInstance().getPlayerBag().getCoins());

        // 触发捕获
        boolean caught = bm.tryCatchEnemy();
        Assert.assertTrue("应捕获成功", caught);

        // 内存列表包含新宠
        Assert.assertFalse("全局宠物列表应包含新宠", GameDataManager.getInstance().getPetList().isEmpty());

        // 数据库中应有记录
        List<entity.Pet> stored = petDAO.getPetsByUserId(userId);
        Assert.assertFalse("数据库应保存捕获的宠物", stored.isEmpty());
    }
}
