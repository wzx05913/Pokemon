package entity;

public class Bag {
    private int bagId;
    private int userId; // 关联用户ID
    private Integer eggCount; // 复活食物数量
    private Integer riceCount; // 经验食物数量
    private Integer soapCount; // 肥皂数量
    private Integer coins; // 金币数量

    public Bag() {}

    //创建新用户背包时使用
    public Bag(int userId) {
        this.userId = userId;
        this.eggCount = 0;
        this.riceCount = 0;
        this.soapCount = 0;
        this.coins = 0;
    }

    // Getter和Setter
    public int getBagId() { return bagId; }
    public void setBagId(int bagId) { this.bagId = bagId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getEggCount() { return eggCount; }
    public void setEggCount(Integer eggCount) { this.eggCount = eggCount; }

    public Integer getRiceCount() { return riceCount; }
    public void setRiceCount(Integer riceCount) { this.riceCount = riceCount; }

    public Integer getSoapCount() { return soapCount; }
    public void setSoapCount(Integer soapCount) { this.soapCount = soapCount; }

    public Integer getCoins() { return coins; }
    public void setCoins(Integer coins) { this.coins = coins; }
}