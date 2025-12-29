package controller;

import Player.Player;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import service.GameDataManager;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

import javax.swing.plaf.synth.Region;


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
    @FXML
    private Button settingsButton;

    // 设置MainController引用
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // 初始化方法
    @FXML
    private void initialize() {
        Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
        System.out.println("BedroomSelectController初始化");
        // 可以在这里添加一些初始化代码
    }

    @FXML
    private void onPetButtonClick() {
        System.out.println("宠物按钮被点击");
        try {
            java.net.URL resource = getClass().getResource("/petmanage.fxml");
            if (resource == null) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("错误");
                a.setHeaderText(null);
                a.setContentText("找不到宠物管理页面资源(petmanage.fxml)。");
                a.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initOwner(petButton.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);

            // 关键修改1：设置为透明无装饰窗口
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setTitle(""); // 移除标题

            // 创建透明场景
            Scene scene = new Scene(root, 685, 444);
            scene.setFill(Color.TRANSPARENT); // 场景背景透明

            dialog.setScene(scene);
            dialog.setResizable(false);

            dialog.centerOnScreen();
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("错误");
            a.setHeaderText(null);
            a.setContentText("无法打开宠物管理界面：" + e.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void onsettingsButtonClick() {
        opensettingsWindow(mainController);
    }

    private void opensettingsWindow(MainController mainController) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/settingswidget.fxml"));
            Parent saveLoadRoot = loader.load();

            // 获取设置窗口的控制器
            SettingsController settingsController = loader.getController();

            // 传递主控制器引用
            settingsController.setMainController(mainController);

            Stage settingsStage = new Stage();
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.initOwner(settingsButton.getScene().getWindow());
            settingsStage.initStyle(StageStyle.UTILITY);
            settingsStage.setTitle("设置");

            Scene scene = new Scene(saveLoadRoot);
            settingsStage.setScene(scene);
            settingsStage.setResizable(false);

            settingsStage.centerOnScreen();
            settingsStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法打开设置界面：" + e.getMessage());
        }
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

            // 关键：使用透明无装饰样式
            saveLoadStage.initStyle(StageStyle.TRANSPARENT);
            saveLoadStage.setTitle("");

            // 创建场景，背景设为透明
            Scene scene = new Scene(saveLoadRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            saveLoadStage.setScene(scene);

            // 设置大小与FXML内容匹配
            saveLoadStage.sizeToScene();


            saveLoadStage.setResizable(false);

            // 添加ESC键关闭功能
            scene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    saveLoadStage.close();
                }
            });

            // 添加点击外部关闭功能（可选）
            saveLoadRoot.setOnMouseClicked(event -> {
                if (event.getTarget() == saveLoadRoot) {
                    saveLoadStage.close();
                }
            });
            saveLoadStage.centerOnScreen();
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

            // 创建商店窗口
            Stage shopStage = new Stage();
            shopStage.setTitle("宝可梦商店");

            // 设置为模态窗口
            shopStage.initModality(Modality.WINDOW_MODAL);
            shopStage.initOwner(shopButton.getScene().getWindow());

            // 创建场景并设置透明
            Scene scene = new Scene(shopRoot, 800, 600);
            scene.setFill(Color.TRANSPARENT);

            // 设置窗口样式 - 无装饰
            shopStage.initStyle(StageStyle.TRANSPARENT);
            shopStage.setScene(scene);
            shopStage.setResizable(false);

            // 确保场景背景透明
            scene.getRoot().setStyle("-fx-background-color: transparent;");

            // 将透明背景应用到根节点
            shopRoot.setStyle("-fx-background-color: transparent;");

            // 确保CSS样式不会覆盖透明背景
            scene.getStylesheets().clear(); // 如果有CSS文件，可能需要特殊处理

            shopStage.centerOnScreen();
            shopStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法加载商店界面：" + e.getMessage());
        }
    }

    // 返回主菜单

    // 显示提示信息
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}