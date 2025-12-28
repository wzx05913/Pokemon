package pokemon;


public class Squirtle extends Pokemon {
    public Squirtle(int level) {
        super("杰尼龟", level);
    }

    @Override
    protected void calculateStats() {
        this.maxHp = 35 + (level - 1) * 9;
        this.attack = 18 + (level - 1) * 3;
        this.defense = 25 + (level - 1) * 6;
        this.hp = this.maxHp;
    }

    @Override
    protected void initializeMoves() {
        moves.add(new Move("水枪", attack, 25));
        moves.add(new Move("撞击", (int) (attack*1.4), 35));
        moves.add(new Move("缩入壳中", 0, 20));  // 特殊技能，提高防御
    }

    @Override
    protected int calculateExpToNextLevel() {
        return level * 110;  // 升级最慢
    }

    @Override
    public void levelUp() {
        super.levelUp();
        // 杰尼龟特殊成长
        this.attack += 3;  // 攻击成长低
        this.defense += 8; // 防御成长高
    }
}