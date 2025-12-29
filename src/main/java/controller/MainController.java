// MainController.java
package controller;
import Music.BgMusicManager;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import Player.Player;

import entity.Pet;
import entity.Bag;
import pokemon.Pokemon;
import service.GameDataManager;
import pokemon.PokemonFactory;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;


public class MainController {

    @FXML
    private StackPane container;

    private Map<String, Parent> pages = new HashMap<>();
    private boolean initialized = false;
    private GameDataManager dataManager = GameDataManager.getInstance();

    @FXML
    private void initialize() {
        if (!initialized) {
            initialized = true;
            System.out.println("控制器初始化开始");

            try {
                loadPage("dialog1", "/enter1.fxml");
                loadPage("dialog2", "/enter2.fxml");
                loadPage("select", "/select.fxml");
                loadPage("main1", "/bedroom.fxml");
                loadPage("hint", "/bedroom-hint.fxml");
                loadPage("main", "/bedroom-select.fxml");

                System.out.println("初始化完成");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 在这里注册事件过滤器
        container.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!container.getChildren().isEmpty()) {
                Node currentNode = container.getChildren().get(0);

                // 使用页面名称比较
                String currentPageName = getCurrentPageName(currentNode);
                System.out.println("当前页面: " + currentPageName);

                if ("main1".equals(currentPageName)) {
                    System.out.println("捕获到main1页面点击，位置: " + e.getSceneX() + "," + e.getSceneY());
                    switchToPageWithFade("hint");
                    e.consume();
                }
            }
        });
    }

    // 添加辅助方法获取当前页面名称
    private String getCurrentPageName(Node node) {
        for (Map.Entry<String, Parent> entry : pages.entrySet()) {
            if (entry.getValue() == node) {
                return entry.getKey();
            }
        }
        return null;
    }

    // 继续游戏按钮
    @FXML
    private void handleContinueGame() {
        System.out.println("继续游戏按钮被点击");
        // 弹出存档管理窗口，但处于只读模式（只能读取，不能保存或删除）
        try {
            // 先检查资源是否存在，避免 "Location is not set"
            java.net.URL resource = getClass().getResource("/savewidget.fxml");
            if (resource == null) {
                System.err.println("无法找到 /savewidget.fxml");
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("错误");
                a.setHeaderText(null);
                a.setContentText("找不到存档界面资源(savewidget.fxml)。");
                a.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent saveLoadRoot = loader.load();
            controller.SaveLoadController saveLoadController = loader.getController();

            // 设置只读模式并传入主控制器引用（以便读档后跳转页面）
            saveLoadController.setReadOnly(true);
            saveLoadController.setMainController(this);

            // 将当前会话中的玩家传入（可能为 null）
            saveLoadController.setCurrentPlayer(GameDataManager.getInstance().getCurrentPlayer());

            Stage saveLoadStage = new Stage();
            saveLoadStage.initModality(Modality.WINDOW_MODAL);
            if (container != null && container.getScene() != null) {
                saveLoadStage.initOwner(container.getScene().getWindow());
            }

            // 关键修改：使用透明无装饰样式
            saveLoadStage.initStyle(StageStyle.TRANSPARENT);
            saveLoadStage.setTitle("");

            // 创建场景，设置透明背景
            Scene scene = new Scene(saveLoadRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            saveLoadStage.setScene(scene);
            saveLoadStage.sizeToScene();

            saveLoadStage.centerOnScreen();
            saveLoadRoot.setOnMouseClicked(event -> {
                if (event.getTarget() == saveLoadRoot) {
                    saveLoadStage.close();
                }
            });

            saveLoadStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("无法打开存档界面：" + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("无法打开存档界面：" + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadPage(String name, String fxmlPath) {
        try {
            System.out.println("正在加载页面: " + fxmlPath);
            java.net.URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("无法找到资源: " + fxmlPath);
            } else {
                FXMLLoader loader = new FXMLLoader(resource);
                Parent page = loader.load();

                // 获取控制器
                Object controller = loader.getController();

                if ("select".equals(name)) {
                    SelectController selectController = (SelectController) controller;
                    selectController.setMainController(this);
                } else if ("hint".equals(name)) {
                    // 为hint页面设置控制器
                    if (controller instanceof BedroomHintController) {
                        BedroomHintController hintController = (BedroomHintController) controller;
                        hintController.setMainController(this);
                        System.out.println("BedroomHintController设置成功");
                    }
                }else if ("main".equals(name)) {  // 注意：这里是"main"，对应bedroom-select
                    if (controller instanceof BedroomSelectController) {
                        BedroomSelectController selectController = (BedroomSelectController) controller;
                        selectController.setMainController(this);
                        System.out.println("BedroomSelectController设置成功");
                    } else {
                        System.out.println("警告: bedroom-select页面的控制器类型不正确");
                    }
                }
                if ("dialog1".equals(name)) {
                    page.setOnMouseClicked(event -> {
                        System.out.println("dialog1被点击");
                        switchToPage("dialog2");
                    });
                } else if ("dialog2".equals(name)) {
                    page.setOnMouseClicked(event -> {
                        System.out.println("dialog2被点击");
                        switchToPage("select");
                    });
                }

                pages.put(name, page);
                System.out.println("页面加载成功: " + fxmlPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("页面加载失败: " + fxmlPath);
        }
    }

    // 选择宠物
    public void selectPet(String petName) {
        try {
            GameDataManager.getInstance().clearSession();
            int userId = -1;
            Pet pet = new Pet();
            pet.setName(petName);
            pet.setType(petName);
            pet.setLevel(1);
            Pokemon pokemon = PokemonFactory.createPokemon(pet);

            Pet petEntity = PokemonFactory.convertToEntity(pokemon, userId);
            GameDataManager.getInstance().addPet(petEntity);

            Player player = new Player(100, userId);
            player.addPet(pokemon);

            GameDataManager gameDataManager = GameDataManager.getInstance();

            gameDataManager.setCurrentPlayer(player,false);
            gameDataManager.setCurrentPokemon(pokemon);
            gameDataManager.addPokemon(pokemon);
            gameDataManager.setCurrentUserId(userId);
            Bag userBag = new Bag();
            if (userBag == null) {
                userBag = new Bag(userId);
            }
            gameDataManager.setCurrentBag(userBag);

            System.out.println("宠物创建成功！");
            System.out.println("宠物名称: " + pokemon.getName());
            System.out.println("宠物等级: " + pokemon.getLevel());
            System.out.println("宠物攻击力: " + pokemon.getAttack());
            System.out.println("最大HP: " + pokemon.getMaxHp());

            showSuccessAlert(petName);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("错误", "创建宠物失败: " + e.getMessage());
        }

    }

    private void showSuccessAlert(String petName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("宠物创建成功");
        alert.setHeaderText(null);
        alert.setContentText("恭喜！你选择了" + petName + "！\n游戏即将开始...");

        // 把 owner 设置为主窗口，避免焦点/模态问题
        if (container != null && container.getScene() != null) {
            alert.initOwner(container.getScene().getWindow());
        }

        System.out.println("[DEBUG] 将显示成功提示框");
        alert.setOnHidden(event -> {
            System.out.println("[DEBUG] 提示框已关闭，准备切换页面 -> main1");
            Platform.runLater(() -> {
                System.out.println("[DEBUG] pages keys: " + pages.keySet());
                System.out.println("[DEBUG] pages.containsKey(\"main1\"): " + pages.containsKey("main1"));
                System.out.println("[DEBUG] container == null? " + (container == null));
                if (container != null) {
                    System.out.println("[DEBUG] container children count: " + container.getChildren().size());
                    if (!container.getChildren().isEmpty()) {
                        Node curr = container.getChildren().get(0);
                        System.out.println("[DEBUG] current child class: " + curr.getClass().getName());
                        System.out.println("[DEBUG] current child equals pages.get(\"select\")? " + (curr == pages.get("select")));
                        System.out.println("[DEBUG] current child equals pages.get(\"main1\")? " + (curr == pages.get("main1")));
                    }
                }
                // 先尝试直接切换（无动画）
                switchToPage("main1");
                // 如果你仍想用淡入淡出，改为 switchToPageWithFade("main1");
            });
        });

        alert.show();
    }

    void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    void switchToPage(String pageName) {
        if (pages.containsKey(pageName)) {
            container.getChildren().clear();
            container.getChildren().add(pages.get(pageName));
            System.out.println("切换到页面: " + pageName);
        } else {
            System.out.println("页面不存在: " + pageName);
        }
    }

    void switchToPageWithFade(String pageName) {
        if (pages.containsKey(pageName) && !container.getChildren().isEmpty()) {
            Parent newPage = pages.get(pageName);
            Node currentPageNode = container.getChildren().get(0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentPageNode);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                container.getChildren().clear();
                newPage.setOpacity(0);
                container.getChildren().add(newPage);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newPage);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                System.out.println("动画切换到: " + pageName);
            });

            fadeOut.play();
        } else {
            switchToPage(pageName);
        }
    }

    // 按钮点击事件
    @FXML
    private void handleNewGame() {
        System.out.println("新游戏按钮被点击");
        switchToPageWithFade("dialog1");
        BgMusicManager.getInstance().stopMusic();
    }
    @FXML
    private void enterMaze(ActionEvent event) {
        try {
            // 加载迷宫入口窗口
            java.net.URL resource = getClass().getResource("/mazeEntry.fxml");
            if (resource == null) {
                System.err.println("无法找到 /mazeEntry.fxml");
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("错误");
                a.setHeaderText(null);
                a.setContentText("找不到迷宫入口资源(mazeEntry.fxml)。");
                a.showAndWait();
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent mazeEntryRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("迷宫探索");
            stage.setScene(new Scene(mazeEntryRoot, 400, 300));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 返回主菜单
    @FXML
    void backToMain() {
        System.out.println("返回主菜单");
        BgMusicManager.getInstance().playSceneMusic("cover");
        if (!container.getChildren().isEmpty()) {
            container.getChildren().clear();
            // 重新创建主菜单
            try {
                java.net.URL resource = getClass().getResource("/main.fxml");
                if (resource == null) {
                    System.err.println("无法找到 /main.fxml");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                Parent mainPage = loader.load();
                mainPage.setOpacity(0);
                container.getChildren().add(mainPage);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), mainPage);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 居中显示窗口
    private void centerStage(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = screenBounds.getMinX() + (screenBounds.getWidth() - stage.getWidth()) / 2;
        double centerY = screenBounds.getMinY() + (screenBounds.getHeight() - stage.getHeight()) / 2;
        stage.setX(centerX);
        stage.setY(centerY);
    }
}