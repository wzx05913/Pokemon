package pokemon;

public class Psyduck extends Pokemon {
    public Psyduck(int level) {
        super("可达鸭", level);
    }

    @Override
    protected void calculateStats() {
        // 可达鸭：中等HP，随机攻击，低防御
        this.maxHp = 35 + (level - 1) * 7;
        this.attack = 18 + (level - 1) * 4;
        this.defense = 16 + (level - 1) * 3;
        this.hp = this.maxHp;
    }

    @Override
    protected void initializeMoves() {
        moves.add(new Move("抓", (int) (attack*1.1), 35));
        moves.add(new Move("水枪", attack, 25));
        moves.add(new Move("念力", (int) (attack*1.6), 15));  // 强力但PP少
    }

    @Override
    protected int calculateExpToNextLevel() {
        return level * 95;
    }

    @Override
    public void levelUp() {
        super.levelUp();
        // 可达鸭的特殊成长 - 有时会有惊喜
        if (Math.random() > 0.7) {  // 30%几率
            this.attack += 10;  // 大幅提升攻击
            System.out.println("可达鸭的头疼发作了！攻击力大幅提升！");
        } else {
            this.attack += 4;
        }
        this.defense += 3;
    }

    // 可达鸭的特殊能力 - 头疼爆发
    public int headacheAttack(Pokemon target) {
        System.out.println("可达鸭头疼爆发了！");
        int baseDamage = 20;
        int randomBonus = (int)(Math.random() * 30) + 1;  // 1-30随机伤害
        int damage = baseDamage + randomBonus + attack - target.getDefense();
        return Math.max(1, damage);
    }
}
