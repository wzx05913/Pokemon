package controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import service.SessionManager;
import entity.SaveData;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;

public class SaveLoadController {

    @FXML
    private ListView<SaveData> saveList;

    @FXML
    private Button saveButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button closeButton;

    @FXML
    private Label statusLabel;

    private SessionManager sessionManager = SessionManager.getInstance();

    // 初始化方法
    @FXML
    private void initialize() {
        System.out.println("SaveLoadController初始化");

        // 设置ListView的单元格工厂
        saveList.setCellFactory(lv -> new javafx.scene.control.ListCell<SaveData>() {
            @Override
            protected void updateItem(SaveData saveData, boolean empty) {
                super.updateItem(saveData, empty);
                if (empty || saveData == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // 创建自定义显示
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
                    hbox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); -fx-padding: 5;");

                    // 图标
                    javafx.scene.control.Label icon = new javafx.scene.control.Label("💾");
                    icon.setStyle("-fx-font-size: 16px;");

                    // 信息区域
                    javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox();
                    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label("存档 " + saveData.getSlot());
                    nameLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                    nameLabel.setFont(javafx.scene.text.Font.font("Ark Pixel", 12));

                    String time = saveData.getSaveTime() != null ?
                            saveData.getSaveTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "空存档";

                    javafx.scene.control.Label infoLabel = new javafx.scene.control.Label(time);
                    infoLabel.setStyle("-fx-text-fill: #7f8c8d;");
                    infoLabel.setFont(javafx.scene.text.Font.font("Ark Pixel", 10));

                    vbox.getChildren().addAll(nameLabel, infoLabel);
                    hbox.getChildren().addAll(icon, vbox);

                    setGraphic(hbox);
                }
            }
        });

        // 加载存档列表
        loadSaveList();
    }

    // 加载存档列表
    private void loadSaveList() {
        // 模拟数据 - 实际应该从数据库或文件加载
        for (int i = 1; i <= 10; i++) {
            SaveData saveData = new SaveData();
            saveData.setSlot(i);
            saveData.setSaveName("存档位 " + i);

            // 模拟一些有数据的存档
            if (i % 2 == 0) {
                saveData.setSaveTime(LocalDateTime.now().minusHours(i * 2));
                saveData.setPlayerName("训练家" + i);
                saveData.setPlayTime(i * 5 + "小时");
            }

            saveList.getItems().add(saveData);
        }

        statusLabel.setText("已加载 " + saveList.getItems().size() + " 个存档位");
    }

    // 保存按钮点击事件
    @FXML
    private void onSaveButtonClick() {
        SaveData selected = saveList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 保存游戏
            selected.setSaveTime(LocalDateTime.now());
            //selected.setPlayerName(sessionManager.getCurrentUser() != null ?
            //        sessionManager.getCurrentUser().getUserId() : "玩家");
            selected.setPlayTime("1小时"); // 实际应该计算

            // 刷新显示
            saveList.refresh();

            statusLabel.setText("存档成功！位置：" + selected.getSlot());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("存档成功");
            alert.setHeaderText(null);
            alert.setContentText("游戏已保存到存档位 " + selected.getSlot());
            alert.show();
        } else {
            statusLabel.setText("请先选择一个存档位");
        }
    }

    // 读取按钮点击事件
    @FXML
    private void onLoadButtonClick() {
        SaveData selected = saveList.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getSaveTime() != null) {
            statusLabel.setText("正在读取存档位 " + selected.getSlot() + "...");

            // 这里应该实现实际的读取逻辑
            // TODO: 从数据库或文件加载游戏数据

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("读取存档");
            alert.setHeaderText(null);
            alert.setContentText("已读取存档位 " + selected.getSlot());
            alert.show();
        } else {
            statusLabel.setText("请选择一个有效的存档位");
        }
    }

    // 删除按钮点击事件
    @FXML
    private void onDeleteButtonClick() {
        SaveData selected = saveList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 确认对话框
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认删除");
            confirm.setHeaderText(null);
            confirm.setContentText("确定要删除存档位 " + selected.getSlot() + " 吗？");

            confirm.showAndWait().ifPresent(response -> {
                if (response == javafx.scene.control.ButtonType.OK) {
                    // 清空存档数据
                    selected.setSaveTime(null);
                    selected.setPlayerName(null);
                    selected.setPlayTime(null);

                    saveList.refresh();
                    statusLabel.setText("已删除存档位 " + selected.getSlot());
                }
            });
        } else {
            statusLabel.setText("请先选择一个存档位");
        }
    }

    // 关闭按钮点击事件
    @FXML
    private void onCloseButtonClick() {
        System.out.println("关闭存档界面");
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}