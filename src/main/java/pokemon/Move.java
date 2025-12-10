package pokemon;

public class Move {
    private String name;
    private int power;  // 威力
    private int pp;     // 使用次数
    private int maxPP;

    public Move(String name, int power, int maxPP) {
        this.name = name;
        this.power = power;
        this.maxPP = maxPP;
        this.pp = maxPP;
    }

    // 计算伤害（简化公式）
    public int calculateDamage(int attackerAttack, int defenderDefense) {
        // 基础伤害 = 威力 + 攻击方攻击力 - 防御方防御力
        int damage = power + attackerAttack - defenderDefense;
        return Math.max(1, damage);  // 至少造成1点伤害
    }

    public boolean canUse() {
        return pp > 0;
    }

    public void use() {
        if (pp > 0) {
            pp--;
        }
    }

    public void restore() {
        pp = maxPP;
    }

    // Getters
    public String getName() { return name; }
    public int getPower() { return power; }
    public int getPP() { return pp; }
    public int getMaxPP() { return maxPP; }
}
