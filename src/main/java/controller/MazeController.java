// src/main/java/controller/MazeController.java
package controller;
import service.GameDataManager;
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
import java.lang.Exception; 
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 迷宫游戏控制器
 */
public class MazeController {
    @FXML
    private Canvas canvas;

    private Maze maze;
    private MazePlayer player;
    private GraphicsContext gc;
    private int cellSize;
    private Stage primaryStage;

    @FXML
    public void initialize() {
        maze = new Maze();
        player = new MazePlayer(maze.getStart());
        gc = canvas.getGraphicsContext2D();
        
        // 计算单元格大小
        cellSize = (int) (Math.min(canvas.getWidth(), canvas.getHeight()) / maze.getSize());
        
        // 绘制初始迷宫
        drawMaze();
        
        // 键盘事件监听
        canvas.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            handleMovement(code);
        });
        
        // 确保画布获得焦点以接收键盘事件
        canvas.setFocusTraversable(true);
        canvas.requestFocus();
    }

    private void handleMovement(KeyCode code) {
        int newX = player.getX();
        int newY = player.getY();

        // 计算新位置
        switch (code) {
            case UP:
                newX--;
                break;
            case DOWN:
                newX++;
                break;
            case LEFT:
                newY--;
                break;
            case RIGHT:
                newY++;
                break;
            default:
                return;
        }

        // 检查是否可以移动（不是墙）
        if (!maze.isWall(newX, newY)) {
            // 更新玩家位置
        	player.setPosition(newX, newY);
            
            // 检查是否碰到橙黄色点
            if (maze.isTreasure(newX, newY)) {
                openFightingWindow();
                // 移除已收集的橙黄色点
                maze.getGrid()[newX][newY] = 0;
            }
            
            // 检查是否到达终点
            if (maze.isEnd(newX, newY)) {
            	
                goBackToMain();
                return;
            }
            
            // 重绘迷宫
            drawMaze();
        }
    }

    private void drawMaze() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // 绘制迷宫
        for (int i = 0; i < maze.getSize(); i++) {
            for (int j = 0; j < maze.getSize(); j++) {
                int x = j * cellSize;
                int y = i * cellSize;
                
                if (maze.isWall(i, j)) {
                    gc.setFill(Color.BLACK); // 墙
                } else if (maze.isTreasure(i, j)) {
                    gc.setFill(Color.ORANGE); // 橙黄色点
                } else if (maze.isEnd(i, j)) {
                    gc.setFill(Color.GREEN); // 终点
                } else {
                    gc.setFill(Color.WHITE); // 路径
                }
                
                gc.fillRect(x, y, cellSize, cellSize);
                //gc.setStroke(Color.GRAY);
                //gc.strokeRect(x, y, cellSize, cellSize);
            }
        }
        
        // 绘制玩家（红点）
        gc.setFill(Color.RED);
        int playerX = player.getY() * cellSize;
        int playerY = player.getX() * cellSize;
        gc.fillOval(playerX, playerY, cellSize, cellSize);
    }

    private void openFightingWindow() {
    	try {
            // 获取当前用户的背包
            
            // 创建战斗窗口并显示
            BattleController battleController = new BattleController(
            	GameDataManager.getInstance().getPetList(), 
            	GameDataManager.getInstance().getPlayerBag(),
                this::onBattleEnd
            );
            Stage battleStage = new Stage();
            battleStage.setTitle("宝可梦对战");
            battleStage.initModality(Modality.APPLICATION_MODAL);
            battleStage.setScene(new Scene(battleController.getRoot()));
            battleStage.show();
        } catch (Exception e) {
            showAlert("错误", "无法启动战斗: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        // 创建 Alert 弹窗（JavaFX 自带）
        Alert alert = new Alert(AlertType.ERROR); // ERROR 类型弹窗
        alert.setTitle(title); // 设置弹窗标题
        alert.setHeaderText(null); // 隐藏头部文本（可选）
        alert.setContentText(content); // 设置弹窗内容
        alert.showAndWait(); // 显示弹窗并等待用户关闭
    }

	private void goBackToMain() {
        if (primaryStage != null) {
            try {
                Parent mainRoot = FXMLLoader.load(getClass().getResource("/main.fxml"));
                primaryStage.setScene(new Scene(mainRoot, 800, 600));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    private void onBattleEnd(boolean playerWon, int coinsEarned, Pet newPet) {
        try {
            if (playerWon) {
            	GameDataManager.getInstance().setCoins(coinsEarned);
                
                if (newPet != null) {
                	GameDataManager.getInstance().addPet(newPet);
                    showAlert("恭喜", "你获得了新宠物: " + newPet.getName());
                }
                showAlert("胜利", "你赢得了战斗，获得了" + coinsEarned + "金币！");
            } else {
                showAlert("失败", "你输掉了战斗！");
            }

        } catch (Exception e) {
            showAlert("错误", "战斗结果处理失败: " + e.getMessage());
        }
    }
}