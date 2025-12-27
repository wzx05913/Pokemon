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
import service.GameDataManager;
import battle.BattleManager;
import battle.BattleResult;
import battle.BattleStepResult;
import java.io.IOException;
import javafx.scene.control.TextArea;


import java.util.ArrayList;
public class BattleController {
    @FXML private Label playerPokemonName;
    @FXML private Label playerPokemonStats;
    @FXML private Label enemyPokemonName;
    @FXML private Label enemyPokemonStats;
    @FXML private VBox movesContainer;
    @FXML private TextArea battleLog;
    @FXML private Label battleResultLabel;

    private BattleManager battleManager;
    private List<Button> moveButtons = new ArrayList<>();
    private Parent root;
    private Consumer<Boolean> battleEndCallback;

    public BattleController() {}
    public void initData(List<Pet> petList, Bag bag, Consumer<Boolean> callback) {
        this.battleEndCallback = callback;
        this.battleManager = new BattleManager();
        this.battleManager.initBattle(petList);

        updateBattleUI();
        createMoveButtons();
        battleLog.appendText("战斗开始！");
    }
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
        battleLog.appendText("战斗开始！");
    }

    public Parent getRoot() {
        return root;
    }

    private void createMoveButtons() {
        // 清理旧按钮（防止切换宠物时重复）
        moveButtons.clear();
        movesContainer.getChildren().clear();

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

        // 更新按钮状态（可用/不可用）
        updateMoveButtons();
    }

    private void updateBattleUI() {
        Pokemon playerPokemon = battleManager.getCurrentPlayerPokemon();
        Pokemon enemyPokemon = battleManager.getCurrentEnemyPokemon();

        if (playerPokemon != null) {
            playerPokemonName.setText(playerPokemon.getName() + " Lv." + playerPokemon.getLevel());
            playerPokemonStats.setText(String.format("HP: %d/%d PP: %d/%d",
                    playerPokemon.getHp(), playerPokemon.getMaxHp(),
                    playerPokemon.getPp(), playerPokemon.getMaxPp()));
        } else {
            playerPokemonName.setText("无可用宠物");
            playerPokemonStats.setText("");
        }

        if (enemyPokemon != null) {
            enemyPokemonName.setText("敌人的" + enemyPokemon.getName() + " Lv." + enemyPokemon.getLevel());
            enemyPokemonStats.setText(String.format("HP: %d/%d",
                    enemyPokemon.getHp(), enemyPokemon.getMaxHp()));
        } else {
            // 当 currentEnemyPokemon 为 null 时，尝试显示最近被击败的敌人（HP:0）
            Pokemon last = battleManager.getLastDefeatedEnemy();
            if (last != null) {
                enemyPokemonName.setText("敌人的" + last.getName() + " Lv." + last.getLevel() + "（已被击败）");
                enemyPokemonStats.setText(String.format("HP: %d/%d", 0, last.getMaxHp()));
            } else {
                enemyPokemonName.setText("无敌人");
                enemyPokemonStats.setText("");
            }
        }

        // 如果当前玩家宠物发生变化（比如战死后换宠），保证按钮与当前宠物技能同步
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) {
            // 清空按钮
            moveButtons.clear();
            movesContainer.getChildren().clear();
        } else {
            List<Move> moves = current.getMoves();
            if (moveButtons.size() != moves.size()) {
                // 技能数量变化：重建按钮
                createMoveButtons();
            } else {
                // 刷新按钮文本和状态
                updateMoveButtons();
            }
        }
    }

    private void updateMoveButtons() {
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) return;

        List<Move> moves = current.getMoves();
        // 若数量不一致，重建更稳妥（防止 index mismatch）
        if (moveButtons.size() != moves.size()) {
            createMoveButtons();
            return;
        }

        for (int i = 0; i < moveButtons.size() && i < moves.size(); i++) {
            Move move = moves.get(i);
            Button button = moveButtons.get(i);
            // 同步按钮文本（以防止仍显示旧技能名称）
            button.setText(move.getName() + " (" + move.getPower() + "伤害, 消耗" + move.getPpCost() + "PP)");
            boolean canUse = current.getPp() >= move.getPpCost();
            button.setDisable(!canUse || !battleManager.isPlayerTurn());
        }
    }

    @FXML
    private void onUseMove(int moveIndex) {
        if (!battleManager.isPlayerTurn()) return;

        BattleStepResult result = battleManager.playerUseMove(moveIndex);
        battleLog.appendText(result.getMessage());

        if (result.isSuccess()) {
            // 检查战斗是否结束
            if (battleManager.isBattleEnded()) {
                endBattle();
                // 更新 UI（确保显示被击败的敌人 HP）
                updateBattleUI();
                return;
            }

            // 敌人回合
            BattleStepResult enemyResult = battleManager.enemyUseMove();
            // 只追加敌人回合的信息（避免重复把已有文本再写一次）
            battleLog.appendText("\n" + enemyResult.getMessage());

            if (battleManager.isBattleEnded()) {
                endBattle();
                updateBattleUI();
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
            GameDataManager.getInstance().addCoins(30);
            // 尝试捕获敌人
            boolean caught = battleManager.tryCatchEnemy();
            if (caught) {
                battleResultLabel.setText(battleResultLabel.getText() + "\n成功捕获了敌人的宝可梦！");
            }
        } else {
            battleResultLabel.setText("你输了！");
        }

        boolean caught = battleManager.tryCatchEnemy();
        if (caught) {
            battleResultLabel.setText(battleResultLabel.getText() + "\n成功捕获了敌人的宝可梦！");
        }
    }
    // 增加一个方法用于接收 MazeController 传来的数据
    public void setupBattle(List<Pet> petList, Consumer<Boolean> callback) {
        this.battleEndCallback = callback;
        this.battleManager = new BattleManager();
        this.battleManager.initBattle(petList);

        updateBattleUI();
        createMoveButtons();
        battleLog.appendText("战斗开始！");
    }
    @FXML
    private void onExit(ActionEvent event) {
        // 1. 如果战斗还没结束，算作中途退出，设为战败
        if (!battleManager.isBattleEnded()) {
            battleManager.exitBattle();
            System.out.println("中途退出，判定为战败");
        }

        // 2. 执行回调（通知父窗口战斗结果）
        if (battleEndCallback != null) {
            // 将当前的最终胜负状态传回（可能是赢了，也可能是中途退出的输）
            boolean isWin = (battleManager.getBattleResult() == BattleResult.PLAYER_WIN);
            battleEndCallback.accept(isWin);
        }

        // 3. 关闭当前窗口
        movesContainer.getScene().getWindow().hide();
    }
}