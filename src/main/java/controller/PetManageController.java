package controller;

import java.sql.SQLException;
import java.util.List;

import entity.Bag;
import entity.Pet;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import pokemon.Pokemon;
import pokemon.PokemonFactory;
import service.GameDataManager;
import service.PetFactory;

/**
 * 宠物管理界面控制器
 */
public class PetManageController {
    @FXML
    private ScrollPane petListScrollPane; // 左侧宠物列表滚动面板

    @FXML
    private ImageView petImageView; // 右侧宠物图片

    @FXML
    private Label nameLabel; // 名字标签

    @FXML
    private Label idLabel; // id标签

    @FXML
    private Label cleanLabel; // 清洁度标签

    @FXML
    private Label levelLabel; // 等级标签

    // 添加经验标签
    @FXML
    private Label expLabel;

    // 添加道具按钮
    @FXML
    private Button useEggButton;

    @FXML
    private Button useSoapButton;

    @FXML
    private Button useRiceButton;

    // 道具数量标签
    @FXML
    private Label eggCountLabel;

    @FXML
    private Label soapCountLabel;

    @FXML
    private Label riceCountLabel;

    private GameDataManager gameDataManager;
    private List<Pet> petList;
    private Bag playerBag;
    private Pet selectedPet;
    private Pokemon selectedPokemon;

    @FXML
    private void initialize() {
        System.out.println("PetManageController 初始化");

        // 获取 GameDataManager 实例
        gameDataManager = GameDataManager.getInstance();

        // 打印调试信息
        System.out.println("当前玩家: " + gameDataManager.getCurrentPlayer());
        System.out.println("宠物列表: " + gameDataManager.getPetList());

        // 初始化UI
        initUI();

        // 加载宠物列表
        loadPetList();

        // 更新道具数量显示
        updateItemCounts();
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 设置默认文本
        nameLabel.setText("未选择宠物");
        idLabel.setText("--");
        cleanLabel.setText("--");
        levelLabel.setText("--");

        if (expLabel != null) {
            expLabel.setText("--/--");
        }

        // 设置默认图片
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_pet.png"));
            petImageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("无法加载默认图片: " + e.getMessage());
        }
    }

    /**
     * 加载宠物列表到左侧滚动面板
     */
    private void loadPetList() {
        try {
            // 获取宠物列表
            petList = gameDataManager.getPetList();

            // 调试：打印宠物列表
            System.out.println("从GameDataManager获取宠物列表，数量: " + (petList != null ? petList.size() : 0));

            if (petList == null || petList.isEmpty()) {
                System.out.println("宠物列表为空");

                // 如果没有宠物，显示提示
                Label noPetsLabel = new Label("暂无宠物");
                noPetsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray; -fx-alignment: center;");
                noPetsLabel.setPrefWidth(160);
                noPetsLabel.setPrefHeight(100);

                VBox vbox = new VBox(noPetsLabel);
                vbox.setAlignment(Pos.CENTER);
                vbox.setPrefWidth(160);
                vbox.setPrefHeight(300);

                petListScrollPane.setContent(vbox);
                return;
            }

            // 创建垂直布局容器
            VBox petVBox = new VBox(10);
            petVBox.setPadding(new Insets(10));
            petVBox.setPrefWidth(160);

            // 为每个宠物创建按钮
            for (Pet pet : petList) {
                Button petButton = new Button();

                // 设置按钮文本
                String buttonText = pet.getName() + " (Lv." + pet.getLevel() + ")";

                // 如果宠物已死亡，添加标记
                if (pet.getAlive() != null && !pet.getAlive()) {
                    buttonText += " [已死亡]";
                }

                petButton.setText(buttonText);
                petButton.setPrefWidth(160);
                petButton.setMinHeight(40);
                petButton.setMaxHeight(40);

                // 设置按钮样式
                petButton.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

                // 如果宠物已死亡，按钮变灰
                if (pet.getAlive() != null && !pet.getAlive()) {
                    petButton.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: gray;");
                }

                // 设置点击事件
                petButton.setOnAction(e -> {
                    System.out.println("选择宠物: " + pet.getName() + " ID: " + pet.getPetId());
                    selectPet(pet);
                });

                petVBox.getChildren().add(petButton);
            }

            // 设置滚动面板内容
            petListScrollPane.setContent(petVBox);
            petListScrollPane.setFitToWidth(true);

            // 如果有宠物，默认选择第一个
            if (!petList.isEmpty()) {
                selectPet(petList.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();

            // 显示错误信息
            Label errorLabel = new Label("加载宠物失败: " + e.getMessage());
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");

            VBox vbox = new VBox(errorLabel);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPrefWidth(160);
            vbox.setPrefHeight(300);

            petListScrollPane.setContent(vbox);
        }
    }

    /**
     * 选择宠物，显示详细信息
     */
    private void selectPet(Pet pet) {
        selectedPet = pet;
        selectedPokemon = null;

        System.out.println("显示宠物信息: " + pet.getName());

        // 显示宠物名字
        nameLabel.setText(pet.getName() != null ? pet.getName() : "未知");

        // 显示宠物ID
        idLabel.setText(String.valueOf(pet.getPetId()));

        // 显示清洁度
        int clean = pet.getClean() != null ? pet.getClean() : 0;
        cleanLabel.setText(clean + "/100");

        // 显示等级
        levelLabel.setText(String.valueOf(pet.getLevel()));

        // 显示经验
        if (expLabel != null) {
            int exp = pet.getExperience() != null ? pet.getExperience() : 0;
            expLabel.setText(exp + "/100"); // 这里应该是升级所需经验
        }

        // 显示宠物图片
        try {
            String imagePath = "/images/" + pet.getType() + ".png";
            Image petImage = new Image(getClass().getResourceAsStream(imagePath));
            petImageView.setImage(petImage);
        } catch (Exception e) {
            System.out.println("无法加载图片: " + pet.getType() + ".png");

            // 尝试加载默认图片
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_pet.png"));
                petImageView.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("也无法加载默认图片: " + ex.getMessage());
                petImageView.setImage(null);
            }
        }

        // 在GameDataManager的pokemonList中找到对应的Pokemon
        for (Pokemon p : gameDataManager.getPokemonList()) {
            if (p.getName().equals(pet.getType())) {
                selectedPokemon = p;
                System.out.println("找到对应的Pokemon: " + p.getName());
                break;
            }
        }

        // 如果没有找到，尝试通过PetFactory创建
        if (selectedPokemon == null) {
            try {
                selectedPokemon = PetFactory.createPokemon(pet);
                if (selectedPokemon != null) {
                    System.out.println("通过PetFactory创建Pokemon: " + selectedPokemon.getName());
                }
            } catch (Exception e) {
                System.err.println("创建Pokemon失败: " + e.getMessage());
            }
        }
    }

    /**
     * 更新道具数量显示
     */
    private void updateItemCounts() {
        playerBag = gameDataManager.getPlayerBag();

        if (playerBag == null) {
            System.out.println("背包为空");
            return;
        }

        // 更新道具数量标签
        if (eggCountLabel != null) {
            eggCountLabel.setText("x" + (playerBag.getEggCount() != null ? playerBag.getEggCount() : 0));
        }

        if (soapCountLabel != null) {
            soapCountLabel.setText("x" + (playerBag.getSoapCount() != null ? playerBag.getSoapCount() : 0));
        }

        if (riceCountLabel != null) {
            riceCountLabel.setText("x" + (playerBag.getRiceCount() != null ? playerBag.getRiceCount() : 0));
        }
    }

    // 道具使用按钮的事件方法
    @FXML
    private void onUseEggClick() {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }

        // 获取背包
        if (playerBag == null) {
            playerBag = gameDataManager.getPlayerBag();
        }

        if (playerBag.getEggCount() == null || playerBag.getEggCount() <= 0) {
            showAlert("提示", "蛋的数量不足");
            return;
        }

        // 使用蛋的逻辑
        useEgg();
    }

    @FXML
    private void onUseSoapClick() {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }

        if (playerBag == null) {
            playerBag = gameDataManager.getPlayerBag();
        }

        if (playerBag.getSoapCount() == null || playerBag.getSoapCount() <= 0) {
            showAlert("提示", "肥皂的数量不足");
            return;
        }

        // 使用肥皂的逻辑
        useSoap();
    }

    @FXML
    private void onUseRiceClick() {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }

        if (playerBag == null) {
            playerBag = gameDataManager.getPlayerBag();
        }

        if (playerBag.getRiceCount() == null || playerBag.getRiceCount() <= 0) {
            showAlert("提示", "米饭的数量不足");
            return;
        }

        // 使用米饭的逻辑
        useRice();
    }

    /**
     * 使用蛋：让宠物的isAlive变成1
     */
    private void useEgg() {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }
        if (playerBag.getEggCount() == null || playerBag.getEggCount() <= 0) {
            showAlert("提示", "蛋的数量不足");
            return;
        }

        // 更新Pet
        selectedPet.setAlive(true);

        // 更新Pokemon
        if (selectedPokemon != null) {
            selectedPokemon.setAlive(true);
        }

        // 减少背包中的蛋数量
        playerBag.setEggCount(playerBag.getEggCount() - 1);

        // 更新数据库
        try {
            database.PetDAO petDAO = new database.PetDAO();
            petDAO.updatePet(selectedPet);

            database.BagDAO bagDAO = new database.BagDAO();
            bagDAO.updateBag(playerBag);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("错误", "数据库更新失败：" + e.getMessage());
        }

        // 刷新界面
        loadPetList();
        if (selectedPet != null) {
            selectPet(selectedPet);
        }

        showAlert("成功", "使用蛋成功，宠物已复活");
    }

    /**
     * 使用肥皂：增加30清洁度，最大100
     */
    private void useSoap() {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }
        if (playerBag.getSoapCount() == null || playerBag.getSoapCount() <= 0) {
            showAlert("提示", "肥皂的数量不足");
            return;
        }

        // 计算新清洁度
        int currentClean = selectedPet.getClean() != null ? selectedPet.getClean() : 0;
        int newClean = Math.min(100, currentClean + 30);

        // 更新Pet
        selectedPet.setClean(newClean);

        // 更新Pokemon
        if (selectedPokemon != null) {
            selectedPokemon.setClean(newClean);
            selectedPokemon.setHp((int)(newClean * 0.01 * selectedPokemon.getMaxHp()));
        }

        // 减少背包中的肥皂数量
        playerBag.setSoapCount(playerBag.getSoapCount() - 1);

        // 更新数据库
        try {
            database.PetDAO petDAO = new database.PetDAO();
            petDAO.updatePet(selectedPet);

            database.BagDAO bagDAO = new database.BagDAO();
            bagDAO.updateBag(playerBag);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("错误", "数据库更新失败：" + e.getMessage());
        }

        // 刷新界面
        loadPetList();
        if (selectedPet != null) {
            selectPet(selectedPet);
        }

        showAlert("成功", "使用肥皂成功，清洁度增加30");
    }

    /**
     * 使用米饭：增加100经验
     */
    private void useRice() {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }
        if (playerBag.getRiceCount() == null || playerBag.getRiceCount() <= 0) {
            showAlert("提示", "米饭的数量不足");
            return;
        }

        // 计算新经验
        int currentExp = selectedPet.getExperience() != null ? selectedPet.getExperience() : 0;
        int newExp = currentExp + 100;

        // 更新Pet
        selectedPet.setExperience(newExp);

        // 更新Pokemon（如果需要升级）
        if (selectedPokemon != null) {
            selectedPokemon.setExp(newExp);
            // 检查升级逻辑（简化）
            while (selectedPokemon.getExp() >= selectedPokemon.getExpToNextLevel()) {
                selectedPokemon.levelUp();
                selectedPet.setLevel(selectedPokemon.getLevel());
            }
        }

        // 减少背包中的米饭数量
        playerBag.setRiceCount(playerBag.getRiceCount() - 1);

        // 更新数据库
        try {
            database.PetDAO petDAO = new database.PetDAO();
            petDAO.updatePet(selectedPet);

            database.BagDAO bagDAO = new database.BagDAO();
            bagDAO.updateBag(playerBag);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("错误", "数据库更新失败：" + e.getMessage());
        }

        // 刷新界面
        loadPetList();
        if (selectedPet != null) {
            selectPet(selectedPet);
        }

        showAlert("成功", "使用米饭成功，经验增加100");
    }

    /**
     * 显示提示框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}