package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class BedroomHintController {

    private MainController mainController;

    @FXML
    private Button confirmButton;  // 这里的fx:id需要和FXML中一致

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onConfirmButtonClick() {
        System.out.println("我清楚了按钮被点击");
        if (mainController != null) {
            Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
            mainController.switchToPage("main");
        }
    }

    // 初始化方法
    @FXML
    private void initialize() {
        if (confirmButton != null) {
            System.out.println("确认按钮已注入: " + confirmButton.getText());
        }
    }
}