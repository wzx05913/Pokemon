package service;

import java.sql.SQLException;
import entity.Bag;
import entity.Pet;
import java.util.ArrayList;
import java.util.List;
import Player.Player;
import database.BagDAO;
import database.DBConnection;
import database.PetDAO;
import database.UserDAO;

import pokemon.PokemonFactory;
import pokemon.Pokemon;
/**
 * 全局游戏数据管理器，存储跨场景共享的宠物、背包等数据
 */
public class GameDataManager {
    private static GameDataManager instance;
    
    //全局变量
    private int currentUserId;
    private Player currentPlayer;
    private Bag playerBag;
    private List<Pet> petList;
    private boolean isTemporary = false;
    private Pokemon currentPokemon;
    private List<Pokemon> pokemonList = new ArrayList<>();  // 新增：存储所有宝可梦
    
    private GameDataManager() {
        petList = new ArrayList<>();
        this.playerBag = new Bag();
        this.playerBag.setBagId(1); // 强制设置初始 ID 为 1
        this.playerBag.setCoins(100); // 也可以设置初始金币
        this.currentUserId = 1;
    }
    
    public static synchronized GameDataManager getInstance() {
        if (instance == null) {
            instance = new GameDataManager();
        }
        return instance;
    }
    
    // 从数据库加载存档数据
    public void loadGame(int userId) throws SQLException {
        this.currentUserId = userId;
        
        // 加载用户宠物
        PetDAO petDAO = new PetDAO();
        this.petList = petDAO.getPetsByUserId(userId);
        
        // 加载用户背包
        BagDAO bagDAO = new BagDAO();
        this.playerBag = bagDAO.getBagByUserId(userId);
        
        // 创建玩家并添加宠物
        this.currentPlayer = new Player(playerBag.getCoins(), userId);
        for (Pet pet : petList) {
            Pokemon pokemon = PokemonFactory.createPokemon(pet);
            currentPlayer.addPet(pokemon);
            pokemonList.add(pokemon);  // 添加到全局列表
        }
        
        // 初始化当前宝可梦
        if (!petList.isEmpty()) {
            Pokemon firstPokemon = PokemonFactory.createPokemon(petList.get(0));
            this.currentPokemon = firstPokemon;
        }
    }
    
    // 新增宠物到数据库和内存
    public void addPet(Pet pet){
        petList.add(pet);
    }
    
    // 新增：添加宝可梦到全局列表
    public void addPokemon(Pokemon pokemon) {
        pokemonList.add(pokemon);
    }
    
    // 新增：获取所有宝可梦
    public List<Pokemon> getPokemonList() {
        return pokemonList;
    }
    
    // getter和setter方法
    public int getCurUser() {
        return currentUserId;
    }
    
    public void setCurUser(int currentUserId) {
        this.currentUserId = currentUserId;
    }
    
    public Player getCurrentPlayer() {
        return currentPlayer;
    }
    
    public void setCurrentPlayer(Player player, boolean temporary) {
        this.currentPlayer = player;
        this.currentUserId = player.getId();
        this.isTemporary = temporary;
    }

    public Bag getPlayerBag() {
        return playerBag;
    }
    
    public void setBag(Bag currentBag) {
        this.playerBag = currentBag;
    }
    
    public List<Pet> getPetList() {
        return petList;
    }
    
    public boolean isTemporary() {
        return isTemporary;
    }

    // 清空当前会话（登出时使用）
    public void clearSession() {
    	currentUserId = 0;
        currentPlayer = null;
        petList = new ArrayList<>();
        playerBag = null;
        currentPokemon = null;
        pokemonList.clear();  // 清空宝可梦列表
        isTemporary = false;
    }

    // 减少所有宠物的清洁度（只修改内存全局变量），并同步到 Pokemon 对象
    public void decreaseAllPetsClean(int amount) {
        if (pokemonList == null || pokemonList.isEmpty()) {
            // 退回到对实体列表的处理（保守）
            if (petList == null) return;
            for (Pet pet : petList) {
                if (pet.getClean() == null) pet.setClean(100);
                int newClean = pet.getClean() - amount;
                pet.setClean(Math.max(0, newClean));
                if (pet.getClean() <= 0) pet.setAlive(false);
            }
            return;
        }

        // 优先通过内存中的 Pokemon 实例减少清洁度，并尝试将变更同步回实体 Pet 列表
        for (Pokemon p : pokemonList) {
            if (p == null) continue;
            int cur = p.getClean();
            int next = Math.max(0, cur - amount);
            p.setClean(next);
            if (next <= 0) {
                p.setAlive(false);
                p.setHp(0);
            }

            // 在 petList 中寻找对应实体并同步（通过 Type/Level/Attack 尝试匹配）
            if (petList != null) {
                for (Pet pet : petList) {
                    if (pet == null) continue;
                    String petType = pet.getType();
                    String pType = p.getType();
                    if (petType == null || pType == null) continue;
                    if (petType.trim().equals(pType.trim())
                            && pet.getLevel() == p.getLevel()
                            && pet.getAttack() == p.getAttack()) {
                        pet.setClean(next);
                        pet.setAlive(p.isAlive());
                        break;
                    }
                }
            }
        }
    }
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void setCurrentBag(Bag bag) {
        this.playerBag = bag;
    }

    public void setCoins(int coins) {
        if (playerBag != null) {
        	playerBag.setCoins(coins);
        }
    }
    
    public void addCoins(int coins) {
    	if (playerBag != null) {
        	playerBag.setCoins(playerBag.getCoins()+coins);
        }
    }
    public int getCurrentUserId() {
        return currentPlayer != null ? currentPlayer.getId() : currentUserId;
    }


    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
        if (player != null) {
            this.currentUserId = player.getId();
        }
    }

    public Pokemon getCurrentPokemon() {
        return currentPokemon;
    }

    public void setCurrentPokemon(Pokemon pokemon) {
        this.currentPokemon = pokemon;
    }
}