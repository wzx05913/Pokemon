package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

import java.io.InputStream;

public class BattleController {
    @FXML private Label playerPokemonName;
    @FXML private Label playerPokemonStats;
    @FXML private Label enemyPokemonName;
    @FXML private Label enemyPokemonStats;
    @FXML private VBox movesContainer;
    @FXML private TextArea battleLog;
    @FXML private Label battleResultLabel;

    // 图片控件
    @FXML private ImageView playerImageView;
    @FXML private ImageView enemyImageView;

    private BattleManager battleManager;
    private List<Button> moveButtons = new ArrayList<>();
    private Parent root;
    private Consumer<Boolean> battleEndCallback;
    private boolean coinsAwarded = false; // 防止重复发放

    public BattleController() {}
    public void initData(List<Pet> petList, Bag bag, Consumer<Boolean> callback) {
        this.battleEndCallback = callback;
        this.battleManager = new BattleManager();
        this.battleManager.initBattle(petList);

        updateBattleUI();
        createMoveButtons();
        battleLog.appendText("战斗开始！");

        // 尝试自动推进（避免双方PP都为0时僵住）
        runAutoProgressLoop();
    }
    public BattleController(List<Pet> petList, Bag bag, Consumer<Boolean> callback) throws IOException {
        // 加载FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/battle.fxml"));  // 假设你的战斗界面FXML路径
        loader.setController(this);
        this.root = loader.load();
        this.battleEndCallback = callback;

        // 初始化战斗管理器
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

        // 自动推进检查
        runAutoProgressLoop();
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
            String label;
            if (move.getPower() > 0) {
                label = move.getName() + "（造成伤害，消耗" + move.getPpCost() + "PP）";
            } else {
                label = move.getName() + "（效果，消耗" + move.getPpCost() + "PP）";
            }
            button.setText(label);
            button.setOnAction(e -> onUseMove(index));

            moveButtons.add(button);
            // 直接添加，不再进行额外的父子检测（若你担心可使用 safeAddChild）
            movesContainer.getChildren().add(button);
        }

        // 添加普通攻击按钮（不消耗 PP）
        Button basicBtn = new Button("普通攻击（不消耗PP）");
        basicBtn.setOnAction(e -> onBasicAttack());
        moveButtons.add(basicBtn);
        movesContainer.getChildren().add(basicBtn);

    }

    @FXML
    private void onBasicAttack() {
        if (!battleManager.isPlayerTurn()) return;

        BattleStepResult res = battleManager.playerBasicAttack();
        if (res != null && res.getMessage() != null && !res.getMessage().isEmpty()) {
            battleLog.appendText(res.getMessage());
        }

        // 刷新 UI
        updateBattleUI();

        // 若战斗结束，结算
        if (battleManager.isBattleEnded()) {
            endBattle();
            updateBattleUI();
            return;
        }

        // 敌人立即回应（与普通技能流程一致）
        if (!battleManager.isPlayerTurn() && battleManager.getCurrentEnemyPokemon() != null) {
            BattleStepResult enemyRes = battleManager.enemyUseMove();
            if (enemyRes != null && enemyRes.getMessage() != null && !enemyRes.getMessage().isEmpty()) {
                battleLog.appendText("\n" + enemyRes.getMessage());
            }
            updateBattleUI();
            if (battleManager.isBattleEnded()) {
                endBattle();
                updateBattleUI();
                return;
            }
        }

        // 自动推进处理（PP回复/睡眠等）
        runAutoProgressLoop();

        updateBattleUI();
        if (battleManager.isBattleEnded()) {
            endBattle();
            updateBattleUI();
        }
    }

    private void updateBattleUI() {
        Pokemon playerPokemon = battleManager.getCurrentPlayerPokemon();
        Pokemon enemyPokemon = battleManager.getCurrentEnemyPokemon();

        // 玩家信息
        if (playerPokemon != null && !playerPokemon.isFainted()) {
            playerPokemonName.setText(playerPokemon.getName() + " Lv." + playerPokemon.getLevel());
            playerPokemonStats.setText(String.format("HP: %d/%d PP: %d/%d",
                    playerPokemon.getHp(), playerPokemon.getMaxHp(),
                    playerPokemon.getPp(), playerPokemon.getMaxPp()));
            // 显示活着的图片
            setPokemonImage(playerImageView, playerPokemon, false);
        } else if (playerPokemon != null && playerPokemon.isFainted()) {
            // 如果宠物死亡，显示死亡变体（存在时），否则隐藏
            playerPokemonName.setText(playerPokemon.getName() + " Lv." + playerPokemon.getLevel() + "（已战亡）");
            playerPokemonStats.setText(String.format("HP: %d/%d", 0, playerPokemon.getMaxHp()));
            setPokemonImage(playerImageView, playerPokemon, true);
            // 清空技能
            moveButtons.clear();
            movesContainer.getChildren().clear();
        } else {
            playerPokemonName.setText("无可用宠物");
            playerPokemonStats.setText("");
            playerImageView.setImage(null);
            playerImageView.setVisible(false);
            // 清空按钮/技能显示
            moveButtons.clear();
            movesContainer.getChildren().clear();
        }

        // 敌人信息
        if (enemyPokemon != null && !enemyPokemon.isFainted()) {
            enemyPokemonName.setText("敌人的" + enemyPokemon.getName() + " Lv." + enemyPokemon.getLevel());
            enemyPokemonStats.setText(String.format("HP: %d/%d",
                    enemyPokemon.getHp(), enemyPokemon.getMaxHp()));
            setPokemonImage(enemyImageView, enemyPokemon, false);
        } else {
            Pokemon last = battleManager.getLastDefeatedEnemy();
            if (last != null) {
                enemyPokemonName.setText("敌人的" + last.getName() + " Lv." + last.getLevel() + "（已被击败）");
                enemyPokemonStats.setText(String.format("HP: %d/%d", 0, last.getMaxHp()));
                setPokemonImage(enemyImageView, last, true);
            } else {
                enemyPokemonName.setText("无敌人");
                enemyPokemonStats.setText("");
                enemyImageView.setImage(null);
                enemyImageView.setVisible(false);
            }
        }

        // 如果当前玩家宠物发生变化（比如战死后换宠），保证按钮与当前宠物技能同步
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null || current.isFainted()) {
            // 清空按钮
            moveButtons.clear();
            movesContainer.getChildren().clear();
        } else {
            List<Move> moves = current.getMoves();
            // 这里考虑到我们始终在 moveButtons 中加入一个额外的普通攻击按钮
            int expectedButtonCount = moves.size() + 1; // +1 表示普通攻击按钮
            if (moveButtons.size() != expectedButtonCount) {
                // 技能数量变化：重建按钮（安全重建）
                createMoveButtons();
                // 构建后需要手动同步一次按钮状态
                updateMoveButtons();
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
        // 如果数量不一致，进行一次安全重建并返回（但重建后不要再次进入会递归的逻辑）
        int expectedButtonCount = moves.size() + 1; // +1 表示普通攻击按钮
        if (moveButtons.size() != expectedButtonCount) {
            createMoveButtons();
            // 重建后不在此处递归调用 updateMoveButtons()，caller 可再刷新一次
            return;
        }

        // 先更新技能按钮的文本/禁用状态（排除最后一个普通攻击按钮）
        for (int i = 0; i < moves.size() && i < moveButtons.size(); i++) {
            Move move = moves.get(i);
            Button button = moveButtons.get(i);
            String label;
            if (move.getPower() > 0) {
                label = move.getName() + "（造成伤害，消耗" + move.getPpCost() + "PP）";
            } else {
                label = move.getName() + "（效果，消耗" + move.getPpCost() + "PP）";
            }
            button.setText(label);
            boolean canUse = current.getPp() >= move.getPpCost();
            button.setDisable(!canUse || !battleManager.isPlayerTurn());
        }

        // 处理普通攻击按钮（最后一个）
        if (!moveButtons.isEmpty()) {
            Button basicBtn = moveButtons.get(moveButtons.size() - 1);
            basicBtn.setDisable(!battleManager.isPlayerTurn()); // 普通攻击不消耗 PP，仅受回合控制
        }
    }

    @FXML
    private void onUseMove(int moveIndex) {
        if (!battleManager.isPlayerTurn()) return;

        // 玩家出手
        BattleStepResult result = battleManager.playerUseMove(moveIndex);
        if (result != null && result.getMessage() != null && !result.getMessage().isEmpty()) {
            // 首次追加玩家出手信息
            battleLog.appendText(result.getMessage());
        }

        // 先更新界面，反映玩家消耗的 PP / 敌方被击等状态
        updateBattleUI();

        // 若玩家击败了敌人或战斗结束，则结算
        if (battleManager.isBattleEnded()) {
            endBattle();
            updateBattleUI();
            return;
        }

        // 如果现在轮到敌人（并且敌人存在），让敌人马上行动一次（这是修复关键）
        if (!battleManager.isPlayerTurn() && battleManager.getCurrentEnemyPokemon() != null) {
            BattleStepResult enemyResult = battleManager.enemyUseMove();
            if (enemyResult != null && enemyResult.getMessage() != null && !enemyResult.getMessage().isEmpty()) {
                battleLog.appendText("\n" + enemyResult.getMessage());
            }

            // 更新 UI（敌人动作后）
            updateBattleUI();

            if (battleManager.isBattleEnded()) {
                endBattle();
                updateBattleUI();
                return;
            }
        }

        // 再执行自动推进循环，处理 PP 恢复 / 睡眠 等需要自动处理的情况
        runAutoProgressLoop();

        // 最后再一次刷新 UI（保险）
        updateBattleUI();

        // 若战斗在自动推进过程中结束，处理结算
        if (battleManager.isBattleEnded()) {
            endBattle();
            updateBattleUI();
        }
    }

    // Run auto progress up to a small number of iterations to avoid infinite loops.
    private void runAutoProgressLoop() {
        if (battleManager.isBattleEnded()) return;

        for (int i = 0; i < 6; i++) {
            if (battleManager.isBattleEnded()) break;

            BattleStepResult autoRes = battleManager.handleAutoTurn();
            if (autoRes == null) break;

            battleLog.appendText("\n" + autoRes.getMessage());
            updateBattleUI();
        }
    }


    private void endBattle() {
        // 禁用所有按钮
        moveButtons.forEach(btn -> btn.setDisable(true));

        // 为保险，先重新判断战斗结束状态
        battleManager.isBattleEnded();

        // 显示结果
        if (battleManager.getBattleResult() == BattleResult.PLAYER_WIN) {
            battleResultLabel.setText("你赢了！获得了30金币！");
            if (!coinsAwarded) {
                GameDataManager.getInstance().addCoins(30);
                // 同步到 currentPlayer 的 money 显示
                if (GameDataManager.getInstance().getCurrentPlayer() != null) {
                    Integer coins = GameDataManager.getInstance().getPlayerBag() != null ?
                            GameDataManager.getInstance().getPlayerBag().getCoins() : null;
                    if (coins != null) {
                        GameDataManager.getInstance().getCurrentPlayer().setMoney(coins);
                    }
                }
                coinsAwarded = true;
            }
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
        battleLog.appendText("战斗开始！");

        runAutoProgressLoop();
    }
    @FXML
    private void onExit(ActionEvent event) {
        if (!battleManager.isBattleEnded()) {
            battleManager.exitBattle();
            System.out.println("中途退出，判定为战败");
        }

        if (battleEndCallback != null) {
            boolean isWin = (battleManager.getBattleResult() == BattleResult.PLAYER_WIN);
            battleEndCallback.accept(isWin);
        }

        movesContainer.getScene().getWindow().hide();
    }

    // Helper: 根据宝可梦名称加载图片并设置到 ImageView
    private void setPokemonImage(ImageView iv, Pokemon p, boolean allowDeadVariant) {
        if (p == null) {
            iv.setImage(null);
            iv.setVisible(false);
            return;
        }

        String baseName = p.getName();
        Image img = null;
        if (p.isFainted() && allowDeadVariant) {
            InputStream isDead = getClass().getResourceAsStream("/images/" + baseName + "_dead.png");
            if (isDead != null) {
                img = new Image(isDead);
            }
        }

        if (img == null) {
            InputStream is = getClass().getResourceAsStream("/images/" + baseName + ".png");
            if (is != null) img = new Image(is);
        }

        if (img != null) {
            iv.setImage(img);
            iv.setVisible(true);
        } else {
            iv.setImage(null);
            iv.setVisible(false);
        }
    }
}