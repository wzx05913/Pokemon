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


public class BedroomSelectController {
    private MainController mainController;
    @FXML
    private Button petButton;
    @FXML
    private Button restButton;
    @FXML
    private Button outButton;
    @FXML
    private Button shopButton;
    @FXML
    private Button settingsButton;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
        System.out.println("BedroomSelectController初始化");
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
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.setTitle("");

            Scene scene = new Scene(root, 685, 444);
            scene.setFill(Color.TRANSPARENT);

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

            SettingsController settingsController = loader.getController();
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

    @FXML
    private void onRestButtonClick() {
        System.out.println("休息（存档）按钮被点击");
        openSaveLoadWindow();
    }

    private void openSaveLoadWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/savewidget.fxml"));
            Parent saveLoadRoot = loader.load();

            SaveLoadController saveLoadController = loader.getController();

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
            saveLoadStage.initStyle(StageStyle.TRANSPARENT);
            saveLoadStage.setTitle("");

            Scene scene = new Scene(saveLoadRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            saveLoadStage.setScene(scene);
            saveLoadStage.sizeToScene();
            saveLoadStage.setResizable(false);

            scene.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    saveLoadStage.close();
                }
            });

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

    private void centerStage(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - 425) / 2;
        double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - 415) / 2;
        stage.setX(centerX);
        stage.setY(centerY);
    }

    @FXML
    private void onOutButtonClick(ActionEvent event) {
        System.out.println("外出按钮被点击");
        try {
            service.GameDataManager.getInstance().decreaseAllPetsClean(20);

            service.GameDataManager gdm = service.GameDataManager.getInstance();
            Player current = gdm.getCurrentPlayer();
            boolean hasAlive = false;
            if (current != null) {
                for (pokemon.Pokemon p : current.getPets()) {
                    if (p != null && p.isAlive()) {
                        hasAlive = true;
                        break;
                    }
                }
            }
            if (!hasAlive) {
                showAlert("无法外出", "您已没有可以出战的宠物，无法外出探索！");
                return;
            }

            Stage currentStage = (Stage) ((javafx.scene.Node) event.getTarget()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/maze.fxml"));
            Parent mazeRoot = loader.load();

            MazeController mazeController = loader.getController();
            mazeController.setPrimaryStage(currentStage);
            currentStage.setScene(new Scene(mazeRoot, 800, 600));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onShopButtonClick() {
        System.out.println("商店按钮被点击");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop.fxml"));
            Parent shopRoot = loader.load();

            Stage shopStage = new Stage();
            shopStage.setTitle("宝可梦商店");

            shopStage.initModality(Modality.WINDOW_MODAL);
            shopStage.initOwner(shopButton.getScene().getWindow());

            Scene scene = new Scene(shopRoot, 800, 600);
            scene.setFill(Color.TRANSPARENT);

            shopStage.initStyle(StageStyle.TRANSPARENT);
            shopStage.setScene(scene);
            shopStage.setResizable(false);

            scene.getRoot().setStyle("-fx-background-color: transparent;");

            shopRoot.setStyle("-fx-background-color: transparent;");

            scene.getStylesheets().clear();

            shopStage.centerOnScreen();
            shopStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "无法加载商店界面：" + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}