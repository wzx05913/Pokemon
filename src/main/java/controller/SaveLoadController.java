package controller;

import database.*;
import service.*;
import entity.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import Player.Player;
import pokemon.Pokemon;
import service.PetFactory;
import service.GameDataManager;

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

    private UserDAO userDAO = new UserDAO();
    private BagDAO bagDAO = new BagDAO();
    private PetDAO petDAO = new PetDAO();
    private GameDataManager dataManager = GameDataManager.getInstance();
    private Player currentPlayer; // 当前游戏中的玩家

    // 新增：只读模式标志（true 表示只读，禁止保存/删除）
    private boolean readOnly = false;

    // 新增：主控制器引用，用于读档后跳转页面
    private MainController mainController;

    // 初始化方法
    @FXML
    public void initialize() {
        currentPlayer = dataManager.getCurrentPlayer();

        saveList.setOrientation(Orientation.HORIZONTAL);

        saveList.setPrefHeight(120);  // 设置合适的高度
        saveList.setMinHeight(120);
        saveList.setMaxHeight(120);

        saveList.setMinWidth(360);

        saveList.setCellFactory(new Callback<ListView<SaveData>, ListCell<SaveData>>() {
            @Override
            public ListCell<SaveData> call(ListView<SaveData> listView) {
                return new ListCell<SaveData>() {
                    @Override
                    protected void updateItem(SaveData saveData, boolean empty) {
                        super.updateItem(saveData, empty);

                        // 设置每个单元格的宽度为1/3
                        setPrefWidth(120);  // 设置固定宽度
                        setMinWidth(120);
                        setMaxWidth(120);

                        setAlignment(Pos.CENTER_LEFT);
                        setWrapText(true);

                        if (empty || saveData == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(saveData.toString());

                            // 根据存档状态设置不同的样式
                            if (saveData.getSaveTime() != null) {
                                // 如果是当前玩家，特殊样式
                                if (currentPlayer != null && saveData.getPlayerName() != null) {
                                    try {
                                        int playerId = Integer.parseInt(saveData.getPlayerName());
                                        if (currentPlayer.getId() == playerId) {
                                            setStyle("-fx-background-color: rgba(173, 216, 230, 0.5); " +
                                                    "-fx-border-color: #3498db; " +
                                                    "-fx-border-width: 2; " +
                                                    "-fx-border-radius: 5; " +
                                                    "-fx-padding: 10px; " +
                                                    "-fx-text-fill: #2c3e50; " +
                                                    "-fx-font-family: 'Ark Pixel'; " +
                                                    "-fx-font-size: 10px; " +  // 字体小一点
                                                    "-fx-background-radius: 5;");
                                        } else {
                                            setStyle("-fx-background-color: rgba(240, 240, 240, 0.5); " +
                                                    "-fx-border-color: #95a5a6; " +
                                                    "-fx-border-width: 1; " +
                                                    "-fx-border-radius: 5; " +
                                                    "-fx-padding: 10px; " +
                                                    "-fx-text-fill: #34495e; " +
                                                    "-fx-font-family: 'Ark Pixel'; " +
                                                    "-fx-font-size: 10px; " +  // 字体小一点
                                                    "-fx-background-radius: 5;");
                                        }
                                    } catch (NumberFormatException e) {
                                        setStyle("-fx-background-color: rgba(240, 240, 240, 0.5); " +
                                                "-fx-border-color: #95a5a6; " +
                                                "-fx-border-width: 1; " +
                                                "-fx-border-radius: 5; " +
                                                "-fx-padding: 10px; " +
                                                "-fx-text-fill: #34495e; " +
                                                "-fx-font-family: 'Ark Pixel'; " +
                                                "-fx-font-size: 10px; " +  // 字体小一点
                                                "-fx-background-radius: 5;");
                                    }
                                } else {
                                    setStyle("-fx-background-color: rgba(240, 240, 240, 0.5); " +
                                            "-fx-border-color: #95a5a6; " +
                                            "-fx-border-width: 1; " +
                                            "-fx-border-radius: 5; " +
                                            "-fx-padding: 10px; " +
                                            "-fx-text-fill: #34495e; " +
                                            "-fx-font-family: 'Ark Pixel'; " +
                                            "-fx-font-size: 10px; " +  // 字体小一点
                                            "-fx-background-radius: 5;");
                                }
                            } else {
                                setStyle("-fx-background-color: rgba(200, 200, 200, 0.3); " +
                                        "-fx-border-color: #bdc3c7; " +
                                        "-fx-border-width: 1; " +
                                        "-fx-border-radius: 5; " +
                                        "-fx-padding: 10px; " +
                                        "-fx-text-fill: #7f8c8d; " +
                                        "-fx-font-family: 'Ark Pixel'; " +
                                        "-fx-font-size: 10px; " +  // 字体小一点
                                        "-fx-font-style: italic; " +
                                        "-fx-background-radius: 5;");
                            }

                            // 选中的高亮效果
                            if (isSelected()) {
                                setStyle("-fx-background-color: linear-gradient(to bottom, #fff8c4, #ffeb3b);" +
                                        "-fx-border-color: #ff9800;" +
                                        "-fx-border-width: 2;" +
                                        "-fx-border-radius: 5;" +
                                        "-fx-padding: 10px;" +
                                        "-fx-text-fill: #333;" +
                                        "-fx-font-family: 'Ark Pixel';" +
                                        "-fx-font-size: 10px;" +
                                        "-fx-background-radius: 5;" +
                                        "-fx-effect: dropshadow(gaussian, rgba(255,215,0,0.5), 10, 0, 0, 0);");
                            }
                        }
                    }
                };
            }
        });

        // 加载存档数据
        loadSaveSlots();

        // 初始按钮状态
        updateButtonStates();

        // 添加选择监听器
        saveList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateButtonStates();
                    updateStatusLabel();
                });
    }


    // 加载所有存档位
    private void loadSaveSlots() {
        try {
            // 获取所有用户
            List<User> users = userDAO.getAllUsers();

            // 创建3个存档位
            SaveData[] saveSlots = new SaveData[3];

            for (int i = 0; i < 3; i++) {
                saveSlots[i] = new SaveData();
                saveSlots[i].setSlot(i + 1);
            }

            // 将用户分配到存档位
            int slotIndex = 0;
            for (User user : users) {
                if (slotIndex < 3) {
                    SaveData saveData = saveSlots[slotIndex];

                    // 设置玩家名称为用户ID
                    saveData.setPlayerName(String.valueOf(user.getUserId()));
                    saveData.setSaveTime(LocalDateTime.now());

                    // 获取玩家数据
                    try {
                        Bag bag = bagDAO.getBagByUserId(user.getUserId());
                        List<Pet> pets = petDAO.getPetsByUserId(user.getUserId());

                        // 计算总金币和经验
                        int totalCoins = bag != null ? bag.getCoins() : 0;
                        int petCount = (pets != null) ? pets.size() : 0;

                        // 设置游戏时长
                        int playTimeMinutes = (int) (Math.random() * 300);
                        int hours = playTimeMinutes / 60;
                        int minutes = playTimeMinutes % 60;
                        saveData.setPlayTime(String.format("%d小时%d分钟", hours, minutes));

                        // 设置存档名称
                        saveData.setSaveName(String.format("金币:%d 宠物:%d", totalCoins, petCount));

                    } catch (SQLException e) {
                        System.err.println("加载用户数据失败: " + e.getMessage());
                        saveData.setPlayTime("0分钟");
                        saveData.setSaveName("新存档");
                    }

                    slotIndex++;
                }
            }

            // 添加到ListView
            saveList.getItems().clear();
            for (SaveData saveData : saveSlots) {
                saveList.getItems().add(saveData);
            }

        } catch (SQLException e) {
            showAlert("错误", "加载存档失败: " + e.getMessage(), AlertType.ERROR);
        }
    }

    // 更新按钮状态
    private void updateButtonStates() {
        SaveData selectedSave = saveList.getSelectionModel().getSelectedItem();

        if (selectedSave != null) {
            if (selectedSave.getSaveTime() != null) {
                // 存档位已被占用
                saveButton.setText("覆盖");
                saveButton.setDisable(false);
                loadButton.setDisable(false);
                deleteButton.setDisable(false);

                // 如果是当前玩家，可以读取
                if (currentPlayer != null && selectedSave.getPlayerName() != null) {
                    try {
                        int playerId = Integer.parseInt(selectedSave.getPlayerName());
                        if (currentPlayer.getId() == playerId) {
                            loadButton.setText("继续游戏");
                        } else {
                            loadButton.setText("读取");
                        }
                    } catch (NumberFormatException e) {
                        loadButton.setText("读取");
                    }
                } else {
                    loadButton.setText("读取");
                }
            } else {
                // 空存档位
                saveButton.setText("创建存档");
                saveButton.setDisable(false);
                loadButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        } else {
            // 未选中任何存档位
            saveButton.setDisable(true);
            loadButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        // 只读模式：禁止保存和删除（但允许读取已经存在的存档）
        if (readOnly) {
            saveButton.setDisable(true);
            deleteButton.setDisable(true);
            // 如果选中是空位，则读取按钮也应该禁用
            SaveData sel = saveList.getSelectionModel().getSelectedItem();
            if (sel == null || sel.getSaveTime() == null) {
                loadButton.setDisable(true);
            }
        }
    }

    // 更新状态标签
    private void updateStatusLabel() {
        SaveData selectedSave = saveList.getSelectionModel().getSelectedItem();

        if (selectedSave == null) {
            statusLabel.setText("请选择一个存档位");
            return;
        }

        if (selectedSave.getSaveTime() != null) {
            StringBuilder status = new StringBuilder();
            status.append("存档位: ").append(selectedSave.getSlot()).append(" | ");

            if (selectedSave.getPlayerName() != null) {
                status.append("玩家ID: ").append(selectedSave.getPlayerName()).append(" | ");
            }

            if (selectedSave.getSaveTime() != null) {
                String formattedTime = selectedSave.getSaveTime().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                status.append("存档时间: ").append(formattedTime).append(" | ");
            }

            if (selectedSave.getPlayTime() != null) {
                status.append("游戏时长: ").append(selectedSave.getPlayTime());
            }

            if (selectedSave.getSaveName() != null) {
                status.append("\n").append(selectedSave.getSaveName());
            }

            if (currentPlayer != null && selectedSave.getPlayerName() != null) {
                try {
                    int playerId = Integer.parseInt(selectedSave.getPlayerName());
                    if (currentPlayer.getId() == playerId) {
                        status.append(" (当前玩家)");
                    }
                } catch (NumberFormatException e) {
                    // 忽略格式错误
                }
            }

            statusLabel.setText(status.toString());
        } else {
            statusLabel.setText("空存档位 - 点击'创建存档'保存当前进度");
        }
    }

    // 保存按钮点击事件
    @FXML
    private void onSaveButtonClick() {
        if (readOnly) {
            showAlert("提示", "当前为只读模式，无法保存。", AlertType.INFORMATION);
            return;
        }

        SaveData selectedSave = saveList.getSelectionModel().getSelectedItem();

        if (selectedSave == null) {
            showAlert("提示", "请先选择一个存档位", AlertType.WARNING);
            return;
        }

        if (currentPlayer == null) {
            showAlert("错误", "当前没有玩家，无法保存", AlertType.ERROR);
            return;
        }

        try {
            boolean isGuest = (currentPlayer.getId() == -1);

            if (selectedSave.getSaveTime() != null) {
                // 确认覆盖
                Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
                confirmAlert.setTitle("确认覆盖");
                confirmAlert.setHeaderText("覆盖存档");

                String oldPlayerIdStr = selectedSave.getPlayerName();
                if (oldPlayerIdStr != null) {
                    int oldPlayerId = Integer.parseInt(oldPlayerIdStr);
                    boolean isOverwritingGuest = (oldPlayerId == -1);

                    if (isGuest && isOverwritingGuest) {
                        confirmAlert.setContentText("确定要覆盖游客存档吗？");
                    } else if (isGuest) {
                        confirmAlert.setContentText("确定要用游客数据覆盖存档位 " + selectedSave.getSlot() + " 吗？");
                    } else if (isOverwritingGuest) {
                        confirmAlert.setContentText("确定要用正式玩家数据覆盖游客存档吗？");
                    } else {
                        confirmAlert.setContentText("确定要覆盖存档位 " + selectedSave.getSlot() + " 吗？\n" +
                                "原有的存档数据将会丢失！");
                    }
                } else {
                    confirmAlert.setContentText("确定要覆盖存档位 " + selectedSave.getSlot() + " 吗？");
                }

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // 获取旧玩家ID
                    oldPlayerIdStr = selectedSave.getPlayerName();
                    if (oldPlayerIdStr != null) {
                        try {
                            int oldPlayerId = Integer.parseInt(oldPlayerIdStr);

                            // 如果是游客存档（oldPlayerId == -1），不需要删除数据库数据
                            if (oldPlayerId != -1) {
                                // 删除原有数据（只删除非游客的数据）
                                petDAO.deletePetsByUserId(oldPlayerId);
                                bagDAO.deleteBagByUserId(oldPlayerId);
                                userDAO.deleteUser(oldPlayerId);
                                System.out.println("DEBUG: 删除了旧玩家数据，ID: " + oldPlayerId);
                            } else {
                                System.out.println("DEBUG: 覆盖的是游客存档，跳过数据库删除");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("旧玩家ID格式错误: " + oldPlayerIdStr);
                        }
                    }
                    saveCurrentPlayer(selectedSave);
                }
            } else {
                // 创建新存档
                saveCurrentPlayer(selectedSave);
            }

            // 重新加载存档列表
            loadSaveSlots();
            saveList.getSelectionModel().select(selectedSave);

        } catch (SQLException e) {
            showAlert("错误", "保存失败: " + e.getMessage(), AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("错误", "玩家ID格式错误", AlertType.ERROR);
        }
    }

    private void saveCurrentPlayer(SaveData saveData) throws SQLException {
        boolean isGuest = (currentPlayer.getId() == -1);

        User dbUser;
        if (isGuest) {
            // 游客：创建新用户（产生新编号）
            dbUser = userDAO.createUser();
            System.out.println("DEBUG: 为游客创建数据库用户，新ID: " + dbUser.getUserId());
            // 将新编号赋给当前玩家
            currentPlayer.setId(dbUser.getUserId());
        } else {
            // 非游客：必须沿用当前编号
            int expectedId = currentPlayer.getId();
            dbUser = userDAO.getUserById(expectedId);
            if (dbUser == null) {
                // 如果数据库中没有该用户，创建新用户
                userDAO.ensureUserExistsWithId(expectedId);
                dbUser = new User(expectedId);
                System.out.println("DEBUG: 为现有玩家补建数据库记录，ID: " + expectedId);
            }
        }
        int playerId = dbUser.getUserId();

        // 保存背包数据：优先使用 GameDataManager 中的内存背包
        Bag bag = new Bag();
        bag.setUserId(playerId);
        Bag currentBag = dataManager.getPlayerBag();

        if (currentBag != null) {
            // 确保所有字段都有值，避免null
            bag.setEggCount(currentBag.getEggCount() != null ? currentBag.getEggCount() : 0);
            bag.setRiceCount(currentBag.getRiceCount() != null ? currentBag.getRiceCount() : 0);
            bag.setSoapCount(currentBag.getSoapCount() != null ? currentBag.getSoapCount() : 0);

            // 金币处理：优先使用背包的金币，如果没有则使用玩家的金币
            Integer coins = currentBag.getCoins();
            if (coins == null) {
                coins = (currentPlayer != null) ? currentPlayer.getMoney() : 0;
            }
            bag.setCoins(coins);
        } else {
            // 如果没有内存背包，使用默认值
            bag.setEggCount(0);
            bag.setRiceCount(0);
            bag.setSoapCount(0);
            bag.setCoins(currentPlayer != null ? currentPlayer.getMoney() : 0);
        }

        // 保存或更新背包
        Bag existingBag = bagDAO.getBagByUserId(playerId);
        if (existingBag != null) {
            bag.setBagId(existingBag.getBagId());
            bagDAO.updateBag(bag);
            System.out.println("DEBUG: 更新背包数据，玩家ID: " + playerId);
        } else {
            bagDAO.createBag(bag);
            System.out.println("DEBUG: 创建新背包数据，玩家ID: " + playerId);
        }

        // 保存宠物数据
        List<Pokemon> pokemons = currentPlayer.getPets();
        if (pokemons != null && !pokemons.isEmpty()) {
            // 先删除旧宠物（如果存在）
            petDAO.deletePetsByUserId(playerId);

            // 保存新宠物
            int savedCount = 0;
            for (Pokemon pokemon : pokemons) {
                try {
                    Pet pet = new Pet();
                    pet.setUserId(playerId);

                    // 宠物名称校验
                    String pokemonName = pokemon.getName();
                    if (pokemonName == null || pokemonName.trim().isEmpty()) {
                        System.err.println("DEBUG: 跳过空名称的宠物");
                        continue;
                    }

                    pokemonName = pokemonName.trim();

                    // 如果是游客，允许英文名称（因为游客可能捕获了英文名的宠物）
                    if (!isGuest && !pokemonName.matches(".*[\\u4e00-\\u9fa5].*")) {
                        System.err.println("DEBUG: 跳过非中文名称的宠物: " + pokemonName);
                        continue;
                    }

                    // 设置宠物属性
                    pet.setType(pokemonName);
                    pet.setLevel(pokemon.getLevel());
                    pet.setAttack(pokemon.getAttack());
                    pet.setClean(pokemon.getClean());
                    pet.setExperience(pokemon.getExp());
                    pet.setAlive(pokemon.isAlive());

                    petDAO.createPet(pet);
                    savedCount++;

                    System.out.println("DEBUG: 保存宠物: " + pokemonName + " Lv." + pokemon.getLevel());

                } catch (Exception e) {
                    System.err.println("DEBUG: 保存宠物失败: " + e.getMessage());
                }
            }
            System.out.println("DEBUG: 成功保存 " + savedCount + " 只宠物");
        } else {
            System.out.println("DEBUG: 没有宠物需要保存");
        }

        // 更新存档信息
        saveData.setPlayerName(String.valueOf(playerId));
        saveData.setSaveTime(LocalDateTime.now());

        // 计算游戏时长（简化）
        int playTimeMinutes = 30;
        int hours = playTimeMinutes / 60;
        int minutes = playTimeMinutes % 60;
        saveData.setPlayTime(String.format("%d小时%d分钟", hours, minutes));

        // 更新存档名称
        int coinCount = (currentPlayer != null) ? currentPlayer.getMoney() : 0;
        int petCount = (pokemons != null) ? pokemons.size() : 0;
        saveData.setSaveName(String.format("金币:%d 宠物:%d", coinCount, petCount));

        String message = isGuest ?
                "游客数据已保存为正式存档！新玩家ID: " + playerId :
                "存档成功！玩家ID: " + playerId;

        showAlert("成功", message + "\n存档位置: " + saveData.getSlot(), AlertType.INFORMATION);

        // 更新GameDataManager中的玩家ID
        dataManager.setCurrentUserId(playerId);
    }

    @FXML
    private void onLoadButtonClick() {
        SaveData selectedSave = saveList.getSelectionModel().getSelectedItem();

        if (selectedSave == null || selectedSave.getSaveTime() == null) {
            showAlert("错误", "请选择一个有效的存档", AlertType.ERROR);
            return;
        }

        try {
            String playerIdStr = selectedSave.getPlayerName();
            if (playerIdStr == null) {
                showAlert("错误", "存档数据损坏，无法读取", AlertType.ERROR);
                return;
            }

            int playerId = Integer.parseInt(playerIdStr);

            // 加载数据库数据
            Bag loadedBag = bagDAO.getBagByUserId(playerId);
            List<Pet> loadedPets = petDAO.getPetsByUserId(playerId);

            if (loadedBag == null) {
                showAlert("错误", "存档数据不完整，无法读取", AlertType.ERROR);
                return;
            }

            // 创建Player对象
            Player loadedPlayer = new Player(loadedBag.getCoins(), playerId);

            // 创建Pokemon对象并收集到本地列表
            List<pokemon.Pokemon> createdPokemons = new ArrayList<>();
            if (loadedPets != null && !loadedPets.isEmpty()) {
                for (Pet pet : loadedPets) {
                    String petType = pet.getType();
                    if (petType == null || petType.trim().isEmpty()) {
                        petType = "Bulbasaur";
                    }

                        pokemon.Pokemon pokemon = PetFactory.createPokemon(pet);
                    if (pokemon != null) {
                        loadedPlayer.addPet(pokemon);
                        createdPokemons.add(pokemon);
                    } else {
                        System.err.println("创建宠物失败: Type=" + petType);
                    }
                }
            }

            // --- 关键：将所有读取的数据同步回 GameDataManager ---
            dataManager.setCurrentPlayer(loadedPlayer);       // 设置当前 Player（会更新 currentUserId）
            dataManager.setCurrentBag(loadedBag);             // 设置背包信息
            // 同步实体 Pet 列表到 manager（用于其他模块读取实体列表）
            dataManager.getPetList().clear();
            if (loadedPets != null) dataManager.getPetList().addAll(loadedPets);
            // 同步已创建的 Pokemon 对象到 manager 的 pokemonList
            // （保留全局可用的 Pokemon 实例，便于界面/战斗复用）
            // 首先清理旧列表（保证一致性）
            List<pokemon.Pokemon> existing = dataManager.getPokemonList();
            existing.clear();
            for (pokemon.Pokemon p : createdPokemons) {
                dataManager.addPokemon(p);
            }
            // 设置当前宝可梦
            if (!createdPokemons.isEmpty()) {
                dataManager.setCurrentPokemon(createdPokemons.get(0));
            } else {
                dataManager.setCurrentPokemon(null);
            }
            // ----------------------------------------------------

            // 更新当前玩家引用
            currentPlayer = loadedPlayer;

            // 如果是“继续游戏”从主菜单打开（mainController 不为 null），则关闭窗口并切换到 bedroom-select 页面
            if (mainController != null) {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
                mainController.switchToPageWithFade("main");
                Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
                return;
            }

            // 否则显示成功提示并刷新界面
            StringBuilder petInfo = new StringBuilder();
            if (loadedPlayer.hasPets()) {
                for (pokemon.Pokemon pokemon : loadedPlayer.getPets()) {
                    petInfo.append("\n- ").append(pokemon.getName())
                            .append(" Lv.").append(pokemon.getLevel());
                }
            }

            showAlert("成功", "读取存档成功！\n玩家ID: " + playerId +
                            "\n金币: " + loadedBag.getCoins() +
                            "\n宠物数量: " + (loadedPets != null ? loadedPets.size() : 0) +
                            petInfo.toString(),
                    AlertType.INFORMATION);

            // 更新显示
            loadSaveSlots();
            saveList.getSelectionModel().select(selectedSave);

        } catch (SQLException e) {
            showAlert("错误", "读取失败: " + e.getMessage(), AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("错误", "玩家ID格式错误", AlertType.ERROR);
        }
    }

    // 删除按钮点击事件
    @FXML
    private void onDeleteButtonClick() {
        if (readOnly) {
            showAlert("提示", "当前为只读模式，无法删除存档。", AlertType.INFORMATION);
            return;
        }

        SaveData selectedSave = saveList.getSelectionModel().getSelectedItem();

        if (selectedSave == null || selectedSave.getSaveTime() == null) {
            showAlert("错误", "请选择一个有效的存档", AlertType.ERROR);
            return;
        }

        // 确认删除
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("删除存档");
        confirmAlert.setContentText("确定要删除存档位 " + selectedSave.getSlot() + " 吗？\n" +
                "此操作不可恢复！");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String playerIdStr = selectedSave.getPlayerName();
                if (playerIdStr != null) {
                    int playerId = Integer.parseInt(playerIdStr);

                    // 删除数据库数据
                    petDAO.deletePetsByUserId(playerId);
                    bagDAO.deleteBagByUserId(playerId);
                    userDAO.deleteUser(playerId);

                    // 清空存档信息
                    selectedSave.setPlayerName(null);
                    selectedSave.setSaveTime(null);
                    selectedSave.setSaveName(null);
                    selectedSave.setPlayTime(null);

                    // 如果删除的是当前玩家
                    if (currentPlayer != null && currentPlayer.getId() == playerId) {
                        dataManager.setCurrentPlayer(null);
                        currentPlayer = null;
                    }

                    // 更新显示
                    saveList.refresh();
                    updateButtonStates();
                    updateStatusLabel();

                    showAlert("成功", "删除存档成功", AlertType.INFORMATION);
                }
            } catch (SQLException e) {
                showAlert("错误", "删除失败: " + e.getMessage(), AlertType.ERROR);
            } catch (NumberFormatException e) {
                showAlert("错误", "玩家ID格式错误", AlertType.ERROR);
            }
        }
    }

    // 关闭按钮点击事件
    @FXML
    private void onCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // 显示提示框
    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 设置当前玩家
    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
        loadSaveSlots();
        updateButtonStates();
    }

    // 新增：设置只读模式（true = 只读）
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        // 立即刷新按钮状态
        updateButtonStates();
    }

    // 新增：设置主控制器引用（用于读档后跳转）
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}