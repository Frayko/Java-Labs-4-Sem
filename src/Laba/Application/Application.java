package Laba.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.Timer;
import java.util.TimerTask;

import Laba.Habitat.Habitat;

public class Application extends JFrame {
    private int window_width;
    private int window_height;

    private JLabel timerLabel;
    private JLabel timerStatus;
    private JTextArea infoLabel;

    private boolean TIMER_LABEL_VISIBLE;

    private Timer timer;
    private Habitat habitat;

    public Application(int width, int height, Habitat h) {
        this.habitat = h;
        this.window_width = width;
        this.window_height = height;

        this.setTitle("Генератор рыбок");
        this.setIconImage(new ImageIcon("src/Laba/Assets/window_icon.png").getImage());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(50, 50, window_width, window_height);
        this.setBackground(Color.white);
        this.setResizable(true);

        timerLabel = new JLabel("Таймер: 0.00");
        timerLabel.setBounds(5, 0, 130, 20);
        timerLabel.setForeground(Color.black);
        timerLabel.setFont(new Font("Open Sans", Font.BOLD,16));
        timerLabel.setVisible(true);
        TIMER_LABEL_VISIBLE = true;
        timerLabel.setFocusable(false);
        this.add(timerLabel);

        timerStatus = new JLabel("Статус: " + StatusType.ВЫКЛ);
        timerStatus.setBounds(5,20, 200,20);
        timerStatus.setForeground(Color.black);
        timerStatus.setFont(new Font("Open Sans", Font.BOLD, 16));
        timerStatus.setVisible(true);
        timerStatus.setFocusable(false);
        this.add(timerStatus);

        infoLabel = new JTextArea();
        infoLabel.setBounds(0, 100, 200, 100);
        infoLabel.setBackground(Color.LIGHT_GRAY);
        infoLabel.setForeground(Color.DARK_GRAY);
        infoLabel.setFont(new Font("Ubuntu", Font.ITALIC,14));
        infoLabel.setVisible(false);
        infoLabel.setFocusable(false);
        this.add(infoLabel);

        this.add(habitat);
        this.setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_B: {
                        start();
                        break;
                    }
                    case KeyEvent.VK_E: {
                        stop();
                        break;
                    }
                    case KeyEvent.VK_T: {
                        TIMER_LABEL_VISIBLE = !TIMER_LABEL_VISIBLE;
                        timerLabel.setVisible(TIMER_LABEL_VISIBLE);
                        timerStatus.setVisible(TIMER_LABEL_VISIBLE);
                        break;
                    }
                    case KeyEvent.VK_ESCAPE: {
                        System.exit(0);
                    }
                }
            }
        });
        this.setVisible(true);
    }

    private void start() {
        if(habitat.begin_simulation()) {
            infoLabel.setVisible(false);
            timerStatus.setText("Статус: " + StatusType.ВКЛ);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    timerLabel.setVisible(TIMER_LABEL_VISIBLE);
                    timerLabel.setText("Таймер: " + habitat.getTime());
                }
            }, 0, habitat.getPeriod());
        }
    }

    private void stop() {
        if(habitat.end_simulation()) {
            timerStatus.setText("Статус: " + StatusType.ВЫКЛ);
            infoLabel.setText(habitat.getMetrics());
            infoLabel.setVisible(true);
        }
    }
}
