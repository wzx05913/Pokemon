package core;

import java.util.Objects;

// 纯数据类，封装坐标点，无业务逻辑
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getter
    public int getX() { return x; }
    public int getY() { return y; }

    // 重写equals和hashCode，用于判断点是否重合
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}