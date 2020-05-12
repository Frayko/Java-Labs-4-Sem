package LaboratoryWork.Habitat.Fish.FishAI;

import java.lang.Thread;

public abstract class BaseAI extends Thread {
    int dX;
    int dY;
    int windowWidth;
    int windowHeight;
    boolean isRunning = true;
    abstract public void run();

    public void setDirection(int dx, int dy) {
        dX = dx;
        dY = dy;
    }

    public void setWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public synchronized void pause() {
        isRunning = false;
        notifyAll();
    }

    public synchronized void resume_() {
        isRunning = true;
        notifyAll();
    }
}
