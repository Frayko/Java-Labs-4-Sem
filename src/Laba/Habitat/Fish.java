package Laba.Habitat;

import java.awt.*;

public abstract class Fish implements IBehaviour {
    private int X;
    private int Y;

    public Fish(int x, int y) {
        X = x;
        Y = y;
    }

    public abstract void drawImage(Graphics g);

    @Override
    public void setX(int x) { this.X = x; }
    @Override
    public void setY(int y) { this.Y = y; }
    @Override
    public int getX() { return this.X; }
    @Override
    public int getY() { return this.Y; }
}
