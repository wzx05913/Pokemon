package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class BedroomSelectController {

    private MainController mainController;
    // 注入FXML中的按钮
    @FXML
    private Button petButton;        // 宠物按钮
    @FXML
    private Button restButton;       // 休息按钮
    @FXML
    private Button outButton;        // 外出按钮
    @FXML
    private Button shopButton;       // 商店按钮

    // 设置MainController引用
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // 初始化方法
    @FXML
    private void initialize() {
        System.out.println("BedroomSelectController初始化");
        // 可以在这里添加一些初始化代码
    }

    // 宠物按钮点击事件
    @FXML
    private void onPetButtonClick() {
        System.out.println("宠物按钮被点击");
        // TODO: 跳转到宠物管理界面
        showAlert("功能开发中", "宠物管理功能正在开发中...");
    }

    // 休息（存档）按钮点击事件
    @FXML
    private void onRestButtonClick() {
        System.out.println("休息（存档）按钮被点击");
        // TODO: 保存游戏进度
        showAlert("存档成功", "游戏进度已保存！");
    }

    // 外出按钮点击事件
    @FXML
    private void onOutButtonClick() {
        System.out.println("外出按钮被点击");

        try {
            // 加载外出地图界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/map.fxml"));
            Parent mapRoot = loader.load();

            // 获取当前舞台
            Stage currentStage = (Stage) outButton.getScene().getWindow();

            // 设置新场景
            Scene scene = new Scene(mapRoot, 800, 600);
            currentStage.setScene(scene);
            currentStage.setTitle("外出地图");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法加载外出地图：" + e.getMessage());
        }
    }

    // 商店按钮点击事件
    @FXML
    private void onShopButtonClick() {
        System.out.println("商店按钮被点击");

        try {
            // 加载商店界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop.fxml"));
            Parent shopRoot = loader.load();

            // 在新窗口中打开商店
            Stage shopStage = new Stage();
            shopStage.setTitle("宝可梦商店");
            shopStage.setScene(new Scene(shopRoot, 600, 400));
            shopStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法加载商店界面：" + e.getMessage());
        }
    }

    // 返回主菜单
    @FXML
    private void onBackButtonClick() {
        System.out.println("返回主菜单");
        if (mainController != null) {
            mainController.backToMain();
        }
    }

    // 显示提示信息
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}