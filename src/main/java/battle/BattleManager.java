package battle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import Music.BgMusicManager;
import pokemon.Pokemon;
import pokemon.PokemonFactory;
import pokemon.PokemonType;
import entity.Pet;
import pokemon.Bulbasaur;
import pokemon.Charmander;
import pokemon.Jigglypuff;
import pokemon.Pikachu;
import pokemon.Psyduck;
import pokemon.Squirtle;
import pokemon.Move;
import service.GameDataManager;
import service.PetFactory;
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
    private Pokemon lastDefeatedEnemy;

    //初始化战斗
    public void initBattle(List<Pet> petList) {
        if (BgMusicManager.isMusicEnabled()) BgMusicManager.getInstance().playSceneMusic("battle");
        playerQueue = new LinkedList<>();
        List<Pokemon> playerPokemons = petList.stream()
                .filter(pet -> Boolean.TRUE.equals(pet.getAlive()))
                .map(PokemonFactory::createPokemon)
                .filter(p -> p != null && p.isAlive())
                .sorted((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel()))
                .collect(Collectors.toList());

        playerQueue.addAll(playerPokemons);
        initEnemy(playerPokemons);

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

        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);

        isPlayerTurn = true;
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());
        playerQueue.forEach(p -> p.enterBattle());
        enemyQueue.forEach(p -> p.enterBattle());
    }

    private void initEnemy(List<Pokemon> playerPokemons) {
        enemyQueue = new LinkedList<>();

        int maxPlayerLevel = playerPokemons.stream()
                .mapToInt(Pokemon::getLevel)
                .max()
                .orElse(5);

        int enemyCount = (int) Math.ceil(playerPokemons.size() / 3.0);

        enemyCount = Math.max(1, enemyCount);

        PokemonType[] allTypes = PokemonType.values();

        List<PokemonType> selectedTypes = new ArrayList<>();
        for (int i = 0; i < enemyCount; i++) {
            // 随机选择类型
            PokemonType randomType;
            int attempts = 0;
            do {
                randomType = allTypes[random.nextInt(allTypes.length)];
                attempts++;
            } while (selectedTypes.contains(randomType) && attempts < 10);

            selectedTypes.add(randomType);

            Pokemon enemy = createEnemyPokemon(randomType.name(), maxPlayerLevel+random.nextInt(3)+1);
            if (enemy != null) {
                enemyQueue.add(enemy);
            } else {
                enemyQueue.add(new Pikachu(maxPlayerLevel+random.nextInt(3)+1));
            }
        }

        System.out.println("DEBUG: 生成了 " + enemyQueue.size() + " 个敌人，等级为 " + maxPlayerLevel + " 级");
    }

    //创建敌人宝可梦
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
    //移除队列头部所有已濒死的宝可梦（避免死的回到队列）
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

        if (currentPlayerPokemon.isPpDepleted()) {
            currentPlayerPokemon.recoverPpEachTurn(15); // 每次跳过回复10 PP
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
            message = currentPlayerPokemon.getName() + " 使用了 " + move.getName() + "，造成了 " + result + " 点伤害。";
            if (currentPlayerPokemon.wasLastCritical()) {
                message += "（暴击！）";
            }
        }

        if (currentEnemyPokemon != null && currentEnemyPokemon.isFainted()) {
            message += "\n敌人的" + currentEnemyPokemon.getName() + " 被击败了！";
            lastDefeatedEnemy = currentEnemyPokemon;
            enemyQueue.poll();
            currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());
        }

        //更新引用（不把未死亡的玩家宝可梦移出队列）
        removeFaintedFromQueue(enemyQueue);
        removeFaintedFromQueue(playerQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        //玩家动作完成，切换到敌人回合
        isPlayerTurn = false;
        return new BattleStepResult(true, message);
    }

    public BattleStepResult enemyUseMove() {
        if (isPlayerTurn || currentEnemyPokemon == null) {
            return new BattleStepResult(false, "不是敌人的回合");
        }

        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        if (currentEnemyPokemon == null) {
            return new BattleStepResult(false, "无敌人");
        }

        if (currentEnemyPokemon.isAsleep()) {
            currentEnemyPokemon.tickAsleep();
            isPlayerTurn = true;
            return new BattleStepResult(true, currentEnemyPokemon.getName() + " 正在睡眠，无法行动。");
        }

        if (currentEnemyPokemon.isPpDepleted()) {
            currentEnemyPokemon.recoverPpEachTurn(15); // 每回合回复10 PP
            BattleStepResult basicAttackResult = enemyBasicAttack();
            String message = "";
            if (basicAttackResult != null && basicAttackResult.getMessage() != null && !basicAttackResult.getMessage().isEmpty()) {
                message = basicAttackResult.getMessage();
            }
            return new BattleStepResult(true, message);
        }

        List<Move> moves = currentEnemyPokemon.getMoves();
        Move bestMove = null;

        for (Move move : moves) {
            if (currentEnemyPokemon.getPp() >= move.getPpCost() &&
                    (bestMove == null || move.getPower() > bestMove.getPower())) {
                bestMove = move;
            }
        }

        if (bestMove == null) {
            currentEnemyPokemon.recoverPpEachTurn(15);
            BattleStepResult basicAttackResult = enemyBasicAttack();
            String message = "";
            if (basicAttackResult != null && basicAttackResult.getMessage() != null && !basicAttackResult.getMessage().isEmpty()) {
                message = basicAttackResult.getMessage();
            }
            return new BattleStepResult(true, message);
        }

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

        if (currentPlayerPokemon != null && currentPlayerPokemon.isFainted()) {
            message += "\n你的" + currentPlayerPokemon.getName() + " 战死了！";
            playerQueue.poll();
            currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        } else {
        }

        if (currentEnemyPokemon != null && currentEnemyPokemon.isFainted()) {
            lastDefeatedEnemy = currentEnemyPokemon;
            enemyQueue.poll();
            currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());
        } else {
        }

        if (currentPlayerPokemon != null) {
            currentPlayerPokemon.recoverPpEachTurn(15);
        }

        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        currentEnemyPokemon = (enemyQueue == null ? null : enemyQueue.peek());

        isPlayerTurn = true;
        return new BattleStepResult(true, message);
    }

    public BattleStepResult handleAutoTurn() {
        //如果战斗已结束，不做事
        if (isBattleEnded()) {
            return null;
        }

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
                //切换到敌人回合并尝试敌人行动（如果敌人有动作会自动执行）
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

        int userId = GameDataManager.getInstance().getCurrentUserId();
        boolean isGuest = (userId == -1);

        if (random.nextDouble() <= 0.5) {
            try {
                Pet newPet = PetFactory.createPetEntity(userId, enemy);

                int newPetId = generateNewPetId();
                // 使用反射设置ID，因为Pet类可能没有setId方法
                setPetId(newPet, newPetId);

                List<Pet> petList = GameDataManager.getInstance().getPetList();
                if (petList != null) {
                    petList.add(newPet);
                    System.out.println("DEBUG: 新宠物已添加到列表，当前宠物数量: " + petList.size());
                }

                if (GameDataManager.getInstance().getCurrentPlayer() != null) {
                    Pokemon created = PetFactory.createPokemon(newPet);
                    if (created != null) {
                        GameDataManager.getInstance().getCurrentPlayer().addPet(created);

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

    //生成新的宠物ID
    private int generateNewPetId() {
        List<Pet> petList = GameDataManager.getInstance().getPetList();
        if (petList == null || petList.isEmpty()) {
            return 1;
        }

        int newId = petList.size() + 1;
        System.out.println("DEBUG: 生成新宠物ID: " + newId + " (基于列表大小: " + petList.size() + ")");
        return newId;
    }

    //使用反射设置Pet的ID（因为Pet类可能没有setId方法）
    private void setPetId(Pet pet, int id) {
        try {
            //先尝试setId方法
            try {
                Method setIdMethod = pet.getClass().getMethod("setId", int.class);
                setIdMethod.invoke(pet, id);
                return;
            } catch (NoSuchMethodException e) {
                try {
                    Field idField = pet.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(pet, id);
                    return;
                } catch (NoSuchFieldException e2) {
                    try {
                        Field petIdField = pet.getClass().getDeclaredField("petId");
                        petIdField.setAccessible(true);
                        petIdField.set(pet, id);
                        return;
                    } catch (NoSuchFieldException e3) {
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

        //若处于睡眠或其他状态阻止行动，处理跳过（与 useMove 一致）
        if (currentPlayerPokemon.isAsleep()) {
            currentPlayerPokemon.tickAsleep();
            isPlayerTurn = false;
            return new BattleStepResult(true, currentPlayerPokemon.getName() + " 正在睡眠，无法行动。");
        }

        int damage = currentPlayerPokemon.basicAttack(currentEnemyPokemon);
        String message = "\n"+currentPlayerPokemon.getName() + " 使用了普通攻击，造成了 " + damage + " 点伤害。";

        if (currentEnemyPokemon.isFainted()) {
            message += "\n敌人的" + currentEnemyPokemon.getName() + " 被击败了！";
            lastDefeatedEnemy = currentEnemyPokemon;
            enemyQueue.poll();
        }

        removeFaintedFromQueue(enemyQueue);
        removeFaintedFromQueue(playerQueue);
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();

        isPlayerTurn = false;
        return new BattleStepResult(true, message);
    }

    public BattleStepResult enemyBasicAttack(){
        removeFaintedFromQueue(playerQueue);
        removeFaintedFromQueue(enemyQueue);
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();
        if (currentEnemyPokemon.isAsleep()) {
            currentEnemyPokemon.tickAsleep();
            isPlayerTurn = true;
            return new BattleStepResult(true, currentEnemyPokemon.getName() + " 正在睡眠，无法行动。");
        }
        int damage = currentEnemyPokemon.basicAttack(currentPlayerPokemon);
        String Message = "敌人的" + currentEnemyPokemon.getName() + " 使用了普通攻击，造成了 " + damage + " 点伤害。";
        if (currentPlayerPokemon != null && currentPlayerPokemon.isFainted()) {
            Message = "你的" + currentPlayerPokemon.getName() + " 战死了！";
            playerQueue.poll();
            currentPlayerPokemon = (playerQueue == null ? null : playerQueue.peek());
        }
        removeFaintedFromQueue(enemyQueue);
        removeFaintedFromQueue(playerQueue);
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();
        isPlayerTurn = true;
        return new BattleStepResult(true, Message);
    }

    public void exitBattle() {
        battleResult = BattleResult.ENEMY_WIN;
        //清理 inBattle 标记（把队列中的所有 Pokemon 恢复为非战斗）
        if (playerQueue != null) playerQueue.forEach(p -> p.exitBattle());
        if (enemyQueue != null) enemyQueue.forEach(p -> p.exitBattle());
        if (lastDefeatedEnemy != null) lastDefeatedEnemy.exitBattle();
    }
    // Getters
    public Pokemon getCurrentPlayerPokemon() { return currentPlayerPokemon; }
    public Pokemon getCurrentEnemyPokemon() { return currentEnemyPokemon; }
    public BattleResult getBattleResult() { return battleResult; }
    public boolean isPlayerTurn() { return isPlayerTurn; }

    public Pokemon getLastDefeatedEnemy() { return lastDefeatedEnemy; }
}