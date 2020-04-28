package Laba.Habitat;

import java.awt.*;

public abstract class Fish implements IBehaviour {
    protected int X;
    protected int Y;

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
    public double getX() { return this.X; }
    @Override
    public double getY() { return this.Y; }
}
