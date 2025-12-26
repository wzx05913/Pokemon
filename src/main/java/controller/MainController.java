// MainController.java
package controller;
import javafx.scene.input.MouseEvent;
import Player.Player;
import service.SessionManager;
import service.PetFactory;
import database.UserDAO;
import database.PetDAO;
import entity.User;
import entity.Pet;
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


public class MainController {

    @FXML
    private StackPane container;

    private Map<String, Parent> pages = new HashMap<>();
    private boolean initialized = false;
    private SessionManager sessionManager = SessionManager.getInstance();

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

            // 将事件过滤器移到外面
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
        // 这里可以添加加载已有用户的功能
    }

    // 创建新用户

    //private void createNewUser() throws SQLException {
        //UserDAO userDAO = new UserDAO();
        //User newUser = userDAO.createUser();
        //sessionManager.setCurrentUser(newUser);
        //System.out.println("新用户创建成功，用户ID: " + newUser.getUserId());
    //}

    // 加载页面
    private void loadPage(String name, String fxmlPath) {
        try {
            System.out.println("正在加载页面: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("页面加载失败: " + fxmlPath);
        }
    }

    // 选择宠物
    public void selectPet(String petName) {
        try {
            Player newPlayer = new Player();
            int userId=1;//暂时默认覆盖存档一
            // 1. 创建Pokemon游戏对象
            Pokemon pokemon = PetFactory.createPokemon(petName, 1);
            
            Pet petEntity = PokemonFactory.convertToEntity(pokemon, userId);
            GameDataManager.getInstance().addPet(petEntity);
            
            newPlayer.addPet(pokemon);

            // 4. 更新会话状态
            sessionManager.setCurrentPokemon(pokemon);

            System.out.println("宠物创建成功！");
            System.out.println("宠物名称: " + pokemon.getName());
            System.out.println("宠物等级: " + pokemon.getLevel());
            System.out.println("宠物攻击力: " + pokemon.getAttack());
            System.out.println("最大HP: " + pokemon.getMaxHp());

            // 5. 显示创建成功消息
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
        alert.setOnHidden(event -> {
            switchToPageWithFade("main1");
        });

        alert.show();
    }

    private void showAlert(String title, String content) {
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
    }
    @FXML
    private void enterMaze(ActionEvent event) {
        try {
            // 加载迷宫入口窗口
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mazeEntry.fxml"));
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
        // 不需要加载main.fxml，因为已经在容器中
        if (!container.getChildren().isEmpty()) {
            container.getChildren().clear();
            // 重新创建主菜单
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
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
}