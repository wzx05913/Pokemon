// src/main/java/controller/MazeEntryController.java
package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent; // 新增导入
import java.io.IOException;

/**
 * 迷宫入口控制器（空白窗口，带有开始探索按钮）
 */
public class MazeEntryController {
    @FXML
    private void startExploring(ActionEvent event) { // 新增ActionEvent参数
        try {
            // 从事件对象中获取目标组件
            Stage currentStage = (Stage) ((javafx.scene.Node) event.getTarget()).getScene().getWindow();
            
            // 加载迷宫界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/maze.fxml"));
            Parent mazeRoot = loader.load();
            
            MazeController mazeController = loader.getController();
            mazeController.setPrimaryStage(currentStage);
            
            currentStage.setScene(new Scene(mazeRoot, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}