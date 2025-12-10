package pokemon;

import java.util.List;
import java.util.ArrayList;

public abstract class Pokemon {
    // 基本属性
    protected String name;      // 名称
    protected int level;       // 等级
    protected int hp;          // 当前生命值
    protected int maxHp;       // 最大生命值
    protected int attack;      // 攻击力
    protected int defense;     // 防御力
    protected int exp;         // 当前经验值
    protected int expToNextLevel; // 升级所需经验

    // 技能列表
    protected List<Move> moves = new ArrayList<>();

    // 构造函数
    public Pokemon(String name, int level) {
        this.name = name;
        this.level = level;
        calculateStats();  // 计算属性
        initializeMoves(); // 初始化技能
        this.exp = 0;
        this.expToNextLevel = calculateExpToNextLevel();
    }

    // 抽象方法 - 子类必须实现
    protected abstract void calculateStats();  // 计算属性值
    protected abstract void initializeMoves(); // 初始化技能
    protected abstract int calculateExpToNextLevel(); // 计算升级经验

    // 受到伤害
    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    // 恢复生命值
    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }

    // 完全恢复
    public void fullHeal() {
        this.hp = this.maxHp;
    }

    // 获得经验
    public void gainExp(int expGained) {
        this.exp += expGained;
        if (this.exp >= this.expToNextLevel) {
            levelUp();
        }
    }

    // 升级
    public void levelUp() {
        this.level++;
        this.exp -= this.expToNextLevel;
        this.expToNextLevel = calculateExpToNextLevel();

        // 升级成长（子类可以重写）
        this.maxHp += 10;
        this.attack += 5;
        this.defense += 5;
        this.hp = this.maxHp;  // 升级后恢复满血

        System.out.println(name + " 升到了 " + level + " 级！");
    }

    // 使用技能
    public void useMove(int moveIndex, Pokemon target) {
        if (moveIndex >= 0 && moveIndex < moves.size()) {
            Move move = moves.get(moveIndex);
            int damage = move.calculateDamage(this.attack, target.defense);
            target.takeDamage(damage);
            System.out.println(name + " 使用了 " + move.getName() + "，造成了 " + damage + " 点伤害！");
        }
    }

    // 是否濒死
    public boolean isFainted() {
        return hp <= 0;
    }

    // 获取信息
    public String getInfo() {
        return String.format("%s Lv.%d HP: %d/%d", name, level, hp, maxHp);
    }

    // 技能信息
    public String getMovesInfo() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            sb.append(i + 1).append(". ").append(move.getName())
                    .append(" (").append(move.getPower()).append(")\n");
        }
        return sb.toString();
    }

    // Getters
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getExp() { return exp; }
    public int getExpToNextLevel() { return expToNextLevel; }
    public List<Move> getMoves() { return moves; }

    // Setters
    public void setHp(int hp) { this.hp = Math.min(hp, maxHp); }
}

