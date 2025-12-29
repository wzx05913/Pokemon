// src/main/java/controller/MazeController.java
package controller;

import Music.BgMusicManager;
import entity.Bag;
import entity.Pet;
import core.Maze;
import core.Point;
import Player.MazePlayer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import service.GameDataManager;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class MazeController {
    @FXML
    private Canvas canvas;

    private Maze maze;
    private MazePlayer player;
    private GraphicsContext gc;
    private int cellSize;
    private Stage primaryStage;
    private int coinsAtExplorationStart = 0;

    @FXML
    public void initialize() {
        if (Music.BgMusicManager.isMusicEnabled())  Music.BgMusicManager.getInstance().playSceneMusic("maze");
        maze = new Maze();
        player = new MazePlayer(maze.getStart());
        gc = canvas.getGraphicsContext2D();

        //计算单元格大小
        cellSize = Math.max(4, (int) (Math.min(canvas.getWidth(), canvas.getHeight()) / maze.getSize()));

        drawMaze();

        //记录探索开始时的金币数
        try {
            Integer coins = GameDataManager.getInstance().getPlayerBag() != null ?
                    GameDataManager.getInstance().getPlayerBag().getCoins() : null;
            coinsAtExplorationStart = coins != null ? coins : 0;
        } catch (Exception e) {
            coinsAtExplorationStart = 0;
        }

        //键盘事件
        canvas.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            handleMovement(code);
        });

        canvas.setFocusTraversable(true);
        canvas.requestFocus();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void onExitExploration(ActionEvent event) {
        try {
            if (this.primaryStage != null) {
                java.net.URL resource = getClass().getResource("/bedroom-select.fxml");
                if (resource == null) {
                    System.err.println("无法找到 /bedroom-select.fxml");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                Parent bedroomRoot = loader.load();
                BedroomSelectController bedroomController = loader.getController();

                //确保设置主控制器
                if (mainController != null) {
                    bedroomController.setMainController(mainController);
                }

                this.primaryStage.setScene(new Scene(bedroomRoot, 800, 600));
                this.primaryStage.centerOnScreen();
                this.primaryStage.show();
                if (Music.BgMusicManager.isMusicEnabled())  Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
            } else {
                //如果没有传入 Stage，直接关闭所在窗口
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("返回卧室失败：" + e.getMessage());
        }
    }

    private void handleMovement(KeyCode code) {
        int newX = player.getX();
        int newY = player.getY();

        switch (code) {
            case UP:    newX = player.getX() - 1; break;
            case DOWN:  newX = player.getX() + 1; break;
            case LEFT:  newY = player.getY() - 1; break;
            case RIGHT: newY = player.getY() + 1; break;
            default: return;
        }

        if (!maze.isWall(newX, newY)) {
            player.setPosition(newX, newY);

            //如果到达遇敌点（Maze 中的 treasure），触发战斗并清除该点
            if (maze.isTreasure(newX, newY)) {
                maze.getGrid()[newX][newY] = 0;
                drawMaze();
                openFightingWindow();
            }

            if (maze.isEnd(newX, newY)) {
                goBackToBedroom();
                return;
            }

            drawMaze();
        }
    }

    private void drawMaze() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        gc.clearRect(0, 0, width, height);

        int size = maze.getSize();
        // Recompute cellSize in case canvas resized
        cellSize = Math.max(4, (int) (Math.min(width, height) / size));

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double x = j * cellSize;
                double y = i * cellSize;

                if (maze.isWall(i, j)) {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(x, y, cellSize, cellSize);
                } else {
                    //地面
                    gc.setFill(Color.LIGHTGRAY);
                    gc.fillRect(x, y, cellSize, cellSize);
                }

                //终点
                if (maze.isEnd(i, j)) {
                    gc.setFill(Color.GREEN);
                    gc.fillRect(x, y, cellSize, cellSize);
                }

                if (maze.isTreasure(i, j)) {
                    gc.setFill(Color.YELLOW);
                    double cx = x + cellSize / 2.0;
                    double cy = y + cellSize / 2.0;
                    double r = Math.max(6, cellSize * 0.5);
                    gc.fillOval(cx - r/2, cy - r/2, r, r);
                }
            }
        }

        int px = player.getX();
        int py = player.getY();
        if (px >= 0 && py >= 0 && px < size && py < size) {
            double pxX = py * cellSize + cellSize / 2.0;
            double pxY = px * cellSize + cellSize / 2.0;
            double pr = Math.max(8, cellSize * 0.6);
            gc.setFill(Color.RED);
            gc.fillOval(pxX - pr/2, pxY - pr/2, pr, pr);
        }
    }

    private void openFightingWindow() {
        try {
            java.net.URL url = getClass().getResource("/BattleView.fxml");
            if (url == null) {
                System.err.println("ERROR: /battle.fxml not found on classpath. Check src/main/resources and build output (target/classes).");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent battleRoot = loader.load();
            BattleController battleController = loader.getController();

            List<Pet> petList = new ArrayList<>(GameDataManager.getInstance().getPetList());
            Stage dialog = new Stage();
            dialog.initOwner(this.primaryStage != null ? this.primaryStage : (Stage) canvas.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setTitle("战斗");

            Scene scene = new Scene(battleRoot, 800, 600);
            dialog.setScene(scene);
            dialog.centerOnScreen();

            battleController.setupBattle(petList, (Boolean win) -> drawMaze());

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法打开战斗界面：" + e.getMessage());
        }
    }
    private MainController mainController;
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void goBackToBedroom() {
        try {
            if (this.primaryStage != null) {
                java.net.URL resource = getClass().getResource("/bedroom-select.fxml");
                if (resource == null) {
                    System.err.println("找不到卧室页面资源(bedroom-select.fxml)。");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                Parent bedroomRoot = loader.load();
                BedroomSelectController bedroomController = loader.getController();

                if (mainController != null) {
                    bedroomController.setMainController(mainController);
                } else {
                    System.err.println("Warning: mainController is null when returning from maze.");
                }

                try {
                    int currentCoins = GameDataManager.getInstance().getPlayerBag() != null ?
                            (GameDataManager.getInstance().getPlayerBag().getCoins() != null ?
                                    GameDataManager.getInstance().getPlayerBag().getCoins() : 0) : 0;
                    int explorationGain = currentCoins - coinsAtExplorationStart;
                    GameDataManager.getInstance().addCoins(200);
                    if (GameDataManager.getInstance().getCurrentPlayer() != null) {
                        Integer afterCoins = GameDataManager.getInstance().getPlayerBag() != null ?
                                GameDataManager.getInstance().getPlayerBag().getCoins() : null;
                        if (afterCoins != null) GameDataManager.getInstance().getCurrentPlayer().setMoney(afterCoins);
                    }
                    showAlert("恭喜探索完成！", "探索获得：" + explorationGain + "个金币，抵达终点额外奖励200金币！");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                this.primaryStage.setScene(new Scene(bedroomRoot, 800, 600));
                this.primaryStage.centerOnScreen();
                this.primaryStage.show();

                Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
            } else {
                Stage stage = (Stage) canvas.getScene().getWindow();
                stage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("返回卧室失败：" + e.getMessage());
        }
    }

    //显示信息提示
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setPlayerPosition(int x, int y) {
        player.setPosition(x, y);
        drawMaze();
    }
}