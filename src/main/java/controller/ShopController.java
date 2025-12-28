package controller;

import entity.Bag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import service.GameDataManager;
import Player.Player;
import java.util.Optional;

public class ShopController {

	@FXML
	private Label coinLabel;

	@FXML
	private Label eggCountLabel;

	@FXML
	private Label soapCountLabel;

	@FXML
	private Label riceCountLabel;

	@FXML
	private Spinner<Integer> eggSpinner;

	@FXML
	private Spinner<Integer> soapSpinner;

	@FXML
	private Spinner<Integer> riceSpinner;

	private GameDataManager dataManager = GameDataManager.getInstance();

	@FXML
	public void initialize() {
		// 初始化 Spinner（可编辑，允许直接输入或上下箭头选择）
		eggSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
		eggSpinner.setEditable(true);
		soapSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
		soapSpinner.setEditable(true);
		riceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
		riceSpinner.setEditable(true);

		updateUI();
	}

	private void updateUI() {
		Bag bag = dataManager.getPlayerBag();
		int coins = 0;
		int egg = 0;
		int soap = 0;
		int rice = 0;
		if (bag != null) {
			coins = bag.getCoins() != null ? bag.getCoins() : 0;
			egg = bag.getEggCount() != null ? bag.getEggCount() : 0;
			soap = bag.getSoapCount() != null ? bag.getSoapCount() : 0;
			rice = bag.getRiceCount() != null ? bag.getRiceCount() : 0;
		} else {
			Player p = dataManager.getCurrentPlayer();
			if (p != null) coins = p.getMoney();
		}

		coinLabel.setText("金币数：" + coins);
		eggCountLabel.setText("持有：" + egg);
		soapCountLabel.setText("持有：" + soap);
		riceCountLabel.setText("持有：" + rice);
	}

	@FXML
	private void buyEgg(ActionEvent event) {
		final int unitPrice = 200;
		int count = eggSpinner.getValue();
		final int price = unitPrice * count;
		Bag bag = dataManager.getPlayerBag();
		if (bag == null) {
			bag = new Bag();
			dataManager.setCurrentBag(bag);
		}
		int coins = bag.getCoins() != null ? bag.getCoins() : 0;
		if (coins < price) {
			showAlert("提示", "金币不足，无法购买煎蛋", AlertType.INFORMATION);
			return;
		}

		// 询问确认
		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle("确认购买");
		confirm.setHeaderText(null);
		confirm.setContentText("确定要购买 " + count + " 个 煎蛋 吗？ 总价：" + price + " 金币");
		Optional<ButtonType> res = confirm.showAndWait();
		if (res.isEmpty() || res.get() != ButtonType.OK) return;

		bag.setCoins(coins - price);
		bag.setEggCount((bag.getEggCount() != null ? bag.getEggCount() : 0) + count);
		// 同步到 currentPlayer money
		Player p = dataManager.getCurrentPlayer();
		if (p != null) p.setMoney(bag.getCoins());

		showAlert("成功", "购买煎蛋成功！", AlertType.INFORMATION);
		updateUI();
	}

	@FXML
	private void buySoap(ActionEvent event) {
		final int unitPrice = 50;
		int count = soapSpinner.getValue();
		final int price = unitPrice * count;
		Bag bag = dataManager.getPlayerBag();
		if (bag == null) {
			bag = new Bag();
			dataManager.setCurrentBag(bag);
		}
		int coins = bag.getCoins() != null ? bag.getCoins() : 0;
		if (coins < price) {
			showAlert("提示", "金币不足，无法购买肥皂", AlertType.INFORMATION);
			return;
		}

		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle("确认购买");
		confirm.setHeaderText(null);
		confirm.setContentText("确定要购买 " + count + " 个 肥皂 吗？ 总价：" + price + " 金币");
		Optional<ButtonType> res = confirm.showAndWait();
		if (res.isEmpty() || res.get() != ButtonType.OK) return;

		bag.setCoins(coins - price);
		bag.setSoapCount((bag.getSoapCount() != null ? bag.getSoapCount() : 0) + count);
		Player p = dataManager.getCurrentPlayer();
		if (p != null) p.setMoney(bag.getCoins());

		showAlert("成功", "购买肥皂成功！", AlertType.INFORMATION);
		updateUI();
	}

	@FXML
	private void buyRice(ActionEvent event) {
		final int unitPrice = 50;
		int count = riceSpinner.getValue();
		final int price = unitPrice * count;
		Bag bag = dataManager.getPlayerBag();
		if (bag == null) {
			bag = new Bag();
			dataManager.setCurrentBag(bag);
		}
		int coins = bag.getCoins() != null ? bag.getCoins() : 0;
		if (coins < price) {
			showAlert("提示", "金币不足，无法购买苹果", AlertType.INFORMATION);
			return;
		}

		Alert confirm = new Alert(AlertType.CONFIRMATION);
		confirm.setTitle("确认购买");
		confirm.setHeaderText(null);
		confirm.setContentText("确定要购买 " + count + " 个 苹果 吗？ 总价：" + price + " 金币");
		Optional<ButtonType> res = confirm.showAndWait();
		if (res.isEmpty() || res.get() != ButtonType.OK) return;

		bag.setCoins(coins - price);
		bag.setRiceCount((bag.getRiceCount() != null ? bag.getRiceCount() : 0) + count);
		Player p = dataManager.getCurrentPlayer();
		if (p != null) p.setMoney(bag.getCoins());

		showAlert("成功", "购买苹果成功！", AlertType.INFORMATION);
		updateUI();
	}

	private void showAlert(String title, String content, AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

}
