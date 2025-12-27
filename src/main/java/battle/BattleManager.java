// battle/BattleManager.java
package battle;
import java.util.ArrayList;
import pokemon.Pokemon;
import pokemon.PokemonFactory;
import pokemon.PokemonType;
import entity.Pet;
import entity.Bag;
import Player.Player;
import pokemon.Bulbasaur;
import pokemon.Charmander;
import pokemon.Jigglypuff;
import pokemon.Pikachu;
import pokemon.Psyduck;
import pokemon.Squirtle;
import pokemon.Move;
import service.GameDataManager;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.Random;
//新增字段

public class BattleManager {
    private boolean enemyJustDefeated = false;
    private Queue<Pokemon> playerQueue;
    private Queue<Pokemon> enemyQueue;
    private Pokemon currentPlayerPokemon;
    private Pokemon currentEnemyPokemon;
    private boolean isPlayerTurn;
    private Random random = new Random();
    private BattleResult battleResult;
    private Pokemon lastDefeatedEnemy; // 最近被击败的敌人，用于战后捕捉

    // 初始化战斗
    public void initBattle(List<Pet> petList) {
        // 复制宠物列表并按等级降序排列
        playerQueue = new LinkedList<>();
        List<Pokemon> playerPokemons = petList.stream()
                .map(PokemonFactory::createPokemon)
                .sorted((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel()))
                .collect(Collectors.toList());

        playerQueue.addAll(playerPokemons);

        // 初始化敌人
        initEnemy(playerPokemons);

        // 战斗开始时所有宠物满状态
        playerQueue.forEach(p -> {
            p.fullHeal();
            p.setPp(p.getMaxPp());
        });

        isPlayerTurn = true;
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();
    }

    // 初始化敌人
    private void initEnemy(List<Pokemon> playerPokemons) {
        enemyQueue = new LinkedList<>();
        int enemyLevel;

        if (playerPokemons.size() == 1) {
            enemyLevel = playerPokemons.get(0).getLevel() - 1;
        } else {
            int maxLevel = playerPokemons.get(0).getLevel();
            enemyLevel = maxLevel + random.nextInt(3) + 2; // 2-4级
        }

        // 确保等级至少为1
        enemyLevel = Math.max(1, enemyLevel);

        // 随机选择一种宝可梦类型
        PokemonType[] types = PokemonType.values();
        PokemonType randomType = types[random.nextInt(types.length)];

        // 创建敌人宝可梦
        Pokemon enemy = createEnemyPokemon(String.valueOf(randomType), enemyLevel);
        enemyQueue.add(enemy);
    }

    // 创建敌人宝可梦
    private Pokemon createEnemyPokemon(String type, int level) {
        switch (type) {
            case "妙蛙种子":
                return new Bulbasaur(level);
            case "小火龙":
                return new Charmander(level);
            case "杰尼龟":
                return new Squirtle(level);
            case "皮卡丘":
                return new Pikachu(level);
            case "胖丁":
                return new Jigglypuff(level);
            case "可达鸭":
                return new Psyduck(level);
            default:
                return new Pikachu(level); // 默认皮卡丘
        }
    }
    // 新增字段
    private Pokemon lastDefeatedPlayer; // 最近被击败的玩家宠物（用于UI显示）

    public BattleStepResult playerUseMove(int moveIndex) {
        // ... 参数检查、PP检测等 ...

        int beforeHp = currentEnemyPokemon != null ? currentEnemyPokemon.getHp() : 0;
        currentPlayerPokemon.useMove(moveIndex, currentEnemyPokemon);
        int afterHp = currentEnemyPokemon != null ? currentEnemyPokemon.getHp() : 0;
        int damage = Math.max(0, beforeHp - afterHp);

        StringBuilder message = new StringBuilder();
        message.append(currentPlayerPokemon.getName()).append(" 使用了 ").append(currentPlayerPokemon.getMoves().get(moveIndex).getName())
                .append("，造成了 ").append(damage).append(" 点伤害");

        if (currentEnemyPokemon != null && currentEnemyPokemon.isFainted()) {
            message.append("\n敌人的").append(currentEnemyPokemon.getName()).append("被击败了！");
            lastDefeatedEnemy = currentEnemyPokemon;
            enemyQueue.poll();          // 从队列移除被击败者
            currentEnemyPokemon = null; // 保留 null 以便 UI 显示被击败的那一只
            enemyJustDefeated = true;   // 标记刚击败敌人
        }

        isPlayerTurn = false;
        return new BattleStepResult(true, message.toString());
    }

    // 新增对标志的访问与推进方法
    public boolean wasEnemyJustDefeated() { return enemyJustDefeated; }
    public void advanceAfterDefeat() {
        enemyJustDefeated = false;
        currentEnemyPokemon = enemyQueue.peek(); // 可能为null
    }

    // 修改 enemyUseMove：当玩家宠物被击败时记录 lastDefeatedPlayer
    public BattleStepResult enemyUseMove() {
        // 如果没有当前敌人，尝试从队列获取（安全防护）
        if (currentEnemyPokemon == null) {
            currentEnemyPokemon = enemyQueue.peek();
        }

        if (isPlayerTurn || currentEnemyPokemon == null) {
            return new BattleStepResult(false, "不是敌人的回合或无敌人");
        }

        // 选择伤害最高的可用技能
        List<Move> moves = currentEnemyPokemon.getMoves();
        Move bestMove = null;
        for (Move m : moves) {
            if (currentEnemyPokemon.getPp() >= m.getPpCost() &&
                    (bestMove == null || m.getPower() > bestMove.getPower())) {
                bestMove = m;
            }
        }

        if (bestMove == null) {
            isPlayerTurn = true;
            return new BattleStepResult(true, "敌人没有可用技能了");
        }

        int moveIndex = moves.indexOf(bestMove);
        int beforeHp = currentPlayerPokemon != null ? currentPlayerPokemon.getHp() : 0;
        currentEnemyPokemon.useMove(moveIndex, currentPlayerPokemon);
        int afterHp = currentPlayerPokemon != null ? currentPlayerPokemon.getHp() : 0;
        int damage = Math.max(0, beforeHp - afterHp);

        StringBuilder message = new StringBuilder();
        message.append("敌人的").append(currentEnemyPokemon.getName())
                .append(" 使用了 ").append(bestMove.getName())
                .append("，造成了 ").append(damage).append(" 点伤害");

        if (currentPlayerPokemon != null && currentPlayerPokemon.isFainted()) {
            message.append("\n你的").append(currentPlayerPokemon.getName()).append("战死了！");
            lastDefeatedPlayer = currentPlayerPokemon; // 记录玩家的最后一只被击败宠物
            playerQueue.poll(); // 移除该宠物
            currentPlayerPokemon = null; // 保持 null，让 UI 能显示 lastDefeatedPlayer
        } else if (currentPlayerPokemon != null && currentPlayerPokemon.isPpDepleted()) {
            message.append("\n你的").append(currentPlayerPokemon.getName()).append("累死了！");
            lastDefeatedPlayer = currentPlayerPokemon;
            playerQueue.poll();
            currentPlayerPokemon = null;
        } else {
            // 正常轮换
            playerQueue.poll();
            playerQueue.add(currentPlayerPokemon);
        }

        // 敌人轮换（若仍可用）
        if (!currentEnemyPokemon.isFainted() && !currentEnemyPokemon.isPpDepleted()) {
            enemyQueue.poll();
            enemyQueue.add(currentEnemyPokemon);
        }

        // 更新当前（尝试从队列读取）
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();

        isPlayerTurn = true;
        return new BattleStepResult(true, message.toString());
    }

    public boolean isBattleEnded() {
        boolean playerHasValid = playerQueue.stream().anyMatch(p -> !p.isFainted() && !p.isPpDepleted());
        boolean enemyHasValid  = enemyQueue.stream().anyMatch(p -> !p.isFainted() && !p.isPpDepleted());

        if (!playerHasValid) {
            battleResult = BattleResult.ENEMY_WIN;
            return true;
        }
        if (!enemyHasValid) {
            battleResult = BattleResult.PLAYER_WIN;
            return true;
        }
        return false;
    }

    // 尝试捕获敌人
    public boolean tryCatchEnemy() {
        // 只有玩家胜利后可以捕获
        if (battleResult != BattleResult.PLAYER_WIN) {
            return false;
        }

        // 捕获目标优先为最近被击败的敌人
        Pokemon enemy = lastDefeatedEnemy != null ? lastDefeatedEnemy : currentEnemyPokemon;
        if (enemy == null) return false;

        // 30% 概率捕获
        if (random.nextDouble() <= 0.3) {
            int userId = GameDataManager.getInstance().getCurrentUserId();

            // 使用 PetFactory 创建实体并保存到数据库（如果需要）以及内存
            entity.Pet newPet = service.PetFactory.createPetEntity(userId, enemy);

            userId = GameDataManager.getInstance().getCurrentUserId();
            if (userId > 0) {
                try {
                    database.PetDAO petDAO = new database.PetDAO();
                    petDAO.createPet(newPet);
                } catch (java.sql.SQLException e) {
                    System.err.println("捕获后保存到数据库失败: " + e.getMessage());
                }
            } else {
                // 临时玩家，不写数据库，仅更新内存
            }

            // 更新全局内存列表
            GameDataManager.getInstance().getPetList().add(newPet);

            // 同步到当前Player对象（如果存在）
            if (GameDataManager.getInstance().getCurrentPlayer() != null) {
                try {
                    pokemon.Pokemon created = service.PetFactory.createPokemon(newPet);
                    if (created != null) {
                        GameDataManager.getInstance().getCurrentPlayer().addPet(created);
                    }
                } catch (Exception ex) {
                    System.err.println("将捕获的宠物加入Player失败: " + ex.getMessage());
                }
            }

            // 清理标记
            lastDefeatedEnemy = null;
            System.out.println("DEBUG: 捕获成功: " + enemy.getName());
            return true;
        } else {
            System.out.println("DEBUG: 捕获失败（未触发概率）");
        }
        return false;
    }
    public void exitBattle() {
        battleResult = BattleResult.ENEMY_WIN;
    }
    // Getters
    public Pokemon getCurrentPlayerPokemon() { return currentPlayerPokemon; }
    public Pokemon getCurrentEnemyPokemon() { return currentEnemyPokemon; }
    public BattleResult getBattleResult() { return battleResult; }
    public boolean isPlayerTurn() { return isPlayerTurn; }

    // 新增：获取最近被击败的敌人（用于界面显示HP=0）
    public Pokemon getLastDefeatedEnemy() { return lastDefeatedEnemy; }
    public Pokemon getLastDefeatedPlayer() { return lastDefeatedPlayer; }
}


