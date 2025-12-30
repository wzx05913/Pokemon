// MazePlayer.java
package Player;

import core.Point;

public class MazePlayer {
    private Point position;

    public MazePlayer(Point startPos) {
        //用getter获取startPos的坐标
        this.position = new Point(startPos.getX(), startPos.getY());
    }

    public void moveUp() {
        position = new Point(position.getX() - 1, position.getY());
    }

    public void moveDown() {
        position = new Point(position.getX() + 1, position.getY());
    }

    public void moveLeft() {
        position = new Point(position.getX(), position.getY() - 1);
    }

    public void moveRight() {
        position = new Point(position.getX(), position.getY() + 1);
    }

    public Point getPosition() {
        return position;
    }

    public int getX() {
        return position.getX();  //获取x
    }

    public int getY() {
        return position.getY();  //获取y
    }

    public void setPosition(int x, int y) {
        this.position = new Point(x, y);
    }
}