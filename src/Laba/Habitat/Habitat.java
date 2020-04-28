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
    private long PERIOD;
    private FishCreator fishCreator = new FishCreator();
    private boolean IS_RUNNING = false;
    private Vector<Fish> fishs;
    Timer timer = null;

    //Параметры
    private int N1,N2;
    private double P1, P2;

    public Habitat(int N1, int N2, double P1, double P2, int PERIOD) {
        this.N1 = N1;
        this.N2 = N2;
        this.P1 = P1;
        this.P2 = P2;
        this.PERIOD = PERIOD;
    }

    public void update(long time) {
        int window_width = this.getWidth();
        int window_height = this.getHeight();

        if ((time - LAST_SPAWN_TIME_GOLDEN) % N1 == 0) {
            LAST_SPAWN_TIME_GOLDEN = time;
            if ((float) Math.random() <= P1 && P1 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                fishs.addElement(fishCreator.createFish(FishTypes.GoldenFish, x, y));
            }
        }

        if ((time - LAST_SPAWN_TIME_GUPPI) % N2 == 0) {
            LAST_SPAWN_TIME_GUPPI = time;
            if ((float) Math.random() <= P2 && P2 > 0) {
                int x = (int) (Math.random() * (window_width - 100));
                int y = (int) (Math.random() * (window_height - 70));
                fishs.addElement(fishCreator.createFish(FishTypes.GuppiFish, x, y));
            }
        }
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
                repaint();
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
