package pokemon;

public class Move {
    private String name;
    private int power;  // 技能威力
    private int ppCost; // 使用该技能消耗的PP值

    public Move(String name, int power, int ppCost) {
        this.name = name;
        this.power = power;
        this.ppCost = ppCost;
    }

    // 计算伤害（简化公式）
    public int calculateDamage(int attackerAttack, int defenderDefense) {
        // 基础伤害 = 威力 + 攻击方攻击力 - 防御方防御力
        int damage = (int) (power + attackerAttack - defenderDefense*0.5);
        return Math.max(1, damage);  // 至少造成1点伤害
    }

    // Getters and Setters
    public String getName() { return name; }
    public int getPower() { return power; }
    public int getPpCost() { return ppCost; }

}