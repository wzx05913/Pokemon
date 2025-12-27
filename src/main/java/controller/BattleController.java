package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import pokemon.Move;
import pokemon.Pokemon;
import entity.Pet;
import entity.Bag;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

import battle.BattleManager;
import battle.BattleResult;
import battle.BattleStepResult;
import java.io.IOException;



import java.util.ArrayList;
public class BattleController {
    @FXML private Label playerPokemonName;
    @FXML private Label playerPokemonStats;
    @FXML private Label enemyPokemonName;
    @FXML private Label enemyPokemonStats;
    @FXML private VBox movesContainer;
    @FXML private Label battleLog;
    @FXML private Label battleResultLabel;
    
    private BattleManager battleManager;
    private List<Button> moveButtons = new ArrayList<>();
    private Parent root;
    private Consumer<Boolean> battleEndCallback;
    
    public BattleController(List<Pet> petList, Bag bag, Consumer<Boolean> callback) throws IOException {
        // 加载FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/battle.fxml"));  // 假设你的战斗界面FXML路径
        loader.setController(this);
        this.root = loader.load();
        this.battleEndCallback = callback;
        
        // 初始化战斗管理器
        // 初始化战斗管理器时使用排序后的宠物队列副本
        List<Pet> sortedPetList = new ArrayList<>(petList);
        sortedPetList.sort((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel())); // 从大到小排序
        this.battleManager = new BattleManager();
        this.battleManager.initBattle(petList);
        
        // 初始化UI
        initialize(battleManager);
    }
    
    public void initialize(BattleManager manager) {
        this.battleManager = manager;
        updateBattleUI();
        createMoveButtons();
        battleLog.setText("战斗开始！");
    }
    
    public Parent getRoot() {
        return root;
    }
    
    private void createMoveButtons() {
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) return;
        
        List<Move> moves = current.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            int index = i;
            
            Button button = new Button();
            button.setText(move.getName() + " (" + move.getPower() + "伤害, 消耗" + move.getPpCost() + "PP)");
            button.setOnAction(e -> onUseMove(index));
            
            moveButtons.add(button);
            movesContainer.getChildren().add(button);
        }
    }
    
    private void updateBattleUI() {
        Pokemon playerPokemon = battleManager.getCurrentPlayerPokemon();
        Pokemon enemyPokemon = battleManager.getCurrentEnemyPokemon();
        
        if (playerPokemon != null) {
            playerPokemonName.setText(playerPokemon.getName() + " Lv." + playerPokemon.getLevel());
            playerPokemonStats.setText(String.format("HP: %d/%d PP: %d/%d", 
                    playerPokemon.getHp(), playerPokemon.getMaxHp(),
                    playerPokemon.getPp(), playerPokemon.getMaxPp()));
        }
        
        if (enemyPokemon != null) {
            enemyPokemonName.setText("敌人的" + enemyPokemon.getName() + " Lv." + enemyPokemon.getLevel());
            enemyPokemonStats.setText(String.format("HP: %d/%d", 
                    enemyPokemon.getHp(), enemyPokemon.getMaxHp()));
        }
        
        // 更新按钮状态
        updateMoveButtons();
    }
    
    private void updateMoveButtons() {
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) return;
        
        List<Move> moves = current.getMoves();
        for (int i = 0; i < moveButtons.size() && i < moves.size(); i++) {
            Move move = moves.get(i);
            Button button = moveButtons.get(i);
            boolean canUse = current.getPp() >= move.getPpCost();
            button.setDisable(!canUse || !battleManager.isPlayerTurn());
        }
    }
    
    @FXML
    private void onUseMove(int moveIndex) {
        if (!battleManager.isPlayerTurn()) return;
        
        BattleStepResult result = battleManager.playerUseMove(moveIndex);
        battleLog.setText(result.getMessage());
        
        if (result.isSuccess()) {
            // 检查战斗是否结束
            if (battleManager.isBattleEnded()) {
                endBattle();
                return;
            }
            
            // 敌人回合
            BattleStepResult enemyResult = battleManager.enemyUseMove();
            battleLog.setText(battleLog.getText() + "\n" + enemyResult.getMessage());
            
            if (battleManager.isBattleEnded()) {
                endBattle();
                return;
            }
        }
        
        updateBattleUI();
    }
    
    private void endBattle() {
        // 禁用所有按钮
        moveButtons.forEach(btn -> btn.setDisable(true));
        
        // 显示结果
        if (battleManager.getBattleResult() == BattleResult.PLAYER_WIN) {
            battleResultLabel.setText("你赢了！获得了30金币！");
            
            // 尝试捕获敌人
            boolean caught = battleManager.tryCatchEnemy();
            if (caught) {
                battleResultLabel.setText(battleResultLabel.getText() + "\n成功捕获了敌人的宝可梦！");
            }
        } else {
            battleResultLabel.setText("你输了！");
        }
    }
 // 增加一个方法用于接收 MazeController 传来的数据
    public void setupBattle(List<Pet> petList, Consumer<Boolean> callback) {
        this.battleEndCallback = callback;
        this.battleManager = new BattleManager();
        this.battleManager.initBattle(petList);
        
        updateBattleUI();
        createMoveButtons();
        battleLog.setText("战斗开始！");
    }
    @FXML
    private void onExit(ActionEvent event) {
        // 关闭战斗窗口的逻辑
        movesContainer.getScene().getWindow().hide();
    }
}