package service;

import entity.Bag;
import entity.Pet;
import java.util.ArrayList;
import java.util.List;

/**
 * 全局游戏数据管理器，存储跨场景共享的宠物、背包等数据
 */
public class GameDataManager {
    // 单例模式确保全局唯一实例
    private static GameDataManager instance;
    
    // 全局宠物列表
    private List<Pet> petList;
    
    // 全局背包
    private Bag playerBag;
    private int curUser = 0; // 默认用户ID为0    
    private GameDataManager() {
        // 初始化容器
        petList = new ArrayList<>();
    }
    // 初始化新游戏数据
    public void initNewGame() {
        petList = new ArrayList<>();
        playerBag = new Bag(curUser); // 使用当前用户ID初始化背包
    }
    public static synchronized GameDataManager getInstance() {
        if (instance == null) {
            instance = new GameDataManager();
        }
        return instance;
    }

    // 宠物列表相关操作
    public List<Pet> getPetList() {
        return petList;
    }
    
    public void addPet(Pet pet) {
        petList.add(pet);
    }
    
    public void removePet(Pet pet) {
        petList.remove(pet);
    }
    
    // 背包相关操作
    public void setCoins(int coins) {
    	playerBag.setCoins(coins);
    }
    
    public Bag getPlayerBag() {
        return playerBag;
    }
    
    public void setPlayerBag(Bag bag) {
        this.playerBag = bag;
    }
    //当前用户
    public int getCurUser() {
    	return curUser; 
    }
    public void setCurUser(int curUser) {
    	this.curUser=curUser;
    }
    // 重置数据（切换用户或新游戏时使用）
    public void reset() {
        petList.clear();
        playerBag = null;
    }
}