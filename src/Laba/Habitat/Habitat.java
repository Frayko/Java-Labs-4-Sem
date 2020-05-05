package Laba.Habitat;

import javax.swing.*;
import java.util.Vector;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;

public class Habitat extends JComponent {
    private long BEGIN_TIME = 0;
    private long END_TIME = 0;
    private long LAST_SPAWN_TIME_GOLDEN = 0;
    private long LAST_SPAWN_TIME_GUPPI = 0;
    private FishCreator fishCreator = new FishCreator();
    private boolean IS_RUNNING = false;
    private Vector<Fish> fishs;
    Timer timer = null;

    //Параметры
    private int N1 = 500;
    private int N2 = 300;
    private double P1 = 0.4;
    private double P2 = 0.5;
    private long PERIOD = 10;

    public Habitat() {}

    public void update(long time) {
        int window_width = this.getWidth();
        int window_height = this.getHeight();
        boolean isRepaint = false;

        if ((time - LAST_SPAWN_TIME_GOLDEN) % N1 == 0) {
            LAST_SPAWN_TIME_GOLDEN = time;
            if ((float) Math.random() <= P1 && P1 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                fishs.addElement(fishCreator.createFish(FishTypes.GoldenFish, x, y));
                isRepaint = true;
            }
        }

        if ((time - LAST_SPAWN_TIME_GUPPI) % N2 == 0) {
            LAST_SPAWN_TIME_GUPPI = time;
            if ((float) Math.random() <= P2 && P2 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                fishs.addElement(fishCreator.createFish(FishTypes.GuppiFish, x, y));
                isRepaint = true;
            }
        }

        if(isRepaint)
            repaint();
    }

    public boolean begin_simulation() {
        if(IS_RUNNING)
            return false;
        IS_RUNNING = true;
        startTimer();
        return true;
    }

    public boolean end_simulation() {
        if(!IS_RUNNING)
            return false;
        IS_RUNNING = false;
        timer.cancel();
        fishs.removeAllElements();
        return true;
    }

    public void startTimer() {
        fishs = new Vector<>();
        reset();
        BEGIN_TIME = System.currentTimeMillis() / PERIOD * PERIOD;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                END_TIME = System.currentTimeMillis() / PERIOD * PERIOD;
                update(END_TIME - BEGIN_TIME);
            }
        }, 0, PERIOD);
    }

    public void reset() {
        fishs.removeAllElements();
        fishCreator.resetFishs();
    }

    @Override
    public void paint(Graphics g) {
        if(fishs != null) {
            for(Fish fish : fishs) {
                fish.drawImage(g);
            }
        }
    }

    public double getTime() { return (END_TIME - BEGIN_TIME) / 1000.; }
    public long getPeriod() { return PERIOD; }

    public String getMetrics() {
        String str = "Информация о симуляции:\n";
        str += "Время: "; str += (END_TIME - BEGIN_TIME) / 1000.;
        str += "\nВсего было рыбок: ";
        str += fishCreator.getAllFishCount();
        str += "\nЗолотых: "; str += fishCreator.getFishCount(FishTypes.GoldenFish);
        str += "\nГуппи: "; str += fishCreator.getFishCount(FishTypes.GuppiFish);
        return str;
    }
}
