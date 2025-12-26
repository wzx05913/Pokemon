package Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pokemon.Pokemon;

public class Player {
    private int id=-1;
    private int money=0;
    private final List<Pokemon> pets = new ArrayList<>();

    public Player(){}

    public Player(int money, int id) {
        this.money = money;
        this.id = id;
    }

    public int getMoney() {
        return money;
    }
    public void setMoney(int money) {
        this.money = money;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    // 宠物操作
    public List<Pokemon> getPets() {
        return Collections.unmodifiableList(pets);
    }

    public void addPet(Pokemon pet) {
        if (pet != null) {
            pets.add(pet);
        }
    }

    public boolean removePet(Pokemon pet) {
        return pets.remove(pet);
    }

    public boolean hasPets() {
        return !pets.isEmpty();
    }

    @Override
    public String toString() {
        return "Player{ id=" + id + ", money=" + money + ", pets=" + pets.size() + "}";
    }
}
