package LaboratoryWork.Habitat.Fish;

import java.awt.Graphics;
import java.io.Serializable;

public abstract class Fish implements IBehaviour, Serializable {
    private int X;
    private int Y;
    private boolean reverse = false;
    private int imageWidth;
    private int imageHeight;
    private double V = 4.0;

    public Fish(int x, int y) {
        this.X = x;
        this.Y = y;
    }

    public abstract void drawImage(Graphics g);

    public void setV(double v) {
        this.V = v;
    }
    public double getV() {
        return this.V;
    }

    public void setReverse(boolean rv) {
        this.reverse = rv;
    }
    public boolean getReverse() {
        return this.reverse;
    }
    public void setImageWidth(int width) {
        this.imageWidth = width;
    }
    public void setImageHeight(int height) {
        this.imageHeight = height;
    }
    public int getImageWidth() {
        return this.imageWidth;
    }
    public int getImageHeight() {
        return this.imageHeight;
    }
    public void reverseImageWidth() {
        this.imageWidth = -this.imageWidth;
    }
    public void reverseImageHeight() {
        this.imageHeight = -this.imageHeight;
    }

    public void move(int dX, int dY) {
        this.X += (int)((double)dX * this.V);
        this.Y += (int)((double)dY * this.V);
    }

    @Override
    public void setX(int x) {
        this.X = x;
    }
    @Override
    public void setY(int y) {
        this.Y = y;
    }
    @Override
    public int getX() {
        return this.X;
    }
    @Override
    public int getY() {
        return this.Y;
    }
}
