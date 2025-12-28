package pokemon;

public class Charmander extends Pokemon {
    public Charmander(int level) {
        super("小火龙", level);
    }

    @Override
    protected void calculateStats() {
        this.maxHp = 28 + (level - 1) * 7;
        this.attack = 22 + (level - 1) * 5;
        this.defense = 13 + (level - 1) * 2;
        this.hp = this.maxHp;
    }

    @Override
    protected void initializeMoves() {
        moves.add(new Move("火花", attack, 25));
        moves.add(new Move("抓", (int) (attack*1.4), 35));
        moves.add(new Move("叫声", 0, 20));  // 特殊技能，可能降低对方攻击
    }

    @Override
    protected int calculateExpToNextLevel() {
        return level * 90;  // 升级比皮卡丘稍快
    }

    @Override
    public void levelUp() {
        super.levelUp();
        // 小火龙特殊成长
        this.attack += 7;  // 攻击成长很高
        this.defense += 2; // 防御成长很低
    }
}