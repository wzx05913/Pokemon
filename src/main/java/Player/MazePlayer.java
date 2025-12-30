// MazePlayer.java
package Player;

import core.Point;

public class MazePlayer {
    private Point position;

    public MazePlayer(Point startPos) {
        //用getter获取startPos的坐标
        this.position = new Point(startPos.getX(), startPos.getY());
    }

    // 上移：创建新Point更新位置
    public void moveUp() {
        position = new Point(position.getX() - 1, position.getY());
    }

    // 下移：创建新Point更新位置
    public void moveDown() {
        position = new Point(position.getX() + 1, position.getY());
    }

    // 左移：创建新Point更新位置
    public void moveLeft() {
        position = new Point(position.getX(), position.getY() - 1);
    }

    // 右移：创建新Point更新位置（原代码x++错误，应为y++）
    public void moveRight() {
        position = new Point(position.getX(), position.getY() + 1);
    }

    // Getters
    public Point getPosition() {
        return position;
    }

    public int getX() {
        return position.getX();  // 用getter获取x
    }

    public int getY() {
        return position.getY();  // 用getter获取y
    }

    // 更新位置的方法
    public void setPosition(int x, int y) {
        this.position = new Point(x, y);
    }
}