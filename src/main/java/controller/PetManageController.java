package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import entity.*;
import pokemon.Pokemon;
import service.PetFactory;
import service.GameDataManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PetManageController implements Initializable {

    @FXML
    private ScrollPane petListScrollPane;
    @FXML
    private ImageView petImageView;
    @FXML
    private Label nameLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Label cleanLabel;
    @FXML
    private Label levelLabel;
    @FXML
    private Label expLabel;
    @FXML
    private Label attackLabel;
    @FXML
    private Label defLabel;
    @FXML
    private Button useEggButton;
    @FXML
    private Button useSoapButton;
    @FXML
    private Button useRiceButton;
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
    private Spinner<Integer> appleSpinner;

    private GameDataManager gameDataManager;
    private List<Pet> petList;
    private Bag playerBag;
    private Pet selectedPet;
    private Pokemon selectedPokemon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PetManageController 初始化");

        gameDataManager = GameDataManager.getInstance();

        System.out.println("当前玩家: " + gameDataManager.getCurrentPlayer());
        System.out.println("宠物列表: " + gameDataManager.getPetList());

        initUI();
        initSpinners();
        loadPetList();
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
        attackLabel.setText("--");
        defLabel.setText("--");

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
     * 初始化Spinner组件
     */
    private void initSpinners() {
        // 初始化蛋的Spinner，默认值为1，范围1-999
        SpinnerValueFactory<Integer> eggValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1);
        eggSpinner.setValueFactory(eggValueFactory);
        eggSpinner.setEditable(true);

        // 初始化肥皂的Spinner，默认值为1，范围1-999
        SpinnerValueFactory<Integer> soapValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1);
        soapSpinner.setValueFactory(soapValueFactory);
        soapSpinner.setEditable(true);

        // 初始化苹果的Spinner，默认值为1，范围1-999
        SpinnerValueFactory<Integer> appleValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1);
        appleSpinner.setValueFactory(appleValueFactory);
        appleSpinner.setEditable(true);
    }

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

    private void selectPet(Pet pet) {
        selectedPet = pet;
        selectedPokemon = null;
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

        System.out.println("显示宠物信息: " + pet.getName());

        nameLabel.setText(pet.getName() != null ? pet.getName() : "未知");

        idLabel.setText(String.valueOf(pet.getPetId()));

        int clean = pet.getClean() != null ? pet.getClean() : 0;
        cleanLabel.setText(clean + "/100");

        levelLabel.setText(String.valueOf(pet.getLevel()));
        attackLabel.setText(String.valueOf(pet.getAttack()));
        defLabel.setText(String.valueOf(selectedPokemon.getDefense()));

        if (expLabel != null) {
            int exp = pet.getExperience() != null ? pet.getExperience() : 0;
            expLabel.setText(exp + "/" + selectedPokemon.getExpToNextLevel()); // 这里应该是升级所需经验
        }

        try {
            String imagePath = "/images/" + pet.getType() + ".png";
            Image petImage = new Image(getClass().getResourceAsStream(imagePath));
            petImageView.setImage(petImage);
        } catch (Exception e) {
            System.out.println("无法加载图片: " + pet.getType() + ".png");

            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_pet.png"));
                petImageView.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("也无法加载默认图片: " + ex.getMessage());
                petImageView.setImage(null);
            }
        }

        for (Pokemon p : gameDataManager.getPokemonList()) {
            if (p.getName().equals(pet.getType())) {
                selectedPokemon = p;
                System.out.println("找到对应的Pokemon: " + p.getName());
                break;
            }
        }

        // 选择宠物时重置Spinner值
        resetSpinners();
    }

    /**
     * 重置Spinner值为默认值1
     */
    private void resetSpinners() {
        eggSpinner.getValueFactory().setValue(1);
        soapSpinner.getValueFactory().setValue(1);
        appleSpinner.getValueFactory().setValue(1);
    }

    private void updateItemCounts() {
        playerBag = gameDataManager.getPlayerBag();

        if (playerBag == null) {
            System.out.println("背包为空");
            return;
        }

        if (eggCountLabel != null) {
            eggCountLabel.setText("x" + (playerBag.getEggCount() != null ? playerBag.getEggCount() : 0));
        }

        if (soapCountLabel != null) {
            soapCountLabel.setText("x" + (playerBag.getSoapCount() != null ? playerBag.getSoapCount() : 0));
        }

        if (riceCountLabel != null) {
            riceCountLabel.setText("x" + (playerBag.getRiceCount() != null ? playerBag.getRiceCount() : 0));
        }

        // 更新Spinner的最大值限制
        updateSpinnerMaxValues();
    }

    /**
     * 更新Spinner的最大值，不能超过背包中道具的数量
     */
    private void updateSpinnerMaxValues() {
        if (playerBag == null) return;

        int eggCount = playerBag.getEggCount() != null ? playerBag.getEggCount() : 0;
        int soapCount = playerBag.getSoapCount() != null ? playerBag.getSoapCount() : 0;
        int riceCount = playerBag.getRiceCount() != null ? playerBag.getRiceCount() : 0;

        // 更新Spinner的最大值
        SpinnerValueFactory<Integer> eggFactory = eggSpinner.getValueFactory();
        if (eggFactory != null) {
            eggFactory.setValue(Math.max(1, eggCount)); // 至少为1
            if (eggSpinner.getValue() > eggCount) {
                eggFactory.setValue(eggCount > 0 ? eggCount : 1);
            }
        }

        SpinnerValueFactory<Integer> soapFactory = soapSpinner.getValueFactory();
        if (soapFactory != null) {
            soapFactory.setValue(Math.max(1, soapCount));
            if (soapSpinner.getValue() > soapCount) {
                soapFactory.setValue(soapCount > 0 ? soapCount : 1);
            }
        }

        SpinnerValueFactory<Integer> appleFactory = appleSpinner.getValueFactory();
        if (appleFactory != null) {
            appleFactory.setValue(Math.max(1, riceCount));
            if (appleSpinner.getValue() > riceCount) {
                appleFactory.setValue(riceCount > 0 ? riceCount : 1);
            }
        }
    }

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

        int useCount = eggSpinner.getValue();
        if (useCount <= 0) {
            showAlert("提示", "使用数量必须大于0");
            return;
        }

        if (playerBag.getEggCount() == null || playerBag.getEggCount() < useCount) {
            showAlert("提示", "蛋的数量不足，当前只有 " + (playerBag.getEggCount() != null ? playerBag.getEggCount() : 0) + " 个");
            return;
        }

        // 使用蛋的逻辑
        useEgg(useCount);
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

        int useCount = soapSpinner.getValue();
        if (useCount <= 0) {
            showAlert("提示", "使用数量必须大于0");
            return;
        }

        if (playerBag.getSoapCount() == null || playerBag.getSoapCount() < useCount) {
            showAlert("提示", "肥皂的数量不足，当前只有 " + (playerBag.getSoapCount() != null ? playerBag.getSoapCount() : 0) + " 个");
            return;
        }

        // 使用肥皂的逻辑
        useSoap(useCount);
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

        int useCount = appleSpinner.getValue();
        if (useCount <= 0) {
            showAlert("提示", "使用数量必须大于0");
            return;
        }

        if (playerBag.getRiceCount() == null || playerBag.getRiceCount() < useCount) {
            showAlert("提示", "苹果的数量不足，当前只有 " + (playerBag.getRiceCount() != null ? playerBag.getRiceCount() : 0) + " 个");
            return;
        }

        // 使用米饭的逻辑
        useRice(useCount);
    }

    @FXML
    private void onExitClick() {
        Stage currentStage = (Stage) petListScrollPane.getScene().getWindow();
        currentStage.close();
    }

    /**
     * 使用蛋：让宠物的isAlive变成1
     */
    private void useEgg(int useCount) {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }
        if (playerBag.getEggCount() == null || playerBag.getEggCount() < useCount) {
            showAlert("提示", "蛋的数量不足");
            return;
        }

        // 检查宠物是否已死亡
        if (selectedPet.getAlive() != null && selectedPet.getAlive()) {
            showAlert("提示", "宠物当前是活的，不需要复活");
            return;
        }

        // 更新Pet
        selectedPet.setAlive(true);

        if (selectedPokemon != null) {
            selectedPokemon.setAlive(true);
            selectedPokemon.setClean(100);
        }

        playerBag.setEggCount(playerBag.getEggCount() - useCount);

        updatePetListUI();
        updateItemCounts();
        if (selectedPet != null) {
            selectPet(selectedPet);
        }

        showAlert("成功", "使用 " + useCount + " 个蛋成功，宠物已复活");
    }

    private void useSoap(int useCount) {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }
        if (playerBag.getSoapCount() == null || playerBag.getSoapCount() < useCount) {
            showAlert("提示", "肥皂的数量不足");
            return;
        }

        int currentClean = selectedPet.getClean() != null ? selectedPet.getClean() : 0;
        int cleanIncrease = 30 * useCount; // 每个肥皂增加30清洁度
        int newClean = Math.min(100, currentClean + cleanIncrease);

        // 更新Pet
        selectedPet.setClean(newClean);

        // 更新Pokemon
        if (selectedPokemon != null) {
            selectedPokemon.setClean(newClean);
            selectedPokemon.setHp((int)(newClean * 0.01 * selectedPokemon.getMaxHp()));
        }

        // 减少背包中的肥皂数量
        playerBag.setSoapCount(playerBag.getSoapCount() - useCount);

        // 更新UI
        updatePetListUI();
        updateItemCounts();
        if (selectedPet != null) {
            selectPet(selectedPet);
        }

        showAlert("成功", "使用 " + useCount + " 个肥皂成功，清洁度增加" + (newClean - currentClean));
    }

    private void useRice(int useCount) {
        if (selectedPet == null) {
            showAlert("提示", "请先选择一个宠物");
            return;
        }
        if (playerBag.getRiceCount() == null || playerBag.getRiceCount() < useCount) {
            showAlert("提示", "苹果的数量不足");
            return;
        }

        int expIncrease = 100 * useCount; // 每个苹果增加100经验
        int currentExp = selectedPet.getExperience() != null ? selectedPet.getExperience() : 0;
        int newExp = currentExp + expIncrease;

        // 更新经验
        selectedPet.setExperience(newExp);

        if (selectedPokemon != null) {
            selectedPokemon.setExp(newExp);

            // 检查是否可以升级
            int levelsGained = 0;
            while (selectedPokemon.getExp() >= selectedPokemon.getExpToNextLevel()) {
                selectedPokemon.levelUp();
                levelsGained++;
            }

            // 更新等级
            if (levelsGained > 0) {
                selectedPet.setLevel(selectedPokemon.getLevel());
            }
        }

        playerBag.setRiceCount(playerBag.getRiceCount() - useCount);

        updatePetListUI();
        updateItemCounts();
        if (selectedPet != null) {
            selectPet(selectedPet);
        }

        String message = "使用 " + useCount + " 个苹果成功，经验增加" + expIncrease;
        if (selectedPokemon != null && selectedPokemon.getLevel() > selectedPet.getLevel() - 1) {
            message += "，宠物升到了 " + selectedPokemon.getLevel() + " 级！";
        }

        showAlert("成功", message);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updatePetListUI() {
        VBox petVBox = (VBox) petListScrollPane.getContent();

        if (petVBox == null) {
            loadPetList();
            return;
        }

        petList = gameDataManager.getPetList();

        petVBox.getChildren().clear();

        // 重新添加宠物按钮
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
            String baseStyle = "-fx-font-size: 12px; -fx-font-weight: bold;";

            // 如果宠物已死亡，按钮变灰
            if (pet.getAlive() != null && !pet.getAlive()) {
                petButton.setStyle(baseStyle + "-fx-text-fill: gray;");
            } else {
                petButton.setStyle(baseStyle);
            }

            final Pet currentPet = pet;  // 需要final或effectively final
            petButton.setOnAction(e -> {
                System.out.println("选择宠物: " + currentPet.getName() + " ID: " + currentPet.getPetId());
                selectPet(currentPet);
            });

            petVBox.getChildren().add(petButton);
        }
    }
}