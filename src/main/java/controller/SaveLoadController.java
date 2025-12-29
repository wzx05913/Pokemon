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
    private Player currentPlayer;
    private boolean readOnly = false;
    private MainController mainController;

    @FXML
    public void initialize() {
        currentPlayer = dataManager.getCurrentPlayer();
        saveList.setOrientation(Orientation.HORIZONTAL);
        saveList.setPrefHeight(120);
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
                        setPrefWidth(120);
                        setMinWidth(120);
                        setMaxWidth(120);
                        setAlignment(Pos.CENTER_LEFT);
                        setWrapText(true);

                        if (empty || saveData == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(saveData.toString());

                            if (saveData.getSaveTime() != null) {
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
                                                    "-fx-font-size: 10px; " +
                                                    "-fx-background-radius: 5;");
                                        } else {
                                            setStyle("-fx-background-color: rgba(240, 240, 240, 0.5); " +
                                                    "-fx-border-color: #95a5a6; " +
                                                    "-fx-border-width: 1; " +
                                                    "-fx-border-radius: 5; " +
                                                    "-fx-padding: 10px; " +
                                                    "-fx-text-fill: #34495e; " +
                                                    "-fx-font-family: 'Ark Pixel'; " +
                                                    "-fx-font-size: 10px; " +
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
                                                "-fx-font-size: 10px; " +
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
                                            "-fx-font-size: 10px; " +
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
                                        "-fx-font-size: 10px; " +
                                        "-fx-font-style: italic; " +
                                        "-fx-background-radius: 5;");
                            }

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

        loadSaveSlots();
        updateButtonStates();
        saveList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateButtonStates();
                    updateStatusLabel();
                });
    }

    private void loadSaveSlots() {
        try {
            List<User> users = userDAO.getAllUsers();
            SaveData[] saveSlots = new SaveData[3];
            for (int i = 0; i < 3; i++) {
                saveSlots[i] = new SaveData();
                saveSlots[i].setSlot(i + 1);
            }

            int slotIndex = 0;
            for (User user : users) {
                if (slotIndex < 3) {
                    SaveData saveData = saveSlots[slotIndex];
                    saveData.setPlayerName(String.valueOf(user.getUserId()));
                    saveData.setSaveTime(LocalDateTime.now());

                    try {
                        Bag bag = bagDAO.getBagByUserId(user.getUserId());
                        List<Pet> pets = petDAO.getPetsByUserId(user.getUserId());
                        int totalCoins = bag != null ? bag.getCoins() : 0;
                        int petCount = (pets != null) ? pets.size() : 0;
                        int playTimeMinutes = (int) (Math.random() * 300);
                        int hours = playTimeMinutes / 60;
                        int minutes = playTimeMinutes % 60;
                        saveData.setPlayTime(String.format("%d小时%d分钟", hours, minutes));
                        saveData.setSaveName(String.format("金币:%d 宠物:%d", totalCoins, petCount));
                    } catch (SQLException e) {
                        saveData.setPlayTime("0分钟");
                        saveData.setSaveName("新存档");
                    }
                    slotIndex++;
                }
            }

            saveList.getItems().clear();
            for (SaveData saveData : saveSlots) {
                saveList.getItems().add(saveData);
            }
        } catch (SQLException e) {
            showAlert("错误", "加载存档失败: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void updateButtonStates() {
        SaveData selectedSave = saveList.getSelectionModel().getSelectedItem();

        if (selectedSave != null) {
            if (selectedSave.getSaveTime() != null) {
                saveButton.setText("覆盖");
                saveButton.setDisable(false);
                loadButton.setDisable(false);
                deleteButton.setDisable(false);

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
                saveButton.setText("创建存档");
                saveButton.setDisable(false);
                loadButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        } else {
            saveButton.setDisable(true);
            loadButton.setDisable(true);
            deleteButton.setDisable(true);
        }

        if (readOnly) {
            saveButton.setDisable(true);
            deleteButton.setDisable(true);
            SaveData sel = saveList.getSelectionModel().getSelectedItem();
            if (sel == null || sel.getSaveTime() == null) {
                loadButton.setDisable(true);
            }
        }
    }

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
                } catch (NumberFormatException e) {}
            }
            statusLabel.setText(status.toString());
        } else {
            statusLabel.setText("空存档位 - 点击'创建存档'保存当前进度");
        }
    }

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
                    oldPlayerIdStr = selectedSave.getPlayerName();
                    if (oldPlayerIdStr != null) {
                        try {
                            int oldPlayerId = Integer.parseInt(oldPlayerIdStr);
                            if (oldPlayerId != -1) {
                                petDAO.deletePetsByUserId(oldPlayerId);
                                bagDAO.deleteBagByUserId(oldPlayerId);
                                userDAO.deleteUser(oldPlayerId);
                            }
                        } catch (NumberFormatException e) {}
                    }
                    saveCurrentPlayer(selectedSave);
                }
            } else {
                saveCurrentPlayer(selectedSave);
            }

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
            dbUser = userDAO.createUser();
            currentPlayer.setId(dbUser.getUserId());
        } else {
            int expectedId = currentPlayer.getId();
            dbUser = userDAO.getUserById(expectedId);
            if (dbUser == null) {
                userDAO.ensureUserExistsWithId(expectedId);
                dbUser = new User(expectedId);
            }
        }
        int playerId = dbUser.getUserId();

        Bag bag = new Bag();
        bag.setUserId(playerId);
        Bag currentBag = dataManager.getPlayerBag();

        if (currentBag != null) {
            bag.setEggCount(currentBag.getEggCount() != null ? currentBag.getEggCount() : 0);
            bag.setRiceCount(currentBag.getRiceCount() != null ? currentBag.getRiceCount() : 0);
            bag.setSoapCount(currentBag.getSoapCount() != null ? currentBag.getSoapCount() : 0);
            Integer coins = currentBag.getCoins();
            if (coins == null) {
                coins = (currentPlayer != null) ? currentPlayer.getMoney() : 0;
            }
            bag.setCoins(coins);
        } else {
            bag.setEggCount(0);
            bag.setRiceCount(0);
            bag.setSoapCount(0);
            bag.setCoins(currentPlayer != null ? currentPlayer.getMoney() : 0);
        }

        Bag existingBag = bagDAO.getBagByUserId(playerId);
        if (existingBag != null) {
            bag.setBagId(existingBag.getBagId());
            bagDAO.updateBag(bag);
        } else {
            bagDAO.createBag(bag);
        }

        List<Pokemon> pokemons = currentPlayer.getPets();
        if (pokemons != null && !pokemons.isEmpty()) {
            petDAO.deletePetsByUserId(playerId);
            int savedCount = 0;
            for (Pokemon pokemon : pokemons) {
                try {
                    Pet pet = new Pet();
                    pet.setUserId(playerId);
                    String pokemonName = pokemon.getName();
                    if (pokemonName == null || pokemonName.trim().isEmpty()) {
                        continue;
                    }
                    pokemonName = pokemonName.trim();
                    if (!isGuest && !pokemonName.matches(".*[\\u4e00-\\u9fa5].*")) {
                        continue;
                    }
                    pet.setType(pokemonName);
                    pet.setLevel(pokemon.getLevel());
                    pet.setAttack(pokemon.getAttack());
                    pet.setClean(pokemon.getClean());
                    pet.setExperience(pokemon.getExp());
                    pet.setAlive(pokemon.isAlive());
                    petDAO.createPet(pet);
                    savedCount++;
                } catch (Exception e) {}
            }
        }

        saveData.setPlayerName(String.valueOf(playerId));
        saveData.setSaveTime(LocalDateTime.now());
        int playTimeMinutes = 30;
        int hours = playTimeMinutes / 60;
        int minutes = playTimeMinutes % 60;
        saveData.setPlayTime(String.format("%d小时%d分钟", hours, minutes));
        int coinCount = (currentPlayer != null) ? currentPlayer.getMoney() : 0;
        int petCount = (pokemons != null) ? pokemons.size() : 0;
        saveData.setSaveName(String.format("金币:%d 宠物:%d", coinCount, petCount));

        String message = isGuest ?
                "游客数据已保存为正式存档！新玩家ID: " + playerId :
                "存档成功！玩家ID: " + playerId;
        showAlert("成功", message + "\n存档位置: " + saveData.getSlot(), AlertType.INFORMATION);
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
            Bag loadedBag = bagDAO.getBagByUserId(playerId);
            List<Pet> loadedPets = petDAO.getPetsByUserId(playerId);

            if (loadedBag == null) {
                showAlert("错误", "存档数据不完整，无法读取", AlertType.ERROR);
                return;
            }

            Player loadedPlayer = new Player(loadedBag.getCoins(), playerId);
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
                    }
                }
            }

            dataManager.setCurrentPlayer(loadedPlayer);
            dataManager.setCurrentBag(loadedBag);
            dataManager.getPetList().clear();
            if (loadedPets != null) dataManager.getPetList().addAll(loadedPets);
            List<pokemon.Pokemon> existing = dataManager.getPokemonList();
            existing.clear();
            for (pokemon.Pokemon p : createdPokemons) {
                dataManager.addPokemon(p);
            }
            if (!createdPokemons.isEmpty()) {
                dataManager.setCurrentPokemon(createdPokemons.get(0));
            } else {
                dataManager.setCurrentPokemon(null);
            }

            currentPlayer = loadedPlayer;

            if (mainController != null) {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
                mainController.switchToPageWithFade("main");
                Music.BgMusicManager.getInstance().playSceneMusic("bedroom");
                return;
            }

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

            loadSaveSlots();
            saveList.getSelectionModel().select(selectedSave);
        } catch (SQLException e) {
            showAlert("错误", "读取失败: " + e.getMessage(), AlertType.ERROR);
        } catch (NumberFormatException e) {
            showAlert("错误", "玩家ID格式错误", AlertType.ERROR);
        }
    }

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
                    petDAO.deletePetsByUserId(playerId);
                    bagDAO.deleteBagByUserId(playerId);
                    userDAO.deleteUser(playerId);
                    selectedSave.setPlayerName(null);
                    selectedSave.setSaveTime(null);
                    selectedSave.setSaveName(null);
                    selectedSave.setPlayTime(null);
                    if (currentPlayer != null && currentPlayer.getId() == playerId) {
                        dataManager.setCurrentPlayer(null);
                        currentPlayer = null;
                    }
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

    @FXML
    private void onCloseButtonClick() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
        loadSaveSlots();
        updateButtonStates();
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        updateButtonStates();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}