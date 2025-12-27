package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;
import javafx.scene.layout.Priority;

public class BattleController {
    @FXML private Label playerPokemonName;
    @FXML private Label playerPokemonStats;
    @FXML private Label enemyPokemonName;
    @FXML private Label enemyPokemonStats;
    @FXML private VBox movesContainer;       // 改为 VBox，垂直排列
    @FXML private TextArea battleLog;
    @FXML private Label battleResultLabel;
    @FXML private ImageView playerImageView;
    @FXML private ImageView enemyImageView;

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
        appendLog("战斗开始！");
    }

    public void initialize(BattleManager manager) {
        this.battleManager = manager;
        updateBattleUI();
        createMoveButtons();
        appendLog("战斗开始！");
    }

    // 纵向创建技能按钮
    private void createMoveButtons() {
        moveButtons.clear();
        movesContainer.getChildren().clear();

        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) return;

        List<Move> moves = current.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            final int index = i;

            Button button = new Button();
            button.setText(move.getName() + " (" + move.getPower() + ")");
            button.setMaxWidth(Double.MAX_VALUE); // 填满容器宽度，纵向更美观
            button.setOnAction(e -> onUseMove(index));

            moveButtons.add(button);
            movesContainer.getChildren().add(button);
            VBox.setVgrow(button, Priority.NEVER);
        }

        updateMoveButtons();
    }

    private void updateBattleUI() {
        Pokemon playerPokemon = battleManager.getCurrentPlayerPokemon();
        Pokemon enemyPokemon = battleManager.getCurrentEnemyPokemon();
        Pokemon lastDefPlayer = battleManager.getLastDefeatedPlayer(); // 需在 BattleManager 添加 getter
        if (playerPokemon != null) {
            playerPokemonName.setText(playerPokemon.getName() + " Lv." + playerPokemon.getLevel());
            playerPokemonStats.setText(String.format("HP: %d/%d   PP: %d/%d",
                    playerPokemon.getHp(), playerPokemon.getMaxHp(),
                    playerPokemon.getPp(), playerPokemon.getMaxPp()));
            loadImageToView(playerPokemon.getName(), playerImageView);
        } else if (lastDefPlayer != null) {
            playerPokemonName.setText(lastDefPlayer.getName() + " Lv." + lastDefPlayer.getLevel() + "（已被击败）");
            playerPokemonStats.setText(String.format("HP: %d/%d", 0, lastDefPlayer.getMaxHp()));
            loadImageToView(lastDefPlayer.getName(), playerImageView);
        } else {
            playerPokemonName.setText("无可用宠物");
            playerPokemonStats.setText("");
            playerImageView.setImage(null);
        }

        if (enemyPokemon != null) {
            enemyPokemonName.setText("敌人的 " + enemyPokemon.getName() + " Lv." + enemyPokemon.getLevel());
            enemyPokemonStats.setText(String.format("HP: %d/%d",
                    enemyPokemon.getHp(), enemyPokemon.getMaxHp()));
            loadImageToView(enemyPokemon.getName(), enemyImageView);
        } else {
            Pokemon last = battleManager.getLastDefeatedEnemy();
            if (last != null) {
                enemyPokemonName.setText("敌人的 " + last.getName() + "（已被击败）");
                enemyPokemonStats.setText(String.format("HP: %d/%d", 0, last.getMaxHp()));
                loadImageToView(last.getName(), enemyImageView);
            } else {
                enemyPokemonName.setText("无敌人");
                enemyPokemonStats.setText("");
                enemyImageView.setImage(null);
            }
        }

        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) {
            moveButtons.clear();
            movesContainer.getChildren().clear();
        } else {
            List<Move> moves = current.getMoves();
            if (moveButtons.size() != moves.size()) {
                createMoveButtons();
            } else {
                updateMoveButtons();
            }
        }
    }

    private void updateMoveButtons() {
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) return;

        List<Move> moves = current.getMoves();
        for (int i = 0; i < moveButtons.size() && i < moves.size(); i++) {
            Move move = moves.get(i);
            Button button = moveButtons.get(i);
            button.setText(move.getName() + " (" + move.getPower() + ")");
            boolean canUse = current.getPp() >= move.getPpCost();
            button.setDisable(!canUse || !battleManager.isPlayerTurn());
        }
    }

    // 把日志追加和换行独立成方法，统一格式
    private void appendLog(String text) {
        if (text == null || text.isEmpty()) return;
        if (battleLog.getText().isEmpty()) {
            battleLog.appendText(text);
        } else {
            battleLog.appendText("\n" + text);
        }
    }

    @FXML
    private void onUseMove(int moveIndex) {
        if (!battleManager.isPlayerTurn()) return;

        BattleStepResult result = battleManager.playerUseMove(moveIndex);
        appendLog(result.getMessage());

        // 立刻刷新 UI，确保 HP / 图片 / 被击败状态能在日志前端看到
        updateBattleUI();

        if (result.isSuccess()) {
            // 如果战斗结束（玩家胜利或失败），直接处理结束逻辑
            if (battleManager.isBattleEnded()) {
                endBattle();
                return;
            }

            // 如果刚击败了敌人，跳过敌人本回合的反击：
            if (battleManager.wasEnemyJustDefeated()) {
                // 不调用 enemyUseMove()，而是推进到下一只敌人并清除标志
                battleManager.advanceAfterDefeat();
                // 刷新 UI，显示下一个敌人或继续
                updateBattleUI();
                // 如果推进后战斗结束（没有更多敌人），处理结束
                if (battleManager.isBattleEnded()) {
                    endBattle();
                }
                return;
            }

            // 正常情形：敌人回合
            BattleStepResult enemyResult = battleManager.enemyUseMove();
            if (enemyResult != null && enemyResult.getMessage() != null) {
                appendLog(enemyResult.getMessage());
            }

            // 刷新界面显示敌人动作带来的变化
            updateBattleUI();

            if (battleManager.isBattleEnded()) {
                endBattle();
                return;
            }
        }

        // 最后再次刷新（正常回合结束）
        updateBattleUI();
    }

    private void endBattle() {
        moveButtons.forEach(btn -> btn.setDisable(true));

        if (battleManager.getBattleResult() == BattleResult.PLAYER_WIN) {
            battleResultLabel.setText("你赢了！获得了30金币！");
            GameDataManager.getInstance().addCoins(30);
            boolean caught = battleManager.tryCatchEnemy();
            if (caught) {
                appendLog("成功捕获了敌人的宝可梦！");
                battleResultLabel.setText(battleResultLabel.getText() + "\n成功捕获了敌人的宝可梦！");
            }
        } else {
            battleResultLabel.setText("你输了！");
        }
    }

    // 加载图片到 ImageView，约定资源路径 /images/<name>.png
    private void loadImageToView(String name, ImageView view) {
        if (name == null || view == null) return;
        String path = "/images/" + name + ".png";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) {
                Image img = new Image(is);
                view.setImage(img);
            } else {
                view.setImage(null);
                System.err.println("DEBUG: 找不到图片: " + path);
            }
        } catch (Exception ex) {
            view.setImage(null);
            System.err.println("加载图片失败: " + ex.getMessage());
        }
    }

    @FXML
    private void onExit(ActionEvent event) {
        if (!battleManager.isBattleEnded()) {
            battleManager.exitBattle();
        }
        if (battleEndCallback != null) {
            boolean isWin = (battleManager.getBattleResult() == BattleResult.PLAYER_WIN);
            battleEndCallback.accept(isWin);
        }
        if (movesContainer != null && movesContainer.getScene() != null) {
            movesContainer.getScene().getWindow().hide();
        }
    }

    // 对外可用的初始化接口（另一种入口）
    public void setupBattle(List<Pet> petList, Consumer<Boolean> callback) {
        this.battleEndCallback = callback;
        this.battleManager = new BattleManager();
        this.battleManager.initBattle(petList);

        updateBattleUI();
        createMoveButtons();
        appendLog("战斗开始！");
    }
}