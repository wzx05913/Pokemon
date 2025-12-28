package controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

public class SettingsController {

    @FXML
    private CheckBox musicCheckBox;

    private MainController mainController;  // 保存主控制器引用

    // 设置主控制器的方法
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        // 初始化代码
    }

    @FXML
    private void backtomain(ActionEvent event) {
        // 关闭设置窗口
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();

        // 如果有主控制器引用，调用返回主菜单的方法
        if (mainController != null) {
            mainController.backToMain();
        } else {
            System.err.println("警告：MainController引用为空");
        }
    }
}
