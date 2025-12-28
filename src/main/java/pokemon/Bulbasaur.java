package pokemon;

public class Bulbasaur extends Pokemon {
    public Bulbasaur(int level) {
        super("妙蛙种子", level);
    }

    @Override
    protected void calculateStats() {
        this.maxHp = 32 + (level - 1) * 8;
        this.attack = 20 + (level - 1) * 4;
        this.defense = 20 + (level - 1) * 4;
        this.hp = this.maxHp;
    }

    @Override
    protected void initializeMoves() {
        moves.add(new Move("藤鞭", attack, 25));
        moves.add(new Move("撞击", (int) (attack*1.2), 35));
        moves.add(new Move("生长", 0, 20));  // 特殊技能，提高攻击
    }

    @Override
    protected int calculateExpToNextLevel() {
        return level * 90;
    }

    @Override
    public void levelUp() {
        super.levelUp();
        // 妙蛙种子平衡成长
        this.attack += 4;
        this.defense += 4;
    }
}
