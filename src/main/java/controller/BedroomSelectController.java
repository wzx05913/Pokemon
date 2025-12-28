package controller;

import Player.Player;
import entity.User;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import service.GameDataManager;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.stage.StageStyle;


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
        openSaveLoadWindow();
    }

    // 打开存档/读档窗口
    private void openSaveLoadWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/savewidget.fxml"));
            Parent saveLoadRoot = loader.load();

            SaveLoadController saveLoadController = loader.getController();

            // 使用GameDataManager获取当前玩家
            GameDataManager gameDataManager = GameDataManager.getInstance();
            Player currentUser = gameDataManager.getCurrentPlayer();
            
            if (currentUser != null) {
                saveLoadController.setCurrentPlayer(currentUser);
            } else {
                showAlert("提示", "当前没有登录用户，请先创建或加载用户");
                return;
            }

            Stage saveLoadStage = new Stage();
            saveLoadStage.initModality(Modality.WINDOW_MODAL);
            saveLoadStage.initOwner(restButton.getScene().getWindow());
            saveLoadStage.initStyle(StageStyle.UTILITY);
            saveLoadStage.setTitle("存档管理");

            Scene scene = new Scene(saveLoadRoot);
            saveLoadStage.setScene(scene);
            centerStage(saveLoadStage);
            saveLoadStage.setResizable(false);
            saveLoadStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法打开存档界面：" + e.getMessage());
        }
    }

    // 居中显示窗口
    private void centerStage(Stage stage) {
        // 获取屏幕尺寸
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // 计算窗口居中位置
        double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - 425) / 2;
        double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - 415) / 2;

        // 设置窗口位置
        stage.setX(centerX);
        stage.setY(centerY);
    }

    // 外出按钮点击事件
    @FXML
    private void onOutButtonClick(ActionEvent event) {
        System.out.println("外出按钮被点击");
        try {
            // 每次外出减少所有宠物的清洁度（在内存中修改全局变量）
            service.GameDataManager.getInstance().decreaseAllPetsClean(20);

            // 检查是否还有可出战的宠物（排除 isAlive=false）
            service.GameDataManager gdm = service.GameDataManager.getInstance();
            Player current = gdm.getCurrentPlayer();
            boolean hasAlive = false;
            if (current != null) {
                for (pokemon.Pokemon p : current.getPets()) {
                    if (p != null && p.isAlive()) { hasAlive = true; break; }
                }
            }
            if (!hasAlive) {
                // 若全部死亡，弹窗提示并阻止进入迷宫
                showAlert("无法外出", "您已没有可以出战的宠物，无法外出探索！");
                return;
            }

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

    // 商店按钮点击事件
    @FXML
    private void onShopButtonClick() {
        System.out.println("商店按钮被点击");

        try {
            // 加载商店界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop.fxml"));
            Parent shopRoot = loader.load();

            // 在新窗口中打开商店（使用较大尺寸以容纳内容）
            Stage shopStage = new Stage();
            shopStage.setTitle("宝可梦商店");
            // 使用模态窗口并设置拥有者，避免被其他窗口遮挡
            shopStage.initModality(Modality.WINDOW_MODAL);
            shopStage.initOwner(shopButton.getScene().getWindow());
            Scene scene = new Scene(shopRoot, 800, 600); // 加大为 800x600
            shopStage.setScene(scene);
            shopStage.setResizable(false);
            shopStage.centerOnScreen();
            shopStage.showAndWait();

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