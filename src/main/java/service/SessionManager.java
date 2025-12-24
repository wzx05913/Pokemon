// SessionManager.java
package service;

import entity.User;
import entity.Pet;
import pokemon.Pokemon;

/**
 * 会话管理器
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Pet currentPet;
    private Pokemon currentPokemon;  // 添加Pokemon对象

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public Pet getCurrentPet() { return currentPet; }
    public void setCurrentPet(Pet pet) { this.currentPet = pet; }

    public Pokemon getCurrentPokemon() { return currentPokemon; }
    public void setCurrentPokemon(Pokemon pokemon) { this.currentPokemon = pokemon; }

    public void clearSession() {
        currentUser = null;
        currentPet = null;
        currentPokemon = null;
    }
}
