package LaboratoryWork.Habitat;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;

public class Habitat extends JComponent {
    private long LAST_SPAWN_TIME_GOLDEN = 0;
    private long LAST_SPAWN_TIME_GUPPIES = 0;
    private final FishCreator fishCreator = new FishCreator();
    private Status status = Status.ВЫКЛ;
    private long ticks = 0;
    Timer timer = null;

    //Параметры
    private int N1;
    private int N2;
    private double P1;
    private double P2;
    private final long PERIOD = 100;

    public Habitat() {
        N1 = 5;
        N2 = 3;
        P1 = 0.4;
        P2 = 0.5;
    }

    public void update() {
        ticks++;
        int window_width = this.getWidth();
        int window_height = this.getHeight();

        if ((ticks - LAST_SPAWN_TIME_GOLDEN) % N1 == 0) {
            LAST_SPAWN_TIME_GOLDEN = ticks;
            if ((float) Math.random() <= P1 && P1 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                FishArray.getFishArray().addFish(fishCreator.createFish(FishTypes.GoldenFish, x, y));
            }
        }

        if ((ticks - LAST_SPAWN_TIME_GUPPIES) % N2 == 0) {
            LAST_SPAWN_TIME_GUPPIES = ticks;
            if ((float) Math.random() <= P2 && P2 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                FishArray.getFishArray().addFish(fishCreator.createFish(FishTypes.GuppiesFish, x, y));
            }
        }
    }

    public void begin_simulation() {
        status = Status.ВКЛ;
        startTimer();
    }

    public void end_simulation() {
        status = Status.ВЫКЛ;
        timer.cancel();
        ticks = 0;
        fishCreator.resetFishes();
        FishArray.getFishArray().removeAllFishes();
        repaint();
    }

    public void pause_simulation() {
        status = Status.ПАУЗА;
        timer.cancel();
    }

    public void continue_simulation() {
        status = Status.ВКЛ;
        startTimer();
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 0, PERIOD);
    }

    @Override
    public void paint(Graphics g) {
        FishArray.getFishArray().drawFishes(g);
    }

    public Status getStatus() { return status; }

    public void setN1(int n1) { N1 = n1; }
    public void setN2(int n2) { N2 = n2; }
    public void setP1(double p1) { P1 = p1; }
    public void setP2(double p2) { P2 = p2; }
    public int getN1() { return N1; }
    public int getN2() { return N2; }
    public double getP1() { return P1; }
    public double getP2() { return P2; }
    public double getTime() { return ticks / 10.; }
    public long getPeriod() { return PERIOD; }

    public String getMetrics() {
        String str = "Прошло времени (сек): "; str += ticks / 10.;
        str += "\nВсего было рыбок: ";
        str += fishCreator.getAllFishCount();
        str += "\nЗолотых: "; str += fishCreator.getFishCount(FishTypes.GoldenFish);
        str += "\nГуппи: "; str += fishCreator.getFishCount(FishTypes.GuppiesFish);
        return str;
    }
}
