package Player;

public class Player {
    public String name;
    private int id;
    private int money;
    public Player(String name, int money) {
        this.name = name;
        this.money = money;
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
}
