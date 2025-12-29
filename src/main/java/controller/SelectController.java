package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SelectController {

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void selectBulbasaur() {
        System.out.println("选择了妙蛙种子");
        if (mainController != null) {
            mainController.selectPet("妙蛙种子");
        }
    }

    @FXML
    private void selectCharmander() {
        System.out.println("选择了小火龙");
        if (mainController != null) {
            mainController.selectPet("小火龙");
        }
    }

    @FXML
    private void selectPikachu() {
        System.out.println("选择了皮卡丘");
        if (mainController != null) {
            mainController.selectPet("皮卡丘");
        }
    }
}
