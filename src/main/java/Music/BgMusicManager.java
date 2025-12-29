package Music;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class BgMusicManager {
    private static BgMusicManager instance;
    private MediaPlayer currentPlayer;
    private final Map<String, String> sceneMusicFiles;
    private static boolean musicEnabled = true; // 添加这个静态变量

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!enabled && getInstance().currentPlayer != null) {
            getInstance().currentPlayer.stop();
        }
    }

    private BgMusicManager() {
        sceneMusicFiles = new HashMap<>();
        sceneMusicFiles.put("cover", "cover.wav");
        sceneMusicFiles.put("bedroom", "bedroom.wav");
        sceneMusicFiles.put("maze", "maze.wav");
        sceneMusicFiles.put("battle", "battle.wav");
    }

    public static BgMusicManager getInstance() {
        if (instance == null) {
            instance = new BgMusicManager();
        }
        return instance;
    }

    // 切换场景音乐
    public void playSceneMusic(String sceneName) {
        if (!musicEnabled) {
            return; // 如果音乐被禁用，直接返回
        }

        String musicFile = sceneMusicFiles.get(sceneName);
        if (musicFile == null) {
            return;
        }
        playMusic(musicFile);
    }

    private void playMusic(String filename) {
        try {
            stopMusic();
            if (!musicEnabled) return; // 再次检查

            String resourcePath = "/music/" + filename;
            java.net.URL resource = BgMusicManager.class.getResource(resourcePath);
            if (resource == null) {
                System.out.println("没找到音乐资源: " + resourcePath);
                return;
            }
            Media media = new Media(resource.toExternalForm());
            currentPlayer = new MediaPlayer(media);
            currentPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            currentPlayer.setOnError(() -> System.out.println("MediaPlayer错误: " + currentPlayer.getError()));
            media.setOnError(() -> System.out.println("Media错误: " + media.getError()));
            currentPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer.dispose();
            currentPlayer = null;
        }
    }
}