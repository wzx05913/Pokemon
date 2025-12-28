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

public class BattleManager {
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
                // 只使用数据库/实体中标记为存活的宠物
                .filter(pet -> java.lang.Boolean.TRUE.equals(pet.getAlive()))
                .map(PokemonFactory::createPokemon)
                .filter(p -> p != null && p.isAlive())
                .sorted((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel()))
                .collect(Collectors.toList());

        playerQueue.addAll(playerPokemons);

        // 初始化敌人
        initEnemy(playerPokemons);

        // 战斗开始时所有宠物满状态
        if (playerQueue != null) {
            playerQueue.forEach(p -> {
                p.fullHeal();
                p.setPp(p.getMaxPp());
            });
        }
        if (enemyQueue != null) {
            enemyQueue.forEach(p -> {
                p.fullHeal();
                p.setPp(p.getMaxPp());
            });
        }

        // 清理队列中可能已濒死的（保险）
        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);

        isPlayerTurn = true;
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());
        playerQueue.forEach(p -> p.enterBattle());
        enemyQueue.forEach(p -> p.enterBattle());
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

    // Helper：移除队列头部所有已濒死的宝可梦（避免死的回到队列）
    private void removeFaintedFromQueue(Queue<Pokemon> q) {
        if (q == null) return;
        while (!q.isEmpty() && q.peek().isFainted()) {
            q.poll();
        }
    }

    public BattleStepResult playerUseMove(int moveIndex) {
        if (!isPlayerTurn || currentPlayerPokemon == null) {
            return new BattleStepResult(false, "不是你的回合");
        }

        // 清理队列前端已濒死的（保险）
        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        if (currentPlayerPokemon == null) {
            return new BattleStepResult(false, "无可用宠物");
        }

        // 如果自己处于睡眠，跳过并减少睡眠回合
        if (currentPlayerPokemon.isAsleep()) {
            currentPlayerPokemon.tickAsleep();
            isPlayerTurn = false;
            return new BattleStepResult(true, currentPlayerPokemon.getName() + " 正在睡眠，无法行动。");
        }

        // 如果玩家当前精灵没有可用 PP，跳过并恢复少量 PP（不会死亡）
        if (currentPlayerPokemon.isPpDepleted()) {
            currentPlayerPokemon.recoverPpEachTurn(10); // 每次跳过回复10 PP
            isPlayerTurn = false;
            return new BattleStepResult(true, "你的 " + currentPlayerPokemon.getName() + " PP 不足，本回合回复了少量 PP。");
        }

        List<Move> moves = currentPlayerPokemon.getMoves();
        if (moveIndex < 0 || moveIndex >= moves.size()) {
            return new BattleStepResult(false, "无效的技能");
        }

        Move move = moves.get(moveIndex);
        if (currentPlayerPokemon.getPp() < move.getPpCost()) {
            return new BattleStepResult(false, "PP不足，无法使用技能");
        }

        // 使用技能（返回实际造成的伤害或状态结果）
        int result = currentPlayerPokemon.useMove(moveIndex, currentEnemyPokemon);
        String message;
        if (result == -1) {
            message = currentPlayerPokemon.getName() + " 无法使用 " + move.getName() + "（被状态或PP阻止）";
        } else if (result == 0) {
            if ("叫声".equals(move.getName())) {
                message = currentPlayerPokemon.getName() + " 使用了 " + move.getName() + "，降低了敌方的攻击！";
            } else if ("唱歌".equals(move.getName()) || "唱".equals(move.getName())) {
                message = currentPlayerPokemon.getName() + " 使用了 " + move.getName() + "，敌方陷入了一回合睡眠！";
            } else if ("生长".equals(move.getName())) {
                message = currentPlayerPokemon.getName() + " 使用了 生长，提升了自身攻击！";
            } else if ("缩入壳中".equals(move.getName())) {
                message = currentPlayerPokemon.getName() + " 使用了 缩入壳中，提升了自身防御！";
            } else {
                message = currentPlayerPokemon.getName() + " 使用了 " + move.getName() + "，技能效果生效！";
            }
        } else {
            // result 为造成的伤害数值，战斗日志显示具体数字
            message = currentPlayerPokemon.getName() + " 使用了 " + move.getName() + "，造成了 " + result + " 点伤害。";
            if (currentPlayerPokemon.wasLastCritical()) {
                message += "（暴击！）";
            }
        }

        // 检查敌人是否被击败（只有 HP <= 0 才出队）
        if (currentEnemyPokemon != null && currentEnemyPokemon.isFainted()) {
            message += "\n敌人的" + currentEnemyPokemon.getName() + " 被击败了！";
            lastDefeatedEnemy = currentEnemyPokemon;
            enemyQueue.poll();
            currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());
        }

        // 在玩家行动后，让敌方回复少量 PP（防止双方 PP 都为0 时卡住）
        if (currentEnemyPokemon != null) {
            currentEnemyPokemon.recoverPpEachTurn(10); // 敌人回合前先回复一点 PP
        }

        // 更新引用（不把未死亡的玩家宝可梦移出队列）
        removeFaintedFromQueue(enemyQueue);
        removeFaintedFromQueue(playerQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        // 玩家动作完成，切换到敌人回合
        isPlayerTurn = false;
        return new BattleStepResult(true, message);
    }

    // 敌人使用技能
    public BattleStepResult enemyUseMove() {
        if (isPlayerTurn || currentEnemyPokemon == null) {
            return new BattleStepResult(false, "不是敌人的回合");
        }

        // 清理队列并刷新引用
        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        if (currentEnemyPokemon == null) {
            return new BattleStepResult(false, "无敌人");
        }

        // 如果敌人处于睡眠，跳过并减少睡眠回合
        if (currentEnemyPokemon.isAsleep()) {
            currentEnemyPokemon.tickAsleep();
            isPlayerTurn = true;
            return new BattleStepResult(true, currentEnemyPokemon.getName() + " 正在睡眠，无法行动。");
        }

        // 若敌人没有任何可用 PP（isPpDepleted），则本回合跳过并恢复少量 PP（不会死亡）
        if (currentEnemyPokemon.isPpDepleted()) {
            currentEnemyPokemon.recoverPpEachTurn(10); // 每回合回复10 PP
            isPlayerTurn = true;
            return new BattleStepResult(true, "敌人的" + currentEnemyPokemon.getName() + " PP 不足，正在回复 PP。");
        }

        // 选择伤害最高的可用技能
        List<Move> moves = currentEnemyPokemon.getMoves();
        Move bestMove = null;

        for (Move move : moves) {
            if (currentEnemyPokemon.getPp() >= move.getPpCost() &&
                    (bestMove == null || move.getPower() > bestMove.getPower())) {
                bestMove = move;
            }
        }

        if (bestMove == null) {
            // 没有可用技能（安全兜底）
            currentEnemyPokemon.recoverPpEachTurn(10);
            isPlayerTurn = true;
            return new BattleStepResult(true, "敌人没有可用技能，本回合回复 PP。");
        }

        // 使用技能
        int moveIndex = moves.indexOf(bestMove);
        int result = currentEnemyPokemon.useMove(moveIndex, currentPlayerPokemon);
        String message;
        if (result == -1) {
            message = "敌人的" + currentEnemyPokemon.getName() + " 无法使用 " + bestMove.getName();
        } else if (result == 0) {
            if ("叫声".equals(bestMove.getName())) {
                message = "敌人的" + currentEnemyPokemon.getName() + " 使用了 " + bestMove.getName() + "，降低了你的攻击！";
            } else if ("唱歌".equals(bestMove.getName()) || "唱".equals(bestMove.getName())) {
                message = "敌人的" + currentEnemyPokemon.getName() + " 使用了 " + bestMove.getName() + "，你进入睡眠！";
            } else if ("生长".equals(bestMove.getName())) {
                message = "敌人的" + currentEnemyPokemon.getName() + " 使用了 生长，提升了自身攻击！";
            } else if ("缩入壳中".equals(bestMove.getName())) {
                message = "敌人的" + currentEnemyPokemon.getName() + " 使用了 缩入壳中，提升了自身防御！";
            } else {
                message = "敌人的" + currentEnemyPokemon.getName() + " 使用了 " + bestMove.getName() + "（效果生效）";
            }
        } else {
            // 包含具体伤害数字
            message = "敌人的" + currentEnemyPokemon.getName() + " 使用了 " + bestMove.getName() + "，造成了 " + result + " 点伤害。";
            if (currentEnemyPokemon.wasLastCritical()) {
                message += "（暴击！）";
            }
        }

        // 检查玩家宠物是否被击败（只有 HP <= 0 才出队）
        if (currentPlayerPokemon != null && currentPlayerPokemon.isFainted()) {
            message += "\n你的" + currentPlayerPokemon.getName() + " 战死了！";
            playerQueue.poll();
            currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        } else {
            // 注意：不把未死亡的玩家宝可梦移出队列（不要轮转）
            // 保持 currentPlayerPokemon 为队首
        }

        // 检查敌人自己是否被击败（极少发生，但保留）
        if (currentEnemyPokemon != null && currentEnemyPokemon.isFainted()) {
            lastDefeatedEnemy = currentEnemyPokemon;
            enemyQueue.poll();
            currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());
        } else {
            // 不轮转敌人
        }

        // 在敌人行动后，让玩家回复少量 PP
        if (currentPlayerPokemon != null) {
            currentPlayerPokemon.recoverPpEachTurn(10);
        }

        // 清理并更新引用
        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        // 切回玩家回合（前提：战斗未结束）
        isPlayerTurn = true;
        return new BattleStepResult(true, message);
    }

    /**
     * 自动推进：当玩家回合但玩家无法行动（睡眠或PP耗尽）时自动跳过并在需要时立即触发敌方回合；
     * 或当敌人回合并敌人也无法行动时处理回复。 控制器可调用该方法来在 UI 更新后让战斗"自动走一步"。
     * 返回非空 BattleStepResult 表示有要追加到日志的消息。
     */
    public BattleStepResult handleAutoTurn() {
        // 如果战斗已结束，不做事
        if (isBattleEnded()) {
            return null;
        }

        // 保证引用最新
        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        if (isPlayerTurn) {
            if (currentPlayerPokemon == null) return null;
            if (currentPlayerPokemon.isAsleep()) {
                currentPlayerPokemon.tickAsleep();
                isPlayerTurn = false;
                return new BattleStepResult(true, currentPlayerPokemon.getName() + " 正在睡眠，无法行动。");
            }
            if (currentPlayerPokemon.isPpDepleted()) {
                currentPlayerPokemon.recoverPpEachTurn(10);
                // 切换到敌人回合并尝试敌人行动（如果敌人有动作会自动执行）
                isPlayerTurn = false;
                BattleStepResult enemyRes = enemyUseMove();
                String msg = "你的 " + currentPlayerPokemon.getName() + " PP 不足，本回合恢复了一些 PP。";
                if (enemyRes != null && enemyRes.getMessage() != null && !enemyRes.getMessage().isEmpty()) {
                    msg += "\n" + enemyRes.getMessage();
                }
                return new BattleStepResult(true, msg);
            }
        } else { // 敌人回合
            if (currentEnemyPokemon == null) return null;
            if (currentEnemyPokemon.isAsleep()) {
                currentEnemyPokemon.tickAsleep();
                isPlayerTurn = true;
                return new BattleStepResult(true, currentEnemyPokemon.getName() + " 正在睡眠，无法行动。");
            }
            if (currentEnemyPokemon.isPpDepleted()) {
                currentEnemyPokemon.recoverPpEachTurn(10);
                isPlayerTurn = true;
                return new BattleStepResult(true, "敌人的" + currentEnemyPokemon.getName() + " PP 不足，本回合回复了一些 PP。");
            }
        }
        return null;
    }

    // 检查战斗是否结束（仅基于 HP）
    public boolean isBattleEnded() {
        boolean playerHasAlive = playerQueue != null && playerQueue.stream().anyMatch(p -> !p.isFainted());
        boolean enemyHasAlive = enemyQueue != null && enemyQueue.stream().anyMatch(p -> !p.isFainted());

        if (!playerHasAlive) {
            battleResult = BattleResult.ENEMY_WIN;
            return true;
        }

        if (!enemyHasAlive) {
            battleResult = BattleResult.PLAYER_WIN;
            // 胜利状态由调用方（例如 BattleController.endBattle）负责结算与奖励，
            // 以避免在多处调用 isBattleEnded() 时重复发放金币。
            return true;
        }

        return false;
    }

    public boolean tryCatchEnemy() {
        if (battleResult != BattleResult.PLAYER_WIN) {
            return false;
        }

        Pokemon enemy = lastDefeatedEnemy != null ? lastDefeatedEnemy : currentEnemyPokemon;
        if (enemy == null) return false;

        // 检查是否为游客账号（userid=-1），如果是游客账号也允许捕获
        int userId = GameDataManager.getInstance().getCurrentUserId();
        boolean isGuest = (userId == -1);

        if (random.nextDouble() <= 1) {
            try {
                // 创建宠物实体（但不保存到数据库）
                entity.Pet newPet = service.PetFactory.createPetEntity(userId, enemy);

                // 生成正数ID（模拟数据库自增）
                int newPetId = generateNewPetId();
                // 使用反射设置ID，因为Pet类可能没有setId方法
                setPetId(newPet, newPetId);

                // 添加到内存中的宠物列表
                List<Pet> petList = GameDataManager.getInstance().getPetList();
                if (petList != null) {
                    petList.add(newPet);
                    System.out.println("DEBUG: 新宠物已添加到列表，当前宠物数量: " + petList.size());
                }

                // 创建Pokemon对象并添加到当前玩家的内存数据中
                if (GameDataManager.getInstance().getCurrentPlayer() != null) {
                    pokemon.Pokemon created = service.PetFactory.createPokemon(newPet);
                    if (created != null) {
                        GameDataManager.getInstance().getCurrentPlayer().addPet(created);

                        // 添加到全局的pokemonList（内存中）
                        try {
                            GameDataManager.getInstance().addPokemon(created);
                            System.out.println("DEBUG: 新Pokemon已添加到玩家和全局列表");
                        } catch (Exception ex) {
                            System.err.println("将捕获的宠物加入全局 pokemonList 失败: " + ex.getMessage());
                        }
                    }
                }

                lastDefeatedEnemy = null;
                System.out.println("DEBUG: 捕获成功: " + enemy.getName() + " (ID: " + newPetId + ")" + (isGuest ? " (游客账号)" : ""));
                return true;
            } catch (Exception ex) {
                System.err.println("捕获宠物时发生错误: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        } else {
            System.out.println("DEBUG: 捕获失败（未触发概率）");
        }
        return false;
    }

    // 生成新的宠物ID（基于现有宠物列表的大小）
    private int generateNewPetId() {
        List<Pet> petList = GameDataManager.getInstance().getPetList();
        if (petList == null || petList.isEmpty()) {
            return 1; // 第一个宠物
        }

        // 简单的基于列表大小的ID生成
        int newId = petList.size() + 1;
        System.out.println("DEBUG: 生成新宠物ID: " + newId + " (基于列表大小: " + petList.size() + ")");
        return newId;
    }

    // 使用反射设置Pet的ID（因为Pet类可能没有setId方法）
    private void setPetId(Pet pet, int id) {
        try {
            // 先尝试setId方法
            try {
                java.lang.reflect.Method setIdMethod = pet.getClass().getMethod("setId", int.class);
                setIdMethod.invoke(pet, id);
                return;
            } catch (NoSuchMethodException e) {
                // 如果setId方法不存在，尝试直接设置id字段
                try {
                    java.lang.reflect.Field idField = pet.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(pet, id);
                    return;
                } catch (NoSuchFieldException e2) {
                    // 如果id字段也不存在，尝试petId字段
                    try {
                        java.lang.reflect.Field petIdField = pet.getClass().getDeclaredField("petId");
                        petIdField.setAccessible(true);
                        petIdField.set(pet, id);
                        return;
                    } catch (NoSuchFieldException e3) {
                        // 如果都没有，记录警告但继续执行
                        System.out.println("WARN: Pet类没有找到id或petId字段，无法设置ID");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("设置宠物ID时发生错误: " + e.getMessage());
        }
    }

    public BattleStepResult playerBasicAttack() {
        if (!isPlayerTurn || currentPlayerPokemon == null) {
            return new BattleStepResult(false, "不是你的回合");
        }

        // 清理并刷新引用
        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();

        if (currentPlayerPokemon == null) {
            return new BattleStepResult(false, "无可用宠物");
        }
        if (currentEnemyPokemon == null) {
            return new BattleStepResult(false, "无敌人");
        }

        // 若处于睡眠或其他状态阻止行动，处理跳过（与 useMove 一致）
        if (currentPlayerPokemon.isAsleep()) {
            currentPlayerPokemon.tickAsleep();
            isPlayerTurn = false;
            return new BattleStepResult(true, currentPlayerPokemon.getName() + " 正在睡眠，无法行动。");
        }

        int damage = currentPlayerPokemon.basicAttack(currentEnemyPokemon);
        String message = currentPlayerPokemon.getName() + " 使用了普通攻击，造成了 " + damage + " 点伤害。";

        // 检查敌人是否被击败（只有 HP <= 0 才出队）
        if (currentEnemyPokemon.isFainted()) {
            message += "\n敌人的" + currentEnemyPokemon.getName() + " 被击败了！";
            lastDefeatedEnemy = currentEnemyPokemon;
            enemyQueue.poll();
        }

        // 在玩家行动后，让敌方回复少量 PP（防止双方 PP 都为0 时卡住）
        if (currentEnemyPokemon != null) {
            currentEnemyPokemon.recoverPpEachTurn(10);
        }

        // 更新引用并切换回合
        removeFaintedFromQueue(enemyQueue);
        removeFaintedFromQueue(playerQueue);
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();

        isPlayerTurn = false;
        return new BattleStepResult(true, message);
    }

    public void exitBattle() {
        battleResult = BattleResult.ENEMY_WIN;
        // 清理 inBattle 标记（把队列中的所有 Pokemon 恢复为非战斗）
        if (playerQueue != null) playerQueue.forEach(p -> p.exitBattle());
        if (enemyQueue != null) enemyQueue.forEach(p -> p.exitBattle());
        if (lastDefeatedEnemy != null) lastDefeatedEnemy.exitBattle();
    }
    // Getters
    public Pokemon getCurrentPlayerPokemon() { return currentPlayerPokemon; }
    public Pokemon getCurrentEnemyPokemon() { return currentEnemyPokemon; }
    public BattleResult getBattleResult() { return battleResult; }
    public boolean isPlayerTurn() { return isPlayerTurn; }

    // 新增：获取最近被击败的敌人（用于界面显示HP=0）
    public Pokemon getLastDefeatedEnemy() { return lastDefeatedEnemy; }
}