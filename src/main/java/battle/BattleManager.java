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
    private Queue<Pokemon> playerQueue;
    private Queue<Pokemon> enemyQueue;
    private Pokemon currentPlayerPokemon;
    private Pokemon currentEnemyPokemon;
    private boolean isPlayerTurn;
    private Random random = new Random();
    private BattleResult battleResult;

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
        Pokemon enemy = createEnemyPokemon(randomType, enemyLevel);
        enemyQueue.add(enemy);
    }
    
    // 创建敌人宝可梦
    private Pokemon createEnemyPokemon(PokemonType type, int level) {
        switch (type) {
            case BULBASAUR:
                return new Bulbasaur(level);
            case CHARMANDER:
                return new Charmander(level);
            case SQUIRTLE:
                return new Squirtle(level);
            case PIKACHU:
                return new Pikachu(level);
            case JIGGLYPUFF:
                return new Jigglypuff(level);
            case PSYDUCK:
                return new Psyduck(level);
            default:
                return new Pikachu(level); // 默认皮卡丘
        }
    }
    
    // 玩家使用技能
    public BattleStepResult playerUseMove(int moveIndex) {
        if (!isPlayerTurn || currentPlayerPokemon == null) {
            return new BattleStepResult(false, "不是你的回合");
        }
        
        List<Move> moves = currentPlayerPokemon.getMoves();
        if (moveIndex < 0 || moveIndex >= moves.size()) {
            return new BattleStepResult(false, "无效的技能");
        }
        
        Move move = moves.get(moveIndex);
        if (currentPlayerPokemon.getPp() < move.getPpCost()) {
            return new BattleStepResult(false, "PP不足，无法使用技能");
        }
        
        // 使用技能
        currentPlayerPokemon.useMove(moveIndex, currentEnemyPokemon);
        String message = currentPlayerPokemon.getName() + "使用了" + move.getName() 
                + "，造成了" + (currentEnemyPokemon.getMaxHp() - currentEnemyPokemon.getHp()) + "点伤害";
        
        // 检查敌人是否被击败
        if (currentEnemyPokemon.isFainted()) {
            message += "\n敌人的" + currentEnemyPokemon.getName() + "被击败了！";
            enemyQueue.poll();
        }
        
        isPlayerTurn = false;
        return new BattleStepResult(true, message);
    }
    
    // 敌人使用技能
    public BattleStepResult enemyUseMove() {
        if (isPlayerTurn || currentEnemyPokemon == null) {
            return new BattleStepResult(false, "不是敌人的回合");
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
            // 没有可用技能
            isPlayerTurn = true;
            return new BattleStepResult(true, "敌人没有可用技能了");
        }
        
        // 使用技能
        int moveIndex = moves.indexOf(bestMove);
        currentEnemyPokemon.useMove(moveIndex, currentPlayerPokemon);
        String message = "敌人的" + currentEnemyPokemon.getName() + "使用了" + bestMove.getName()
                + "，造成了" + (currentPlayerPokemon.getMaxHp() - currentPlayerPokemon.getHp()) + "点伤害";
        
        // 检查玩家宠物是否被击败
        if (currentPlayerPokemon.isFainted()) {
            message += "\n你的" + currentPlayerPokemon.getName() + "战死了！";
            playerQueue.poll();
        } else if (currentPlayerPokemon.isPpDepleted()) {
            message += "\n你的" + currentPlayerPokemon.getName() + "累死了！";
            playerQueue.poll();
        } else {
            // 移到队尾
            playerQueue.poll();
            playerQueue.add(currentPlayerPokemon);
        }
        
        // 敌人移到队尾
        if (!currentEnemyPokemon.isFainted() && !currentEnemyPokemon.isPpDepleted()) {
            enemyQueue.poll();
            enemyQueue.add(currentEnemyPokemon);
        }
        
        // 更新当前战斗宠物
        currentPlayerPokemon = playerQueue.peek();
        currentEnemyPokemon = enemyQueue.peek();
        
        isPlayerTurn = true;
        return new BattleStepResult(true, message);
    }
    
    // 检查战斗是否结束
    public boolean isBattleEnded() {
        boolean playerHasValid = playerQueue.stream().anyMatch(p -> !p.isFainted() && !p.isPpDepleted());
        boolean enemyHasValid = enemyQueue.stream().anyMatch(p -> !p.isFainted() && !p.isPpDepleted());
        
        if (!playerHasValid) {
            battleResult = BattleResult.ENEMY_WIN;
            return true;
        }
        
        if (!enemyHasValid) {
            battleResult = BattleResult.PLAYER_WIN;
            // 增加金币
            GameDataManager.getInstance().setCoins(GameDataManager.getInstance().getPlayerBag().getCoins()+30);
            return true;
        }
        
        return false;
    }
    
    // 尝试捕获敌人
    public boolean tryCatchEnemy() {
    	if (battleResult != BattleResult.PLAYER_WIN || enemyQueue.isEmpty()) {
            return false;
        }
        
        // 30%概率捕获
        if (random.nextDouble() <= 0.3) {
            // 创建与敌人同类型同等级的新宠物
            Pokemon enemy = enemyQueue.peek();
            Pet newPet = new Pet();
            
            // 设置新宠物属性
            newPet.setUserId(GameDataManager.getInstance().getCurUser());
            newPet.setName(enemy.getName());
            newPet.setType(PokemonType.fromString(enemy.getName()).getEnglishName());
            newPet.setLevel(enemy.getLevel());
            newPet.setAttack(enemy.getAttack());
            newPet.setExperience(0);
            newPet.setAlive(true);
            
            // 添加到全局管理类而非SessionManager
            GameDataManager.getInstance().addPet(newPet);

            return true;
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
}


