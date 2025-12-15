package entity;

/**
 * 对应数据库pet表的实体类
 */
public class Pet {
    private int petId;
    private int userId; // 关联用户ID
    private String name;
    private String type; // 宠物类型（如"小火龙"）
    private int level;
    private int attack;
    private Integer clean; // 清洁度
    private Integer experience; // 当前经验
    private Boolean isAlive; // 是否存活

    public Pet() {}

    // 带参构造（创建新宠物时使用）
    public Pet(int userId, String name, String type, int level, int attack) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.level = level;
        this.attack = attack;
    }

    // Getter和Setter
    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public Integer getClean() { return clean; }
    public void setClean(Integer clean) { this.clean = clean; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public Boolean getAlive() { return isAlive; }
    public void setAlive(Boolean alive) { isAlive = alive; }
}