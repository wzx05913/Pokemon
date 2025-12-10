package pokemon;

public class Jigglypuff extends Pokemon {
    public Jigglypuff(int level) {
        super("胖丁", level);
    }

    @Override
    protected void calculateStats() {
        // 胖丁：高HP，低攻击，中防御
        this.maxHp = 40 + (level - 1) * 10;  // HP很高
        this.attack = 15 + (level - 1) * 2;  // 攻击很低
        this.defense = 18 + (level - 1) * 4; // 防御中等
        this.hp = this.maxHp;
    }

    @Override
    protected void initializeMoves() {
        moves.add(new Move("拍击", 35, 35));
        moves.add(new Move("唱歌", 0, 15));    // 特殊技能：可能使对手睡眠
        moves.add(new Move("连环巴掌", 15, 10)); // 特殊：多次攻击
    }

    @Override
    protected int calculateExpToNextLevel() {
        return level * 85;  // 升级较快
    }

    @Override
    public void levelUp() {
        super.levelUp();
        // 胖丁特殊成长
        this.maxHp += 12;  // HP成长非常高
        this.attack += 2;  // 攻击成长很低
        this.defense += 5; // 防御成长中等
        this.hp = this.maxHp;
    }

    // 胖丁的特殊技能效果
    public boolean useSing(Pokemon target) {
        System.out.println("胖丁唱起了催眠曲！");
        // 这里可以添加睡眠状态的逻辑
        return Math.random() > 0.5;  // 50%几率成功
    }
}
