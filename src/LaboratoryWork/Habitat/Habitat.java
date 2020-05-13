package LaboratoryWork.Habitat;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;

import LaboratoryWork.Habitat.Fish.*;
import LaboratoryWork.Habitat.Fish.FishAI.GoldenFishAI;
import LaboratoryWork.Habitat.Fish.FishAI.GuppiesFishAI;

public class Habitat extends JComponent {
    private long LAST_SPAWN_TIME_GOLDEN;
    private long LAST_SPAWN_TIME_GUPPIES;
    private GoldenFishAI goldenFishAI;
    private GuppiesFishAI guppiesFishAI;
    private final FishCreator fishCreator = new FishCreator();
    private Status status = Status.ВЫКЛ;
    private long ticks = 0;
    Timer timer = null;

    //Параметры
    private double N1;
    private double N2;
    private int P1;
    private int P2;
    private double goldenTTL;
    private double guppiesTTL;
    private final long PERIOD = 100;

    public Habitat() {
        N1 = 0.5;
        N2 = 0.3;
        P1 = 40;
        P2 = 50;
        goldenTTL = 1.0;
        guppiesTTL = 2.0;
        goldenFishAI = new GoldenFishAI();
        guppiesFishAI = new GuppiesFishAI();
    }

    public void update() {
        ticks++;
        int window_width = this.getWidth();
        int window_height = this.getHeight();
        goldenFishAI.setWindowSize(window_width, window_height);
        guppiesFishAI.setWindowSize(window_width, window_height);

        FishArray.getFishArray().checkTTL(ticks, goldenTTL * 10., guppiesTTL * 10.);

        if ((ticks - LAST_SPAWN_TIME_GOLDEN) >= (N1 * (PERIOD / 10.))) {
            LAST_SPAWN_TIME_GOLDEN = ticks;
            if ((float) Math.random() <= (float) P1 / 100. && P1 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                FishArray.getFishArray().addFish(fishCreator.createFish(FishTypes.GoldenFish, x, y), ticks);
            }
        }

        if ((ticks - LAST_SPAWN_TIME_GUPPIES) >= (N2 * (PERIOD / 10.))) {
            LAST_SPAWN_TIME_GUPPIES = ticks;
            if ((float) Math.random() <= (float) P2 / 100. && P2 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                FishArray.getFishArray().addFish(fishCreator.createFish(FishTypes.GuppiesFish, x, y), ticks);
            }
        }

        goldenFishAI.setDirection(2,0);
        guppiesFishAI.setDirection(0,2);
    }

    public void begin_simulation() {
        goldenFishAI = new GoldenFishAI();
        guppiesFishAI = new GuppiesFishAI();
        goldenFishAI.start();
        guppiesFishAI.start();
        status = Status.ВКЛ;
        startTimer();
        LAST_SPAWN_TIME_GOLDEN = 0;
        LAST_SPAWN_TIME_GUPPIES = 0;
    }

    public void end_simulation() {
        status = Status.ВЫКЛ;
        goldenFishAI.interrupt();
        guppiesFishAI.interrupt();
        timer.cancel();
        ticks = 0;
        fishCreator.resetFishes();
        FishArray.getFishArray().removeAllFishes();
        repaint();
    }

    public void pause_simulation() {
        status = Status.ПАУЗА;
        goldenFishAI.pause();
        guppiesFishAI.pause();
        timer.cancel();
    }

    public void continue_simulation() {
        status = Status.ВКЛ;
        startTimer();
    }

    public void pause_goldenAI() {
        goldenFishAI.pause();
    }

    public void pause_guppiesAI() {
        guppiesFishAI.pause();
    }

    public void resume_goldenAI() {
        goldenFishAI.resume_();
    }

    public void resume_guppiesAI() {
        guppiesFishAI.resume_();
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

    public void setN1(double n1) { N1 = n1; }
    public void setN2(double n2) { N2 = n2; }
    public void setP1(int p1) { P1 = p1; }
    public void setP2(int p2) { P2 = p2; }
    public void setGoldenTTL(double ttl) { goldenTTL = ttl; }
    public void setGuppiesTTL(double ttl) { guppiesTTL = ttl; }
    public void setGoldenFishAIPriority(int priority) { goldenFishAI.setPriority(priority); }
    public void setGuppiesFishAIPriority(int priority) { guppiesFishAI.setPriority(priority); }
    public double getN1() { return N1; }
    public double getN2() { return N2; }
    public int getP1() { return P1; }
    public int getP2() { return P2; }
    public double getTime() { return ticks / (PERIOD / 10.); }
    public long getPeriod() { return PERIOD; }
    public double getGoldenTTL() { return goldenTTL; }
    public double getGuppiesTTL() { return guppiesTTL; }
    public int getGoldenFishAIPriority() { return goldenFishAI.getPriority(); }
    public int getGuppiesFishAIPriority() { return guppiesFishAI.getPriority(); }

    public String getMetrics() {
        String str = "Прошло времени (сек): "; str += getTime();
        str += "\nВсего было рыбок: ";
        str += fishCreator.getAllFishCount();
        str += "\nЗолотых: "; str += fishCreator.getFishCount(FishTypes.GoldenFish);
        str += "\nГуппи: "; str += fishCreator.getFishCount(FishTypes.GuppiesFish);
        str += "\n----------------------------------------------";
        return str;
    }
}
