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
import controller.BattleController;
import javafx.stage.Modality;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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
            // 1. 加载FXML文件（关键修复：正确设置FXML路径）
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/battleView.fxml"));

            
            Parent battleRoot = loader.load(); // 此时会触发 BattleController 的默认构造函数
            
            // 获取由 FXMLLoader 创建的控制器实例
            BattleController controller = loader.getController();
            
            // 如果你需要传递数据，在 BattleController 中增加一个 setupData 方法，而不是写在构造函数里
            List<Pet> petList = GameDataManager.getInstance().getPetList();
            controller.setupBattle(new ArrayList<>(petList), this::onBattleEnd);

            Stage battleStage = new Stage();
            battleStage.setScene(new Scene(battleRoot));
            battleStage.show();
            
        } catch (IOException e) {
            // 更详细的错误提示，帮助定位问题
            String errorMsg = "无法启动战斗: ";
            if (e.getMessage().contains("Location is not set")) {
                errorMsg += "FXML文件路径错误，请检查battleView.fxml是否存在于指定路径";
            } else {
                errorMsg += e.getMessage();
            }
            showAlert("错误", errorMsg);
            e.printStackTrace(); // 控制台输出详细堆栈，便于调试
        } catch (Exception e) {
            showAlert("错误", "战斗初始化失败: " + e.getMessage());
            e.printStackTrace();
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
    
    private void onBattleEnd(Boolean isPlayerWin) {
        try {
            if (isPlayerWin) {
            	
            }
        } catch (Exception e) {
            showAlert("错误", "战斗结果处理失败: " + e.getMessage());
        }
    
    }        


                
}