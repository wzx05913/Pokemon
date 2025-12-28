package pokemon;

public class Pikachu extends Pokemon {
    public Pikachu(int level) {
        super("皮卡丘", level);
    }

    @Override
    protected void calculateStats() {
        // 初始属性
        this.maxHp = 30 + (level - 1) * 8;
        this.attack = 20 + (level - 1) * 4;
        this.defense = 15 + (level - 1) * 3;
        this.hp = this.maxHp;
    }

    @Override
    protected void initializeMoves() {
        moves.add(new Move("电击", attack, 25));
        moves.add(new Move("撞击", (int) (attack*1.5), 35));
        moves.add(new Move("十万伏特", (int) (attack*1.2), 30));
    }

    @Override
    protected int calculateExpToNextLevel() {
        return level * 100;  // 每级需要 100*等级 经验
    }

    @Override
    public void levelUp() {
        super.levelUp();
        // 皮卡丘特殊成长
        this.attack += 6;  // 攻击成长更高
        this.defense += 3; // 防御成长较低
    }
}