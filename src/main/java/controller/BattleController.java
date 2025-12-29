package controller;

import Player.Player;
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

    //图片
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

        runAutoProgressLoop();
    }
    public BattleController(List<Pet> petList, Bag bag, Consumer<Boolean> callback) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/battle.fxml"));  // 假设你的战斗界面FXML路径
        loader.setController(this);
        this.root = loader.load();
        this.battleEndCallback = callback;

        List<Pet> sortedPetList = new ArrayList<>(petList);
        sortedPetList.sort((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel())); // 从大到小排序
        this.battleManager = new BattleManager();
        this.battleManager.initBattle(petList);

        initialize(battleManager);
    }

    public void initialize(BattleManager manager) {
        this.battleManager = manager;
        updateBattleUI();
        createMoveButtons();
        battleLog.appendText("战斗开始！");
        //自动推进检查
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
            movesContainer.getChildren().add(button);
        }

        //添加普通攻击按钮
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

        updateBattleUI();

        if (battleManager.isBattleEnded()) {
            endBattle();
            updateBattleUI();
            return;
        }

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

        //玩家信息
        if (playerPokemon != null && !playerPokemon.isFainted()) {
            playerPokemonName.setText(playerPokemon.getName() + " Lv." + playerPokemon.getLevel());
            playerPokemonStats.setText(String.format("HP: %d/%d PP: %d/%d",
                    playerPokemon.getHp(), playerPokemon.getMaxHp(),
                    playerPokemon.getPp(), playerPokemon.getMaxPp()));
            setPokemonImage(playerImageView, playerPokemon, false);
        } else if (playerPokemon != null && playerPokemon.isFainted()) {
            playerPokemonName.setText(playerPokemon.getName() + " Lv." + playerPokemon.getLevel() + "（已战亡）");
            playerPokemonStats.setText(String.format("HP: %d/%d", 0, playerPokemon.getMaxHp()));
            setPokemonImage(playerImageView, playerPokemon, true);
            moveButtons.clear();
            movesContainer.getChildren().clear();
        } else {
            playerPokemonName.setText("无可用宠物");
            playerPokemonStats.setText("");
            playerImageView.setImage(null);
            playerImageView.setVisible(false);
            moveButtons.clear();
            movesContainer.getChildren().clear();
        }

        //敌人信息
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

        //如果当前玩家宠物发生变化保证按钮与当前宠物技能同步
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null || current.isFainted()) {
            //清空按钮
            moveButtons.clear();
            movesContainer.getChildren().clear();
        } else {
            List<Move> moves = current.getMoves();
            int expectedButtonCount = moves.size() + 1;
            if (moveButtons.size() != expectedButtonCount) {
                createMoveButtons();
                updateMoveButtons();
            } else {
                updateMoveButtons();
            }
        }
    }

    private void updateMoveButtons() {
        Pokemon current = battleManager.getCurrentPlayerPokemon();
        if (current == null) return;

        List<Move> moves = current.getMoves();
        int expectedButtonCount = moves.size() + 1;
        if (moveButtons.size() != expectedButtonCount) {
            createMoveButtons();
            return;
        }

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

        //处理普通攻击按钮（最后一个）
        if (!moveButtons.isEmpty()) {
            Button basicBtn = moveButtons.get(moveButtons.size() - 1);
            basicBtn.setDisable(!battleManager.isPlayerTurn()); // 普通攻击不消耗 PP，仅受回合控制
        }
    }

    @FXML
    private void onUseMove(int moveIndex) {
        if (!battleManager.isPlayerTurn()) return;

        //玩家出手
        BattleStepResult result = battleManager.playerUseMove(moveIndex);
        if (result != null && result.getMessage() != null && !result.getMessage().isEmpty()) {
            battleLog.appendText("\n"+result.getMessage());
        }

        updateBattleUI();

        if (battleManager.isBattleEnded()) {
            endBattle();
            updateBattleUI();
            return;
        }

        if (!battleManager.isPlayerTurn() && battleManager.getCurrentEnemyPokemon() != null) {
            BattleStepResult enemyResult = battleManager.enemyUseMove();
            if (enemyResult != null && enemyResult.getMessage() != null && !enemyResult.getMessage().isEmpty()) {
                battleLog.appendText("\n" + enemyResult.getMessage());
            }

            updateBattleUI();

            if (battleManager.isBattleEnded()) {
                endBattle();
                updateBattleUI();
                return;
            }
        }

        runAutoProgressLoop();
        updateBattleUI();

        //若战斗在自动推进过程中结束，处理结算
        if (battleManager.isBattleEnded()) {
            endBattle();
            updateBattleUI();
        }
    }

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
        moveButtons.forEach(btn -> btn.setDisable(true));

        battleManager.isBattleEnded();

        if (battleManager.getBattleResult() == BattleResult.PLAYER_WIN) {
            battleResultLabel.setText("你赢了！获得了30金币！");
            if (!coinsAwarded) {
                try {
                    Bag playerBag = GameDataManager.getInstance().getPlayerBag();
                    if (playerBag == null) {
                        int userId = GameDataManager.getInstance().getCurrentUserId();
                        playerBag = new Bag(userId);
                        playerBag.setCoins(0);
                        GameDataManager.getInstance().setCurrentBag(playerBag);
                        System.out.println("DEBUG: 创建了新Bag");
                    }

                    Integer coins = playerBag.getCoins();
                    if (coins == null) {
                        coins = 0;
                        playerBag.setCoins(coins);
                        System.out.println("DEBUG: 初始化coins为0");
                    }
                    GameDataManager.getInstance().addCoins(30);
                    System.out.println("DEBUG: 成功添加30金币，当前金币: " + playerBag.getCoins());

                    Player currentPlayer = GameDataManager.getInstance().getCurrentPlayer();
                    if (currentPlayer != null) {
                        Integer updatedCoins = GameDataManager.getInstance().getPlayerBag().getCoins();
                        currentPlayer.setMoney(updatedCoins != null ? updatedCoins : 0);
                        System.out.println("DEBUG: 同步金币到玩家显示");
                    }

                } catch (Exception e) {
                    System.err.println("发放金币时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
                coinsAwarded = true;
            }

            boolean caught = battleManager.tryCatchEnemy();
            if (caught) {
                battleResultLabel.setText(battleResultLabel.getText() + "\n成功捕获了敌人的宝可梦！");
            } else {
                battleResultLabel.setText(battleResultLabel.getText() + "\n捕获失败！");
            }
        } else {
            battleResultLabel.setText("你输了！");
        }
    }

    //接收 MazeController 传来的数据
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
        Music.BgMusicManager.getInstance().playSceneMusic("maze");
        movesContainer.getScene().getWindow().hide();
    }

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