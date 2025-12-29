package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.stage.Window;

import java.io.IOException;

public class SettingsController {

    @FXML
    private CheckBox musicCheckBox;
    private MainController mainController;

    //设置主控制器的方法
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        musicCheckBox.setSelected(Music.BgMusicManager.isMusicEnabled());
    }

    @FXML
    private void backtomain(ActionEvent event) {
        // 关闭当前窗口
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.close();
        try {
            for (Window window : Window.getWindows()) {
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    if (stage.getTitle().equals("神奇宝可梦") && stage.isShowing()) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
                        Parent root = loader.load();
                        Scene scene = new Scene(root, 800, 600);
                        stage.setScene(scene);

                        if (Music.BgMusicManager.isMusicEnabled()) {
                            Music.BgMusicManager.getInstance().playSceneMusic("cover");
                        }
                        return;
                    }
                }
            }

            //如果没有找到主舞台，创建新的
            Stage newStage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            newStage.setTitle("神奇宝可梦");
            newStage.setScene(scene);
            newStage.setResizable(false);
            newStage.show();

            if (Music.BgMusicManager.isMusicEnabled()) {
                Music.BgMusicManager.getInstance().playSceneMusic("cover");
            }

        } catch (IOException e) {
            e.printStackTrace();
            mainController.showAlert("错误", "返回主菜单失败: " + e.getMessage());
        }
    }

    @FXML
    private void onMusicToggle(ActionEvent event) {
        boolean isSelected = musicCheckBox.isSelected();
        //设置音乐开关
        Music.BgMusicManager.setMusicEnabled(isSelected);

        if (isSelected) {
            Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
        }
    }
}