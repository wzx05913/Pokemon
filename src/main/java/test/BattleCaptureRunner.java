package test;

import battle.BattleManager;
import battle.BattleStepResult;
import database.UserDAO;
import database.PetDAO;
import entity.Bag;
import entity.Pet;
import entity.User;
import Player.Player;
import service.GameDataManager;
import pokemon.Pokemon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BattleCaptureRunner {
    public static void main(String[] args) throws Exception {
        UserDAO userDAO = new UserDAO();
        PetDAO petDAO = new PetDAO();

        User user = userDAO.createUser();
        int userId = user.getUserId();
        System.out.println("Created test user id=" + userId);

        // init game data
        GameDataManager.getInstance().clearSession();
        Player player = new Player(100, userId);
        GameDataManager.getInstance().setCurrentPlayer(player);
        Bag bag = new Bag(userId);
        bag.setCoins(100);
        GameDataManager.getInstance().setBag(bag);

        // prepare player's pet
        List<Pet> pets = new ArrayList<>();
        Pet p = new Pet(userId, "皮卡丘", 5, 50);
        p.setAlive(true);
        p.setClean(100);
        p.setExperience(0);
        pets.add(p);

        BattleManager bm = new BattleManager();
        bm.initBattle(pets);

        Pokemon enemy = bm.getCurrentEnemyPokemon();
        System.out.println("Enemy before: " + (enemy != null ? enemy.getInfo() : "null"));
        if (enemy != null) enemy.setHp(1);

        // make capture deterministic
        Field rf = BattleManager.class.getDeclaredField("random");
        rf.setAccessible(true);
        rf.set(bm, new java.util.Random() { @Override public double nextDouble() { return 0.0; } });

        BattleStepResult step = bm.playerUseMove(0);
        System.out.println("PlayerUseMove: " + step.getMessage());

        boolean ended = bm.isBattleEnded();
        System.out.println("Battle ended: " + ended);
        System.out.println("Coins after battle: " + GameDataManager.getInstance().getPlayerBag().getCoins());

        boolean caught = bm.tryCatchEnemy();
        System.out.println("Caught: " + caught);

        System.out.println("Global petList size: " + GameDataManager.getInstance().getPetList().size());

        // list DB pets
        List<entity.Pet> stored = petDAO.getPetsByUserId(userId);
        System.out.println("DB stored pets count: " + stored.size());

        // cleanup
        petDAO.deletePetsByUserId(userId);
        System.out.println("Cleaned up DB pets for user " + userId);
    }
}
