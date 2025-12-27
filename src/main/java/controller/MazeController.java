package controller;

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

/**
 * 迷宫游戏控制器（优化版）
 * - 添加“退出探索”按钮（onExitExploration）
 * - 添加右侧图示（legend）
 * - 在画布上绘制玩家（红点）和敌人（黄点），遇敌点来自 Maze 的 treasure 标记
 */
public class MazeController {
    @FXML
    private Canvas canvas;

    private Maze maze;
    private MazePlayer player;
    private GraphicsContext gc;
    private int cellSize;
    private Stage primaryStage; // 由调用者（BedroomSelectController）传入同一个 Stage

    @FXML
    public void initialize() {
        maze = new Maze();
        player = new MazePlayer(maze.getStart());
        gc = canvas.getGraphicsContext2D();

        // 计算单元格大小，根据当前画布尺寸动态计算
        cellSize = Math.max(4, (int) (Math.min(canvas.getWidth(), canvas.getHeight()) / maze.getSize()));

        drawMaze();

        // 键盘事件监听（在 canvas 上）
        canvas.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            handleMovement(code);
        });

        canvas.setFocusTraversable(true);
        canvas.requestFocus();
    }

    // 供外部注入 Stage（BedroomSelectController 会传入当前 Stage）
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // 退出探索（回到卧室/bedroom-select）
    @FXML
    private void onExitExploration(ActionEvent event) {
        try {
            // 如果有 primaryStage，加载 bedroom-select 并设置回去
            if (this.primaryStage != null) {
                java.net.URL resource = getClass().getResource("/bedroom-select.fxml");
                if (resource == null) {
                    System.err.println("无法找到 /bedroom-select.fxml");
                    showAlert("错误", "找不到卧室页面资源(bedroom-select.fxml)。");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                Parent bedroomRoot = loader.load();

                // 如果需要，可以把 MainController / BedroomSelectController 相关引用注入
                this.primaryStage.setScene(new Scene(bedroomRoot, 800, 600));
                this.primaryStage.centerOnScreen();
                this.primaryStage.show();
            } else {
                // 如果没有传入 Stage，直接关闭所在窗口
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("错误", "返回卧室失败：" + e.getMessage());
        }
    }

    // 处理移动并触发重绘/事件
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

            // 如果到达遇敌点（Maze 中的 treasure），触发战斗并清除该点
            if (maze.isTreasure(newX, newY)) {
                // 把该点清空，防止重复触发
                maze.getGrid()[newX][newY] = 0;
                drawMaze(); // 先刷新迷宫显示（把该黄点清掉）
                openFightingWindow();
            }

            // 到达终点
            if (maze.isEnd(newX, newY)) {
                goBackToBedroom();
                return;
            }

            drawMaze();
        }
    }

    // 绘制迷宫、遇敌点（黄色圆点）、玩家（红色圆点）、终点（绿色格）
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
                    // 地面
                    gc.setFill(Color.LIGHTGRAY);
                    gc.fillRect(x, y, cellSize, cellSize);
                }

                // 终点
                if (maze.isEnd(i, j)) {
                    gc.setFill(Color.GREEN);
                    gc.fillRect(x, y, cellSize, cellSize);
                }

                // 遇敌点（原来的 treasure）绘制为黄色圆点
                if (maze.isTreasure(i, j)) {
                    gc.setFill(Color.YELLOW);
                    double cx = x + cellSize / 2.0;
                    double cy = y + cellSize / 2.0;
                    double r = Math.max(3, cellSize * 0.2);
                    gc.fillOval(cx - r/2, cy - r/2, r, r);
                }
            }
        }

        // 绘制玩家（红点）在其当前格子中心
        int px = player.getX();
        int py = player.getY();
        if (px >= 0 && py >= 0 && px < size && py < size) {
            double pxX = py * cellSize + cellSize / 2.0;
            double pxY = px * cellSize + cellSize / 2.0;
            double pr = Math.max(5, cellSize * 0.35);
            gc.setFill(Color.RED);
            gc.fillOval(pxX - pr/2, pxY - pr/2, pr, pr);
        }

        // 可选：绘制网格线（美观）
        gc.setStroke(Color.gray(0.8));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= size; i++) {
            gc.strokeLine(0, i * cellSize, size * cellSize, i * cellSize);
            gc.strokeLine(i * cellSize, 0, i * cellSize, size * cellSize);
        }
    }

    // 打开战斗窗口（保持原有逻辑：加载 Battle 界面并传入玩家宠物）
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
            // 如果需要从 Player 构造 PetList，这里补充逻辑

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

    // 到达终点回到卧室（与退出逻辑一致）
    private void goBackToBedroom() {
        try {
            if (this.primaryStage != null) {
                java.net.URL resource = getClass().getResource("/bedroom-select.fxml");
                if (resource == null) {
                    showAlert("错误", "找不到卧室页面资源(bedroom-select.fxml)。");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                Parent bedroomRoot = loader.load();
                this.primaryStage.setScene(new Scene(bedroomRoot, 800, 600));
                this.primaryStage.centerOnScreen();
                this.primaryStage.show();
            } else {
                // 无 primaryStage 时弹提示并关闭当前窗口
                Stage stage = (Stage) canvas.getScene().getWindow();
                stage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("错误", "返回卧室失败：" + e.getMessage());
        }
    }

    // 辅助：显示信息提示
    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 供测试或外部使用：可手动设置玩家位置
    public void setPlayerPosition(int x, int y) {
        player.setPosition(x, y);
        drawMaze();
    }
}