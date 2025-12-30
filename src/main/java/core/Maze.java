// src/main/java/core/Maze.java
package core;

import java.util.Random;

public class Maze {
    private static final int SIZE = 15; // 固定迷宫大小
    private static final int WALL = 1;
    private static final int PATH = 0;
    private static final int TREASURE = 2; // 橙黄色点：敌人/宝箱

    private int[][] grid;
    private Point start;
    private Point end;

    public Maze() {
        grid = new int[SIZE][SIZE];
        initializeGrid();
        generateMaze();
        setStartEndPoints();
        placeTreasures();

    }

    private void initializeGrid() {
        // 初始化全为墙
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = WALL;
            }
        }
    }

    private void generateMaze() {
        Random random = new Random();
        // 从左上角开始(1,1)
        dfs(1, 1, random);
    }

    private void dfs(int x, int y, Random random) {
        grid[x][y] = PATH;

        // 四个方向
        int[] dx = {-2, 0, 2, 0};
        int[] dy = {0, 2, 0, -2};
        int[] order = {0, 1, 2, 3};

        //打乱方向
        for (int i = 0; i < 4; i++) {
            int j = random.nextInt(4);
            int temp = order[i];
            order[i] = order[j];
            order[j] = temp;
        }

        //尝试每个方向
        for (int dir : order) {
            int nx = x + dx[dir];
            int ny = y + dy[dir];
            
            // 检查新位置是否在边界内且为墙
            if (nx > 0 && nx < SIZE - 1 && ny > 0 && ny < SIZE - 1 && grid[nx][ny] == WALL) {
                // 打通当前单元格与下一个单元格之间的墙
                grid[x + dx[dir] / 2][y + dy[dir] / 2] = PATH;
                dfs(nx, ny, random);
            }
        }
    }

    private void placeTreasures() {
        Random random = new Random();
        int treasureCount = 30 + random.nextInt(5); // 宝藏个数
        for (int i = 0; i < treasureCount; i++) {
            int x, y;
            do {
                x = random.nextInt(SIZE);
                y = random.nextInt(SIZE);
            }while (grid[x][y] != PATH || 
                    (x == start.getX() && y == start.getY()) ||
                    (x == end.getX() && y == end.getY()));

            grid[x][y] = TREASURE;
        }
    }

    private void setStartEndPoints() {
        start = new Point(1, 1);
        end = new Point(SIZE - 2, SIZE - 2);
        grid[start.getX()][start.getY()] = PATH;
        grid[end.getX()][end.getY()] = PATH;
    }

    // Getters
    public int getSize() {
        return SIZE;
    }

    public int[][] getGrid() {
        return grid;
    }

    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    public boolean isWall(int x, int y) {
        return grid[x][y] == WALL;
    }

    public boolean isTreasure(int x, int y) {
        return grid[x][y] == TREASURE;
    }

    public boolean isEnd(int x, int y) {
    	return x == end.getX() && y == end.getY();
    }
}