package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SelectController {

    private MainController mainController;

    // 设置MainController引用
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // 妙蛙种子按钮
    @FXML
    private void selectBulbasaur() {
        System.out.println("选择了妙蛙种子");
        if (mainController != null) {
            mainController.selectPet("BULBASAUR");
        }
    }

    // 小火龙按钮
    @FXML
    private void selectCharmander() {
        System.out.println("选择了小火龙");
        if (mainController != null) {
            mainController.selectPet("CHARMANDER");
        }
    }

    // 皮卡丘按钮
    @FXML
    private void selectPikachu() {
        System.out.println("选择了皮卡丘");
        if (mainController != null) {
            mainController.selectPet("PIKACHU");
        }
    }
}
