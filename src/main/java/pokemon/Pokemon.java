package pokemon;

import javax.lang.model.element.Name;
import java.util.List;
import java.util.ArrayList;
import javax.lang.model.element.Name;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import javafx.scene.image.Image;


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
    protected int maxPp;  // 最大PP值
    protected int pp;     // 当前PP值

    // 状态：睡眠
    protected boolean asleep = false;
    protected int asleepTurns = 0;
    
    // 清洁度与存活状态
    protected int clean = 100; // 默认为100
    protected boolean alive = true;

    // 最近一次伤害是否暴击
    private boolean lastCritical = false;

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
        this.maxPp = 100;  // 固定最大PP为100
        this.pp = maxPp;   // 初始PP为最大值
    }
    public int basicAttack(Pokemon target) {
        if (target == null) return 0;
        // 计算：基础伤害以攻击的80%为主，再略微考虑防御
        int raw = (int)Math.round(this.attack * 0.8);
        int reduction = target.getDefense() / 4; // 防御只削弱一部分
        int damage = Math.max(1, raw - reduction);

        // 暴击：20%概率，1.5倍伤害
        boolean crit = rollCritical();
        if (crit) {
            damage = (int)Math.round(damage * 1.5);
        }
        this.lastCritical = crit;

        target.takeDamage(damage);
        System.out.println(this.name + " 进行了普通攻击，造成 " + damage + " 点伤害" + (crit ? "（暴击！）" : ""));
        return damage;
    }
    // 抽象方法 - 子类必须实现
    protected abstract void calculateStats();  // 计算属性值
    protected abstract void initializeMoves(); // 初始化技能
    protected abstract int calculateExpToNextLevel(); // 计算升级经验


    protected boolean inBattle = false;

    // 标记进入/退出战斗
    public void enterBattle() {
        this.inBattle = true;
    }
    public void exitBattle() {
        this.inBattle = false;
    }

    // 修改 fullHeal，避免战斗中被误调用恢复
    public void fullHeal() {
        if (inBattle) {
            // 战斗中不允许被外部随意 fullHeal（避免战死后复活）
            System.err.println("DEBUG: 被禁止的 fullHeal 调用: " + name + "（inBattle=true）");
            return;
        }
        this.hp = this.maxHp;
    }

    // 可选：在 setHp 中记录来自哪里（用于进一步调试）
    // 这是可选的调试代码，如果你愿意可以临时加入，便于追踪谁在恢复 HP
    public void setHp(int hp) {
        // 保持原有逻辑，仍然允许设置 hp（但是 fullHeal 禁止）
        this.hp = Math.min(hp, maxHp);
    }
    // 受到伤害
    public void takeDamage(int damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    // 恢复生命值
    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }


    // 获得经验
    public void gainExp(int expGained) {
        this.exp += expGained;
        if (this.exp >= this.expToNextLevel) {
            levelUp();
            exp = exp % this.expToNextLevel;
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

    // 恢复PP（每次出场恢复20点）
    public void restorePpOnEnter() {
        this.pp = Math.min(maxPp, this.pp + 20);
    }

    // 每回合小幅回复PP（用于跳过回合时逐回合恢复）
    public void recoverPpEachTurn(int amount) {
        this.pp = Math.min(maxPp, this.pp + amount);
    }

    /**
     * 使用技能（带PP检查和消耗）
     * 返回值含义：
     *  -1 表示无法使用（PP 或 状态阻止）
     *   0 表示没有造成伤害但效果生效（如催眠、降攻、加攻、加防）
     *  >0 表示造成的伤害值
     */
    public int useMove(int moveIndex, Pokemon target) {
        if (moveIndex >= 0 && moveIndex < moves.size()) {
            Move move = moves.get(moveIndex);

            // 如果自己处于睡眠中不能行动
            if (this.isAsleep()) {
                System.out.println(name + " 处于睡眠状态，无法行动！");
                return -1;
            }

            // 检查PP是否足够
            if (pp < move.getPpCost()) {
                System.out.println(name + " 的PP不足，无法使用 " + move.getName() + "！");
                return -1;
            }

            pp -= move.getPpCost();

            // 特殊技能判定（通过名字简单匹配）
            String moveName = move.getName();
            if ("叫声".equals(moveName)) {
                // 降低目标攻击（不可低于1）
                if (target != null) {
                    target.reduceAttack(5);
                    System.out.println(name + " 使用了 叫声，降低了 " + target.getName() + " 的攻击！");
                }
                this.lastCritical = false;
                return 0;
            } else if ("唱歌".equals(moveName) || "唱".equals(moveName)) {
                // 使对方沉睡一回合
                if (target != null) {
                    target.setAsleep(1);
                    System.out.println(name + " 唱起了催眠曲，" + target.getName() + " 进入了睡眠！");
                }
                this.lastCritical = false;
                return 0;
            } else if ("生长".equals(moveName)) {
                // 提高自身攻击
                this.increaseAttack(5);
                System.out.println(name + " 使用了 生长，提升了自身攻击！");
                this.lastCritical = false;
                return 0;
            } else if ("缩入壳中".equals(moveName)) {
                // 提高自身防御
                this.increaseDefense(5);
                System.out.println(name + " 使用了 缩入壳中，提升了自身防御！");
                this.lastCritical = false;
                return 0;
            } else {
                // 普通伤害技能
                int damage = move.calculateDamage(this.attack, target.defense);

                // 暴击判定
                boolean crit = rollCritical();
                if (crit) {
                    damage = (int)Math.round(damage * 1.5);
                }
                this.lastCritical = crit;

                target.takeDamage(damage);
                System.out.println(name + " 使用了 " + move.getName() + "，造成了 " + damage + " 点伤害" + (crit ? "（暴击！）" : "") + "！");
                return Math.max(0, damage);
            }
        }
        return -1;
    }

    // 检查是否PP耗尽（所有技能都无法使用）
    public boolean isPpDepleted() {
        for (Move move : moves) {
            if (pp >= move.getPpCost()) {
                return false; // 还有可用技能
            }
        }
        return true;
    }
    // 是否濒死
    public boolean isFainted() {
        return hp <= 0;
    }

    // 睡眠相关
    public boolean isAsleep() {
        return asleep && asleepTurns > 0;
    }

    public void setAsleep(int turns) {
        this.asleep = true;
        this.asleepTurns = Math.max(1, turns);
    }

    // 在跳过回合时调用，减少睡眠回合数
    public void tickAsleep() {
        if (asleep) {
            asleepTurns--;
            if (asleepTurns <= 0) {
                asleep = false;
                asleepTurns = 0;
                System.out.println(name + " 清醒了！");
            }
        }
    }

    // 降低攻击（用于“叫声”等技能）
    public void reduceAttack(int amount) {
        this.attack = Math.max(1, this.attack - amount);
    }

    // 提高攻击（用于“生长”等技能）
    public void increaseAttack(int amount) {
        this.attack = Math.max(1, this.attack + amount);
    }

    // 提高防御（用于“缩入壳中”等技能）
    public void increaseDefense(int amount) {
        this.defense = Math.max(1, this.defense + amount);
    }

    // 最近一次是否暴击（供 BattleManager 读取）
    public boolean wasLastCritical() {
        return lastCritical;
    }

    // 暴击概率（可按需调整）
    protected boolean rollCritical() {
        return Math.random() < 0.10; // 20% 概率
    }

    // 获取信息
    public String getInfo() {
        return String.format("%s Lv.%d HP: %d/%d", name, level, hp, maxHp);
    }

    // Clean 相关
    public int getClean() { return clean; }
    public void setClean(int clean) { this.clean = Math.max(0, Math.min(100, clean)); }

    // 存活状态
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; if (!alive) this.hp = 0; }

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

    // 添加PP相关的getter和setter（补充完整性）
    public int getMaxPp() {
        return maxPp;
    }

    public int getPp() {
        return pp;
    }

    public void setPp(int pp) {
        this.pp = Math.min(pp, maxPp); // 确保不超过最大值
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

    public void setExp(int exp) { this.exp = Math.min(this.maxHp, exp); }
    public String getType(){return name;}
    public void setType(String name) { this.name = name; }

    public void setAttack(int attack) {
        this.attack = attack;
    }
}