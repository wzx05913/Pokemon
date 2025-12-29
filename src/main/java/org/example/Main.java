package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.DBConnection;
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 初始化数据库连接
            DBConnection.getInstance();

            // 加载FXML文件
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();

            // 创建场景
            Scene scene = new Scene(root, 800, 600);

            // 设置舞台
            primaryStage.setTitle("神奇宝可梦");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            System.out.println("应用程序启动成功");

            // 显示窗口
            primaryStage.show();

            // 直接用音乐管理器控制音乐，不要再 getController()！
            Music.BgMusicManager.getInstance().playSceneMusic("cover");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("加载FXML文件失败: " + e.getMessage());
        }
    }
    @Override
    public void stop() {
        // 程序退出时关闭连接池
        DBConnection.getInstance().close();
    }

    public static void main(String[] args) {
        // 启动JavaFX应用
        launch(args);
    }
}