package entity;

import java.time.LocalDateTime;

public class SaveData {
    private int slot;           //存档位编号
    private String saveName;    //存档名称
    private LocalDateTime saveTime; //存档时间
    private String playerName;  //玩家名称
    private String playTime;    //游戏时长

    public SaveData() {
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public LocalDateTime getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(LocalDateTime saveTime) {
        this.saveTime = saveTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayTime() {
        return playTime;
    }

    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    @Override
    public String toString() {
        if (saveTime == null) {
            return "存档位 " + slot + " (空)";
        }
        return "存档位 " + slot + " - " + saveTime.format(
                java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }
}