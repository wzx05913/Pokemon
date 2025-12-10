package controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML
    private StackPane container;

    private Map<String, Parent> pages = new HashMap<>();
    private boolean initialized = false;  // 添加标记

    // 初始化
    @FXML
    private void initialize() {
        if (!initialized) {  // 防止重复初始化
            initialized = true;
            System.out.println("控制器初始化开始");

            try {
                // 调用loadPage方法加载页面
                loadPage("dialog1", "/enter1.fxml");
                loadPage("dialog2", "/enter2.fxml");
                loadPage("dialog3", "/select.fxml");

                System.out.println("初始化完成");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 加载页面
    private void loadPage(String name, String fxmlPath) {
        try {
            System.out.println("正在加载页面: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // 根据页面名称添加不同的点击事件
            if (name.equals("dialog1")) {
                page.setOnMouseClicked(event -> {
                    System.out.println("点击dialog1，切换到dialog2");
                    switchToPage("dialog2");
                });
            } else if (name.equals("dialog2")) {
                page.setOnMouseClicked(event -> {
                    System.out.println("点击dialog2，切换到dialog3");
                    switchToPage("dialog3");
                });
            }

            pages.put(name, page);
            System.out.println("页面加载成功: " + fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("页面加载失败: " + fxmlPath);
        }
    }

    // 切换到指定页面
    private void switchToPage(String pageName) {
        if (pages.containsKey(pageName)) {
            container.getChildren().clear();
            container.getChildren().add(pages.get(pageName));
            System.out.println("切换到页面: " + pageName);
        } else {
            System.out.println("页面不存在: " + pageName);
        }
    }

    // 带淡入淡出动画的切换
    private void switchToPageWithFade(String pageName) {
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

    // 返回主菜单
    @FXML
    private void backToMain() {
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