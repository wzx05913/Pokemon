package service;

import Player.Player;
import pokemon.Pokemon;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static SessionManager instance;
    private Player currentPlayer;  // 改为Player
    private Pokemon currentPokemon;
    private List<Pokemon> allPokemons = new ArrayList<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    public Pokemon getCurrentPokemon() {
        return currentPokemon;
    }

    public void setCurrentPokemon(Pokemon pokemon) {
        this.currentPokemon = pokemon;
    }

    public List<Pokemon> getAllPokemons() {
        return allPokemons;
    }

    public void addPokemon(Pokemon pokemon) {
        allPokemons.add(pokemon);
    }

    public void clearPokemons() {
        allPokemons.clear();
    }

    // 获取当前玩家ID
    public int getCurrentPlayerId() {
        return currentPlayer != null ? currentPlayer.getId() : -1;
    }

    // 获取当前玩家金币
    public int getCurrentPlayerMoney() {
        return currentPlayer != null ? currentPlayer.getMoney() : 0;
    }

    // 设置当前玩家金币
    public void setCurrentPlayerMoney(int money) {
        if (currentPlayer != null) {
            currentPlayer.setMoney(money);
        }
    }
}