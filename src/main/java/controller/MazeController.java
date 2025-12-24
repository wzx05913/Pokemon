// src/main/java/controller/MazeController.java
package controller;

import core.Maze;
import core.Point;
import Player.MazePlayer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fighting.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Fighting");
            stage.setScene(new Scene(root, 400, 300));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}